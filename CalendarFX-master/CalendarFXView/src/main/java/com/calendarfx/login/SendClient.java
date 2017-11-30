/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calendarfx.login;

import com.calendarfx.view.CalendarView;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * sending message from client
 * @author sveta
 */
public class SendClient {
    static String name;
    static ServerConnection sc;
    static String serverNames;
    static PrintWriter out;
    static int toDelete;
    static CalendarView view;
    
    public SendClient(String name, CalendarView view){
    this.name = name;
    this.view = view;
}
 /**
  * connect to the server
  * @param host 127.0.0.1
  * @param portString 1664 in this case
  * @throws IOException to calm down the compiler
  */   
    public void connect(String host, String portString) throws IOException {
        int port = Integer.parseInt(portString);
        Socket server = null;
        server = new Socket(host, port);
        this.out = new PrintWriter(server.getOutputStream());
        this.sc = new ServerConnection(server, toDelete, view);
 
        Thread t = new Thread(sc);
        t.start();
        sendMessageFromConsole(new String(MessageUtils.hello(name)));
    }
    
    /**
     * does not actually send message from console, just sends the message to the server
     * @param message the message to be sent
     * @throws IOException as always
     */
    
    public void sendMessageFromConsole(String message) throws IOException {
            if (sc.isActive()) {
                System.out.println("I am here!");
                System.out.println(message);
                this.out.println(message); 
                this.out.flush();
                return;
            }
        }
    }

