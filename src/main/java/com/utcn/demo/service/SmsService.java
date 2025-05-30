package com.utcn.demo.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void send(String phoneNumber, String message) {
        // Placeholder implementation for sending SMS
        System.out.println("Simulating SMS to " + phoneNumber + ": " + message);
        // In a real application, you would integrate with an SMS gateway here
    }
}
