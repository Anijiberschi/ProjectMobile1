package com.example.projectmobile1;

public class ChatMessage {
    private String message;
    private String sender;

    public ChatMessage() {
        // Required empty constructor for Firebase
    }

    public ChatMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }
}
