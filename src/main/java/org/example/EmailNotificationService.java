package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EmailNotificationService implements NotificationService {

    @Value("${hospital.name:General City Hospital}")
    private String hospitalName;

    @Override
    public void sendNotification(int patientId, String message) {
        System.out.println("EMAIL" + hospitalName + " To patient " + patientId + " -> " + message);
    }
}