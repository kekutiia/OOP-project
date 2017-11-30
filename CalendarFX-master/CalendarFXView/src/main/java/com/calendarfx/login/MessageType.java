/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calendarfx.login;

/**
 *Types of messages being sent. Encoded in the first byte
 *  message-type (1 byte) + messages count (1 byte
 *   + messages counts * (message-length (4 byte) + message body)
 * @author sveta
 */
public enum MessageType {
    HELLO(1),
    MESSAGE(2);

    private final byte id; 
/**
 * same as in server, id to denote the byte
 * @param id 
 */
    private MessageType(int id) {
        this.id = (byte) id;
    }
/**
 * get that id
 * @return the byte that is the id
 */
    public byte getId() {
        return id;
    }
}
