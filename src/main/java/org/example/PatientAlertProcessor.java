package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PatientAlertProcessor {

    private final NotificationService defaultService;
    private final NotificationService emergencyService;
    private final NotificationService smsService;

    private final String hospitalName;
    private final String hospitalApiSecret;

    @Autowired
    public PatientAlertProcessor(
            NotificationService defaultService,
            @Qualifier("emergencyService") NotificationService emergencyService,
            @Qualifier("smsService") NotificationService smsService,
            @Value("${hospital.name:General City Hospital}") String hospitalName,
            @Value("${HOSPITAL_API_SECRET}") String hospitalApiSecret) {

        this.defaultService = defaultService;
        this.emergencyService = emergencyService;
        this.smsService = smsService;
        this.hospitalName = hospitalName;
        this.hospitalApiSecret = hospitalApiSecret;

        System.out.println("Hospital: " + hospitalName);
        System.out.println("Default channel  :  " + defaultService.getClass().getSimpleName());
        System.out.println("Emergency channel : " + emergencyService.getClass().getSimpleName());
        System.out.println("SMS channel : " + smsService.getClass().getSimpleName());
    }

    public void sendRoutineAlert(int patientId, String message) {
        defaultService.sendNotification(patientId, message);
    }

    public void sendEmergencyAlert(int patientId, String message) {
        emergencyService.sendNotification(patientId, message);
    }

    public void sendSmsAlert(int patientId, String message) {
        smsService.sendNotification(patientId, message);
    }

    // Entry point: starts the Spring IoC container
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        // Ask Spring for this class as a bean (fully wired)
        PatientAlertProcessor processor = context.getBean(PatientAlertProcessor.class);

        processor.sendEmergencyAlert(1001, "Heart rate dropped below 40 bpm in ICU bed 3");
        processor.sendRoutineAlert(2045, "Reminder: your appointment is tomorrow at 10:00");
        processor.sendSmsAlert(3310, "How are you feeling after your discharge? Reply 1-5");

        context.close();
    }
}