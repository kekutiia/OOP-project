/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view;

import com.calendarfx.view.DayEntryView;
import com.calendarfx.view.DayViewBase;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.time.LocalTime;
import java.util.Objects;

/**
 * A specialized scrollpane used for automatic scrolling when the user performs
 * a drag operation close to the edges of the pane.
 */
public class DayViewScrollPane extends Pane {

    private DayViewBase dayView;

    private LocalTime cachedStartTime;

    /**
     * Constructs a new scrollpane for the given content node.
     *
     * @param dayView the content node
     */
    public DayViewScrollPane(DayViewBase dayView, ScrollBar scrollBar) {
        super();

        this.dayView = Objects.requireNonNull(dayView);
        this.dayView.setManaged(false);
        this.dayView.layoutBoundsProperty().addListener(it -> requestLayout());

        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.maxProperty().bind(dayView.heightProperty().subtract(heightProperty()));
        scrollBar.visibleAmountProperty()
                .bind(Bindings.multiply(scrollBar.maxProperty(),
                        Bindings.divide(heightProperty(), dayView.heightProperty())));
        scrollBar.valueProperty().addListener(it -> dayView.setTranslateY(scrollBar.getValue() * -1));

        // user clicks on scrollbar arrows -> scroll one hour
        scrollBar.unitIncrementProperty().bind(dayView.hourHeightProperty());

        // user clicks in backround of scrollbar = block scroll -> scroll half a page
        scrollBar.blockIncrementProperty().bind(heightProperty().divide(2));

        dayView.translateYProperty().addListener(it -> {
            updateVisibleTimeRange();
            cachedStartTime = getStartTime();
            scrollBar.setValue(-dayView.getTranslateY());
        });

        getChildren().add(dayView);

        heightProperty().addListener(it -> updateVisibleTimeRange());

        dayView.sceneProperty().addListener(it -> updateVisibleTimeRange());
        dayView.heightProperty().addListener(it -> updateVisibleTimeRange());
        dayView.visibleHoursProperty().addListener(it -> updateVisibleTimeRange());

        dayView.earlyLateHoursStrategyProperty().addListener(it -> requestLayout());
        dayView.hourHeightCompressedProperty().addListener(it -> requestLayout());
        dayView.hoursLayoutStrategyProperty().addListener(it -> requestLayout());
        dayView.hourHeightProperty().addListener(it -> requestLayout());

        updateVisibleTimeRange();

        addEventFilter(ScrollEvent.SCROLL, evt -> scrollY(evt.getDeltaY()));

        // regular drag, e.g. of an entry view
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::autoscrollIfNeeded);
        addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> stopAutoScrollIfNeeded());

        // drag and drop from the outside
        // TODO: PUT BACK IN addEventFilter(MouseEvent.DRAG_DETECTED, evt -> startDrag(evt));
        addEventFilter(DragEvent.DRAG_OVER, this::autoscrollIfNeeded);
        addEventFilter(DragEvent.DRAG_EXITED, evt -> stopAutoScrollIfNeeded());
        addEventFilter(DragEvent.DRAG_DROPPED, evt -> stopAutoScrollIfNeeded());
        addEventFilter(DragEvent.DRAG_DONE, evt -> stopAutoScrollIfNeeded());

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    private void scrollY(double deltaY) {
        Insets insets = getInsets();
        dayView.setTranslateY(Math.min(0, Math.max(dayView.getTranslateY() + deltaY, getMaxTranslateY(insets))));
    }

    public void scrollToTime(LocalTime time) {
        double y = ViewHelper.getTimeLocation(dayView, time, true);
        Insets insets = getInsets();

        // place the given time at one third of the visible height
        dayView.setTranslateY(Math.min(0, Math.max(-y + getHeight() / 3, getMaxTranslateY(insets))));
    }

    private void updateVisibleTimeRange() {
        if (dayView.getScene() == null || dayView.getHeight() == 0) {
            return;
        }

        final LocalTime startTime = dayView.getZonedDateTimeAt(0, -dayView.getTranslateY()).toLocalTime();
        final LocalTime endTime = dayView.getZonedDateTimeAt(0, -dayView.getTranslateY() + getHeight()).toLocalTime();

        this.startTime.set(startTime);
        this.endTime.set(endTime);
    }

    // visible start time support

    private ReadOnlyObjectWrapper<LocalTime> startTime = new ReadOnlyObjectWrapper<>(this, "startTime", LocalTime.MIN);

    public final ReadOnlyObjectProperty<LocalTime> startTimeProperty() {
        return startTime.getReadOnlyProperty();
    }

    public final LocalTime getStartTime() {
        return startTime.get();
    }

    // visible end time support

    private ReadOnlyObjectWrapper<LocalTime> endTime = new ReadOnlyObjectWrapper<>(this, "endTime", LocalTime.MIN);

    public final ReadOnlyObjectProperty<LocalTime> endTimeProperty() {
        return endTime.getReadOnlyProperty();
    }

    public final LocalTime getEndTime() {
        return endTime.get();
    }

    public DayViewBase getDayView() {
        return dayView;
    }

    @Override
    protected double computePrefWidth(double height) {
        return getDayView().prefWidth(-1);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        switch (dayView.getHoursLayoutStrategy()) {
            case FIXED_HOUR_COUNT:
                double height = getHeight();
                int visibleHours = dayView.getVisibleHours();
                dayView.setHourHeight(Math.max(1, height / visibleHours)); // height must be at least 1px
                break;
            case FIXED_HOUR_HEIGHT:
                break;
        }

        Insets insets = getInsets();


        final double ph = dayView.prefHeight(-1);
        dayView.resizeRelocate(
                snapPosition(insets.getLeft()),
                snapPosition(insets.getTop()),
                snapSize(getWidth() - insets.getLeft() - insets.getRight()),
                snapSize(Math.max(ph, getHeight() - insets.getTop() - insets.getBottom())));

        switch (dayView.getHoursLayoutStrategy()) {
            case FIXED_HOUR_COUNT:
                if (cachedStartTime != null) {
                    dayView.setTranslateY(-ViewHelper.getTimeLocation(dayView, cachedStartTime, true));
                }
                break;
            case FIXED_HOUR_HEIGHT:
                break;
        }

        if (dayView.getTranslateY() + dayView.getHeight() < getHeight() - insets.getTop() - insets.getBottom()) {
            dayView.setTranslateY(getMaxTranslateY(insets));
        }
    }

    private double getMaxTranslateY(Insets insets) {
        return (getHeight() - insets.getTop() - insets.getBottom()) - dayView.getHeight();
    }

    private void startDrag(MouseEvent evt) {
        EventTarget target = evt.getTarget();
        if (!isOnEntry(target)) {
            return;
        }
        Dragboard db = startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();

        /*
         * We have to add some content, otherwise drag over will not be called.
         */
        content.putString("dummy"); //$NON-NLS-1$
        db.setContent(content);
    }

    private boolean isOnEntry(EventTarget target) {
        if (target == null || !(target instanceof Node)) {
            return false;
        }

        Node node = (Node) target;
        if (node instanceof DayEntryView) {
            return true;
        }

        return isOnEntry(node.getParent());
    }

    private void autoscrollIfNeeded(DragEvent evt) {
        evt.acceptTransferModes(TransferMode.ANY);

        if (getBoundsInLocal().getWidth() < 1) {
            if (getBoundsInLocal().getWidth() < 1) {
                stopAutoScrollIfNeeded();
                return;
            }
        }

        double yOffset = 0;

        // y offset

        double delta = evt.getSceneY() - localToScene(0, 0).getY();
        double proximity = 20;
        if (delta < proximity) {
            yOffset = -(proximity - delta);
        }

        delta = localToScene(0, 0).getY() + getHeight() - evt.getSceneY();
        if (delta < proximity) {
            yOffset = proximity - delta;
        }

        if (yOffset != 0) {
            autoscroll(yOffset);
        } else {
            stopAutoScrollIfNeeded();
        }
    }

    private void autoscrollIfNeeded(MouseEvent evt) {
        if (getBoundsInLocal().getWidth() < 1) {
            if (getBoundsInLocal().getWidth() < 1) {
                stopAutoScrollIfNeeded();
                return;
            }
        }

        double yOffset = 0;

        // y offset

        double delta = evt.getSceneY() - localToScene(0, 0).getY();
        if (delta < 0) {
            yOffset = Math.max(delta / 2, -10);
        }

        delta = localToScene(0, 0).getY() + getHeight() - evt.getSceneY();
        if (delta < 0) {
            yOffset = Math.min(-delta / 2, 10);
        }

        if (yOffset != 0) {
            autoscroll(yOffset);
        } else {
            stopAutoScrollIfNeeded();
        }
    }

    class ScrollThread extends Thread {
        private boolean running = true;
        private double yOffset;

        public ScrollThread() {
            super("Autoscrolling List View"); //$NON-NLS-1$
            setDaemon(true);
        }

        @Override
        public void run() {

            /*
             * Some initial delay, especially useful when dragging something in
             * from the outside.
             */

            try {
                Thread.sleep(300);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            while (running) {

                Platform.runLater(this::scrollToY);

                try {
                    sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void scrollToY() {
            scrollY(-yOffset);
        }

        public void stopRunning() {
            this.running = false;
        }

        public void setDelta(double yOffset) {
            this.yOffset = yOffset;
        }
    }

    private ScrollThread scrollThread;

    private void autoscroll(double yOffset) {
        if (scrollThread == null) {
            scrollThread = new ScrollThread();
            scrollThread.start();
        }

        scrollThread.setDelta(yOffset);
    }

    private void stopAutoScrollIfNeeded() {
        if (scrollThread != null) {
            scrollThread.stopRunning();
            scrollThread = null;
        }
    }
}