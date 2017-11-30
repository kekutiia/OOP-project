/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calendarfx.login;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author sveta
 */
public class MessageUtils {
       
    private MessageUtils() {
    }
/**
 * create a message of type hello where the first byte is 1
 * @param name string
 * @return  byte[] of the encoded message
 */
    public static byte[] hello(String name) {
        return getMessageBytes(MessageType.HELLO, name.getBytes());
    }
    /**
     * encode the message of type message with the first byte 2
     * @param name who to send to from the server
     * @param message the message to send
     * @return byte[] of the encoded message 
     */
     public static byte[] message(String name, String message) {
        return getMessageBytes(MessageType.MESSAGE, name.getBytes(),
                message.getBytes());
    }
    /**
     * encodes the message just like in the server
     * @param type the type of message one of MessageType
     * @param messages the message to send (can be mutiple, for example like in message above)
     * @return the prepared message to send 
     */
    private static byte[] getMessageBytes(MessageType type, byte[]... messages) {
        int messagesLength = 0;
        for (byte[] bytes : messages) {
            messagesLength += 4 + bytes.length;
        }
        // message-type (1 byte) + messages count (1 byte)
        // + messages counts * (message-length (4 byte) + message body)
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + messagesLength).order(
                ByteOrder.BIG_ENDIAN);
        buffer.put(type.getId());
        buffer.put((byte) messages.length);
        for (byte[] bytes : messages) {
            buffer.putInt(bytes.length).put(bytes);
        }
        return buffer.array();
    }

}
