package com.avanseus.avr.broker;

/**
 * Created by hemanth on 18/5/16.
 */
public class MessageProtocol {
    public String handleProtocolMessage(String messageText) {
        String responseText;
        if ("MyProtocolMessage".equalsIgnoreCase(messageText)) {
            responseText = "I recognize your protocol message";
        } else {
            responseText = "Unknown protocol message: " + messageText;
        }

        return responseText;
    }
}
