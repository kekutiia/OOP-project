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

package com.calendarfx.view.popover;

import com.calendarfx.exceptions.EmailParser;
import com.calendarfx.exceptions.IncorrectEmailInput;

import com.calendarfx.login.SendClient;
import com.calendarfx.model.Entry;
import com.calendarfx.util.Util;
import com.calendarfx.view.Messages;
import com.calendarfx.view.RecurrenceView;
import com.calendarfx.view.TimeField;
import impl.com.calendarfx.view.DayEntryViewSkin;
import java.io.IOException;
import java.time.LocalDateTime;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import com.calendarfx.login.MessageUtils;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.controlsfx.control.textfield.CustomTextField;

public class EntryDetailsView extends EntryPopOverPane {

    private final Label summaryLabel;
    private final MenuButton recurrenceButton;

    public EntryDetailsView(Entry<?> entry) {
        super();

        getStyleClass().add("entry-details-view");

        Label fullDayLabel = new Label(Messages.getString("EntryDetailsView.FULL_DAY")); //$NON-NLS-1$
        Label addEmailLabel = new Label(Messages.getString("EntryDetailsView.ADD_EMAIL")); //$NON-NLS-1$
        Label requestMeetingLabel = new Label(Messages.getString("EntryDetailsView.REQUEST_MEETING")); //$NON-NLS-1$
        Label startDateLabel = new Label(Messages.getString("EntryDetailsView.FROM")); //$NON-NLS-1$
        Label endDateLabel = new Label(Messages.getString("EntryDetailsView.TO")); //$NON-NLS-1$
        Label recurrentLabel = new Label(Messages.getString("EntryDetailsView.REPEAT")); //$NON-NLS-1$

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("recurrence-summary-label"); //$NON-NLS-1$
        summaryLabel.setWrapText(true);
        summaryLabel.setMaxWidth(300);

        CheckBox fullDay = new CheckBox();
        fullDay.disableProperty().bind(entry.getCalendar().readOnlyProperty());
       
        CheckBox requestMeeting = new CheckBox();
        requestMeeting.disableProperty();
        
        CustomTextField emailField = new CustomTextField();
        emailField.disableProperty();
        
        
        
        /**
         * this is for requesting a meeting: sets up a new socket which sends the meeting request
         */
        Button requestButton = new Button(Messages.getString("EntryDetailsView.REQUEST")); //$NON-NLS-1$
        requestButton.setDefaultButton(true);
        requestButton.setOnAction((ActionEvent evt) -> {
            try
            {
            String email = emailField.getText();
            EmailParser myParser = new EmailParser(email);
            myParser.parse();
            String temp = entry.getTitle();
            if (!(temp.contains("Pending Approval")))
            entry.setTitle(temp + ": Pending Approval");
            LocalDateTime start = entry.getStartAsLocalDateTime();
            LocalDateTime finish = entry.getEndAsLocalDateTime();
            String start_str = start.toString();
            String end_str = finish.toString();
            String to_send = temp + "|" + start_str + "|" + end_str;
            Random rand = new Random();
            int value = rand.nextInt(50);
            String name = "someemail" + value;
            SendClient client = new SendClient(name, null);
                try {
                    client.connect("127.0.0.1", "1664");
                    client.sendMessageFromConsole(new String(MessageUtils.message(email,to_send)));
                } catch (IOException ex) {
                    Logger.getLogger(EntryDetailsView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            catch (IncorrectEmailInput e)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid email input");
                alert.setContentText("This is not a valid email address. Please input valid email");
                alert.showAndWait();
                emailField.clear();
            } catch (Exception ex) {
                Logger.getLogger(EntryDetailsView.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        
        
        TimeField startTimeField = new TimeField();
        startTimeField.setValue(entry.getStartTime());
        startTimeField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        TimeField endTimeField = new TimeField();
        endTimeField.setValue(entry.getEndTime());
        endTimeField.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(entry.getStartDate());
        startDatePicker.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(entry.getEndDate());
        endDatePicker.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        entry.intervalProperty().addListener(it -> {
            startTimeField.setValue(entry.getStartTime());
            endTimeField.setValue(entry.getEndTime());
            startDatePicker.setValue(entry.getStartDate());
            endDatePicker.setValue(entry.getEndDate());
        });

        HBox startDateBox = new HBox(10);
        HBox endDateBox = new HBox(10);
        
        HBox emailBox = new HBox(10);

        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.getChildren().addAll(addEmailLabel, emailField, requestButton);
        
        startDateBox.setAlignment(Pos.CENTER_LEFT);
        endDateBox.setAlignment(Pos.CENTER_LEFT);

        startDateBox.getChildren().addAll(startDateLabel, startDatePicker, startTimeField);
        endDateBox.getChildren().addAll(endDateLabel, endDatePicker, endTimeField);

        fullDay.setSelected(entry.isFullDay());
        startDatePicker.setValue(entry.getStartDate());
        endDatePicker.setValue(entry.getEndDate());

        Set<String> availableZoneIds = ZoneId.getAvailableZoneIds();
        ObservableList<ZoneId> zoneIds = FXCollections.observableArrayList();
        for (String id : availableZoneIds) {
            ZoneId zoneId = ZoneId.of(id);
            if (!zoneIds.contains(zoneId)) {
                zoneIds.add(zoneId);
            }
        }

        zoneIds.sort(Comparator.comparing(ZoneId::getId));

        Label zoneLabel = new Label(Messages.getString("EntryDetailsView.TIMEZONE")); //$NON-NLS-1$

        ComboBox<ZoneId> zoneBox = new ComboBox<>(zoneIds);
        zoneBox.disableProperty().bind(entry.getCalendar().readOnlyProperty());
        zoneBox.setConverter(new StringConverter<ZoneId>() {

            @Override
            public String toString(ZoneId object) {
                return object.getId();
            }

            @Override
            public ZoneId fromString(String string) {
                return null;
            }
        });
        zoneBox.setValue(entry.getZoneId());

        recurrenceButton = new MenuButton(Messages.getString("EntryDetailsView.MENU_BUTTON_NONE")); //$NON-NLS-1$

        MenuItem none = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_NONE")); //$NON-NLS-1$
        MenuItem everyDay = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_DAY")); //$NON-NLS-1$
        MenuItem everyWeek = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_WEEK")); //$NON-NLS-1$
        MenuItem everyMonth = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_MONTH")); //$NON-NLS-1$
        MenuItem everyYear = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_EVERY_YEAR")); //$NON-NLS-1$
        MenuItem custom = new MenuItem(Messages.getString("EntryDetailsView.MENU_ITEM_CUSTOM")); //$NON-NLS-1$

        none.setOnAction(evt -> updateRecurrenceRule(entry, null));
        everyDay.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=DAILY")); //$NON-NLS-1$
        everyWeek.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=WEEKLY")); //$NON-NLS-1$
        everyMonth.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=MONTHLY")); //$NON-NLS-1$
        everyYear.setOnAction(evt -> updateRecurrenceRule(entry, "RRULE:FREQ=YEARLY")); //$NON-NLS-1$
        custom.setOnAction(evt -> showRecurrenceEditor(entry));

        recurrenceButton.getItems().setAll(none, everyDay, everyWeek, everyMonth, everyYear, new SeparatorMenuItem(), custom);
        recurrenceButton.disableProperty().bind(entry.getCalendar().readOnlyProperty());

        GridPane box = new GridPane();
        box.getStyleClass().add("content"); //$NON-NLS-1$
        box.add(fullDayLabel, 0, 0);
        box.add(fullDay, 1, 0);
        box.add(startDateLabel, 0, 1);
        box.add(startDateBox, 1, 1);
        box.add(endDateLabel, 0, 2);
        box.add(endDateBox, 1, 2);
        box.add(zoneLabel, 0, 3);
        box.add(zoneBox, 1, 3);
        //box.add(recurrentLabel, 0, 4);
        //box.add(recurrenceButton, 1, 4);
        box.add(summaryLabel, 1, 5);
        box.add(requestMeetingLabel, 0, 5);
        box.add(requestMeeting, 1, 5);
        
        GridPane.setFillWidth(zoneBox, true);
        GridPane.setHgrow(zoneBox, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();

        col1.setHalignment(HPos.RIGHT);
        col2.setHalignment(HPos.LEFT);

        box.getColumnConstraints().addAll(col1, col2);

        getChildren().add(box);

        startTimeField.visibleProperty().bind(Bindings.not(entry.fullDayProperty()));
        endTimeField.visibleProperty().bind(Bindings.not(entry.fullDayProperty()));

        // start date and time
        startDatePicker.valueProperty().addListener(evt -> entry.changeStartDate(startDatePicker.getValue(), true));
        startTimeField.valueProperty().addListener(evt -> entry.changeStartTime(startTimeField.getValue(), true));

        // end date and time
        endDatePicker.valueProperty().addListener(evt -> entry.changeEndDate(endDatePicker.getValue(), false));
        endTimeField.valueProperty().addListener(evt -> entry.changeEndTime(endTimeField.getValue(), false));

        // full day
        fullDay.setOnAction(evt -> entry.setFullDay(fullDay.isSelected()));

        // request meeting
        //requestMeeting.setOnAction(evt -> box.add(addEmailLabel, 0, 6)); 
        requestMeeting.setOnAction(evt ->{
                if (requestMeeting.isSelected())
                box.addRow(1, emailBox);
                else box.getChildren().removeAll(emailBox);
        });
        
        // zone Id
        zoneBox.setOnAction(evt -> entry.setZoneId(zoneBox.getValue()));

        entry.recurrenceRuleProperty().addListener(it -> updateRecurrenceRuleButton(entry));

        updateRecurrenceRuleButton(entry);

        entry.recurrenceRuleProperty().addListener(it -> updateSummaryLabel(entry));
    }

    private void updateSummaryLabel(Entry<?> entry) {
        String rule = entry.getRecurrenceRule();
        String text = Util.convertRFC2445ToText(rule,
                entry.getStartDate());
        summaryLabel.setText(text);
    }

    private void showRecurrenceEditor(Entry<?> entry) {
        RecurrencePopup popup = new RecurrencePopup();
        RecurrenceView recurrenceView = popup.getRecurrenceView();
        String recurrenceRule = entry.getRecurrenceRule();
        if (recurrenceRule == null || recurrenceRule.trim().equals("")) { //$NON-NLS-1$
            recurrenceRule = "RRULE:FREQ=DAILY;"; //$NON-NLS-1$
        }
        recurrenceView.setRecurrenceRule(recurrenceRule);
        popup.setOnOkPressed(evt -> {
            String rrule = recurrenceView.getRecurrenceRule();
            entry.setRecurrenceRule(rrule);
        });

        Point2D anchor = recurrenceButton.localToScreen(0,
                recurrenceButton.getHeight());
        popup.show(recurrenceButton, anchor.getX(), anchor.getY());
    }

    private void updateRecurrenceRule(Entry<?> entry, String rule) {
        entry.setRecurrenceRule(rule);
    }

    private void updateRecurrenceRuleButton(Entry<?> entry) {
        String rule = entry.getRecurrenceRule();
        if (rule == null) {
            recurrenceButton.setText(Messages.getString("EntryDetailsView.NONE")); //$NON-NLS-1$
        } else {
            switch (rule.trim().toUpperCase()) {
                case "RRULE:FREQ=DAILY": //$NON-NLS-1$
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.DAILY")); //$NON-NLS-1$
                    break;
                case "RRULE:FREQ=WEEKLY": //$NON-NLS-1$
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.WEEKLY")); //$NON-NLS-1$
                    break;
                case "RRULE:FREQ=MONTHLY": //$NON-NLS-1$
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.MONTHLY")); //$NON-NLS-1$
                    break;
                case "RRULE:FREQ=YEARLY": //$NON-NLS-1$
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.YEARLY")); //$NON-NLS-1$
                    break;
                default:
                    recurrenceButton.setText(Messages.getString("EntryDetailsView.CUSTOM")); //$NON-NLS-1$
                    break;
            }
        }
    }
}
