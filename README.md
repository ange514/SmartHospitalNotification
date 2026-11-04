# Smart Hospital Notification System

A small Spring Framework project that sends patient alerts through the right
channel: Email for routine reminders, SMS for follow-ups, and an Emergency
channel for critical ICU alerts. The goal is to show how Spring creates objects,
connects them, and chooses between several beans of the same type.

## 1. Core concepts used in this project

### Inversion of Control (IoC)

Normally a class creates the objects it needs with `new`. In this project, no
class does that. Spring creates every object (bean) and decides how they are
connected. The control over object creation is moved from my code to Spring.

### Dependency Injection (DI)

`PatientAlertProcessor` needs notification services to do its job. Instead of
building them itself, it lists them in its constructor and Spring passes them in.
Because the processor only depends on the `NotificationService` interface, I can
change or add a channel without touching the processor.

### The Application Context

`AnnotationConfigApplicationContext` is the Spring container. When the program
starts, it:

1. Reads `AppConfig` (the configuration class).
2. Scans the `org.example` package for classes marked `@Component`.
3. Loads the settings from `application.properties` and `.env`.
4. Creates each bean, fills in `@Value` settings, and injects dependencies.
5. Hands the ready-made `PatientAlertProcessor` to `main`.

## 2. The problem: three beans of the same type

`EmailNotificationService`, `SmsNotificationService` and
`EmergencyUrgentNotificationService` all implement `NotificationService`. When
`PatientAlertProcessor` asks for a `NotificationService`, Spring finds three
candidates and needs to be told which one to use:

| Alert | Channel | How Spring chooses it |
|---|---|---|
| Routine (appointment reminder) | `EmailNotificationService` | `@Primary`: used when no name is given |
| Emergency (ICU alert) | `EmergencyUrgentNotificationService` | `@Qualifier("emergencyService")` |
| Follow-up | `SmsNotificationService` | `@Qualifier("smsService")` |

`@Qualifier` always wins over `@Primary`, because asking by name is more specific
than the default.

## 3. Annotations used and why

| Annotation | Where | Purpose |
|---|---|---|
| `@Configuration` | `AppConfig` | Marks the class that holds Spring's setup |
| `@ComponentScan("org.example")` | `AppConfig` | Tells Spring which package to search for beans |
| `@PropertySource` | `AppConfig` | Loads `application.properties` and `.env` |
| `@Bean` | `AppConfig` | Registers the placeholder resolver that fills `${...}` values |
| `@Component` | All services and the processor | Makes the class a Spring-managed bean |
| `@Component("smsService")` | `SmsNotificationService` | Gives the bean a name for `@Qualifier` |
| `@Component("emergencyService")` | `EmergencyUrgentNotificationService` | Gives the bean a name for `@Qualifier` |
| `@Primary` | `EmailNotificationService` | Default bean when no qualifier is used |
| `@Autowired` | `PatientAlertProcessor` constructor | Constructor injection of all dependencies |
| `@Qualifier` | `PatientAlertProcessor` constructor | Picks a specific bean by name |
| `@Value` | Services and processor | Injects the hospital name and the API secret |

I used constructor injection because the processor cannot work without its
services, and it lets the fields be `final`.

## 4. Project structure

```
Spring/
├── .env                  # real values, NOT committed
├── .env.example          # template, committed
├── .gitignore
├── pom.xml
├── README.md
└── src/main/
    ├── java/org/example/
    │   ├── AppConfig.java
    │   ├── NotificationService.java
    │   ├── EmailNotificationService.java
    │   ├── SmsNotificationService.java
    │   ├── EmergencyUrgentNotificationService.java
    │   └── PatientAlertProcessor.java     # main class
    └── resources/
        └── application.properties
```

The only dependency is `spring-context` (version 7.0.8).

## 5. Keeping the secret safe

The hospital API secret is never written in the code.

- `application.properties` only contains placeholders:
  ```properties
  hospital.name=${HOSPITAL_NAME:General City Hospital}
  hospital.api.secret=${HOSPITAL_API_SECRET}
  ```
- The real values live in `.env`, which is listed in `.gitignore` and never pushed.
- `.env.example` shows reviewers which values are needed, with placeholders only.
- If `HOSPITAL_NAME` is missing, it falls back to `General City Hospital`.
- If `HOSPITAL_API_SECRET` is missing, the app refuses to start.

## 6. How to run it

**Requirements:** Java 26 and Maven (both available through IntelliJ IDEA).

1. Clone the repository and open it in IntelliJ IDEA.
2. Let Maven download `spring-context` (or click **Reload All Maven Projects**).
3. In the project root (next to `pom.xml`), copy the template:
   ```
   copy .env.example .env
   ```
4. Open `.env` and fill in your own values:
   ```
   HOSPITAL_API_SECRET=your_secret_key_here
   HOSPITAL_NAME=Your_Hospital_Name
   ```
5. Open `PatientAlertProcessor.java` and run its `main` method.

Instead of `.env`, the values can also be set as environment variables in
**Edit Configurations → Environment variables**.

## 7. Expected output

```
Hospital: Central_Medical_Center
Default channel   -> EmailNotificationService
Emergency channel -> EmergencyUrgentNotificationService
SMS channel       -> SmsNotificationService
[EMERGENCY - ICU] [Central_Medical_Center] CRITICAL alert for patient P-1001 -> Heart rate dropped below 40 bpm in ICU bed 3
[EMAIL] [Central_Medical_Center] To patient P-2045 -> Reminder: your appointment is tomorrow at 10:00
[SMS] [Central_Medical_Center] To patient P-3310 -> How are you feeling after your discharge? Reply 1-5
```

The line `Default channel -> EmailNotificationService` confirms that Spring chose
the `@Primary` bean when no `@Qualifier` was given.