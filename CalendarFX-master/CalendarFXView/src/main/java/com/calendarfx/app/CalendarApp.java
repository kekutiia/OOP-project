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

package com.calendarfx.app;

import com.calendarfx.login.SendClient;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import static java.lang.Thread.sleep;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import javafx.stage.StageStyle;

public class CalendarApp extends Application {
    
    String email;
    
    public CalendarApp(String email){
       this.email = email;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        CalendarView calendarView = new CalendarView();
        
        
            CalendarSimon(primaryStage, calendarView);
            SendClient client = new SendClient(this.email, calendarView);
                client.connect("127.0.0.1", "1664");
       }
        
    /**
     *creates a calendar for user Simon (ideally, but actually does not)
     * @param primaryStage the stage of the calendar
     * @param calendarView the view of the calendar 
     */
    public void CalendarSimon(Stage primaryStage, CalendarView calendarView) {
        Calendar katja = new Calendar("Grading");
        Calendar dirk = new Calendar("Simon");
        Calendar philip = new Calendar("Assignments");
        Calendar jule = new Calendar("Planning a sabbatical");
        //Calendar armin = new Calendar("Armin");
        Calendar birthdays = new Calendar("Birthdays");
        Calendar holidays = new Calendar("Holidays");

        katja.setShortName("G");
        dirk.setShortName("S");
        philip.setShortName("A");
        jule.setShortName("J");
        //armin.setShortName("A");
        birthdays.setShortName("B");
        holidays.setShortName("H");

        katja.setStyle(Style.STYLE1);
        dirk.setStyle(Style.STYLE2);
        philip.setStyle(Style.STYLE3);
        jule.setStyle(Style.STYLE4);
        //armin.setStyle(Style.STYLE5);
        birthdays.setStyle(Style.STYLE6);
        holidays.setStyle(Style.STYLE7);

        CalendarSource familyCalendarSource = new CalendarSource("Family");
        familyCalendarSource.getCalendars().addAll(birthdays, holidays, katja, dirk, philip, jule);

        calendarView.getCalendarSources().setAll(familyCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(calendarView); // introPane);

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        // update every 10 seconds
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        Scene scene = new Scene(stackPane);
        primaryStage.setTitle("pick");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        primaryStage.setResizable(true);
        //primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    /*public static void main(String[] args) {
        launch(args);
    }*/
}
