package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("smsService")
public class SmsNotificationService implements NotificationService {

    @Value("${hospital.name:General City Hospital}")
    private String hospitalName;

    @Override
    public void sendNotification(int patientId, String message) {
        System.out.println("SMS" + hospitalName + " To patient " + patientId + " -> " + message);
    }
}