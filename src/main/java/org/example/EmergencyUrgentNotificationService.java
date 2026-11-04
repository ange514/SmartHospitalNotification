package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("emergencyService")
public class EmergencyUrgentNotificationService implements NotificationService {

    @Value("${hospital.name:General City Hospital}")
    private String hospitalName;

    @Override
    public void sendNotification(int patientId, String message) {
        System.out.println("EMERGENCY - ICU " + hospitalName + " CRITICAL alert for patient " + patientId + " -> " + message);
    }
}