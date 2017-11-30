
package com.calendarfx.login;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import com.calendarfx.view.CalendarView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;

/**
 *
 * @author sveta
 * 
 * 
 */
class ServerConnection implements Runnable {
    
    private BufferedReader in = null;
    public boolean active;
    //public boolean toBeDeleted;
    Socket server;
    //Integer toDelete;
    int number;
    CalendarView view;
    
    /**
     * establishes the connection with the server
     * @param server Socket to connect to
     * @param number used as a checker
     * @param view CalendarView that is set up for the whole calendar
     */
    public ServerConnection(Socket server, int number, CalendarView view){
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(this.server.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        active = true;
        this.view = view;
    }
    
    @Override
    /**
     * to run the thread
     */
    public void run() {
         String msg;
        try {
            while((msg = in.readLine())!=null){
                String [] parts = msg.split("\\|");
                //String parts1 = parts[0];
                System.out.println("I have recieved a message!");
                System.out.println(msg);
                String parts2 = parts[0];
                String parts3 = parts[1];
                String parts4 = parts[2];
                
                //Entry entry = new Entry(parts2);
                //LocalDateTime time;
                System.out.println(parts4);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm");
                LocalDateTime start = LocalDateTime.parse(parts3, formatter);
                LocalDateTime end = LocalDateTime.parse(parts4, formatter);
                Interval inter = new Interval(start,end);
                //ZonedDateTime zt = inter.getStartZonedDateTime();
                
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        view.createCalendarSource();
                        //System.out.println(zt);
                        ZoneId zoneId = ZoneId.of("UTC+8");
                        ZonedDateTime zt;
                        zt = ZonedDateTime.of(start, zoneId);
                        //Calendar cal = new Calendar("requested");
                        CalendarSource s = view.getCalendarSources().get(view.getCalendarSources().size()-1);
                        Calendar cal = s.getCalendars().get(s.getCalendars().size()-1);
                        //entry.setInterval(new Interval(start,end));
                        Entry entry = view.createEntryAt(zt,cal);

                        //Entry entry = view.createEntryAt(new Interval(start,end),cal);
                        entry.setTitle(parts2);    
                    }
                  });
                 
                        
                        }
        } catch (IOException ex) {
            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
               
    }
    /**
     * to check if the sever is active
     * @return true if active false if not 
     */
    boolean isActive() {
            return active;
    }
}
  
