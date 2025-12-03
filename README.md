# Clinalert & DoctorTracker System

This project consists of two main components:
1.  **Clinalert (Flutter)**: A mobile application for patients to connect to a SmartWatch via BLE, view health data, and send it to their doctor.
2.  **DoctorTracker Backend (Spring Boot)**: A backend server to receive, store, and manage patient health data.

## Prerequisites

-   **Flutter SDK**: 3.x+
-   **Java JDK**: 17+
-   **Maven**: 3.x+
-   **PostgreSQL**: 14+ (or use Docker)

## 1. Backend Setup (DoctorTracker)

### Configuration
The backend configuration is located in `backend/doctortracker-backend/src/main/resources/application.yml`.
-   **Database**: Defaults to `jdbc:postgresql://localhost:5432/doctortracker`. Ensure you have a Postgres DB running with these credentials or update the file.
-   **Secrets**:
    -   `app.jwtSecret`: Secret key for JWT generation/verification.
    -   `app.hmacSecret`: Secret key for HMAC-SHA256 signature verification. **Must match the key used in the Flutter app.**

### Running the Backend
1.  Navigate to `backend/doctortracker-backend`.
2.  Run with Maven:
    ```bash
    mvn spring-boot:run
    ```
3.  The server will start on `http://localhost:8080`.

### Docker (Optional)
A `Dockerfile` is provided. You can build and run the backend container:
```bash
docker build -t doctortracker-backend .
docker run -p 8080:8080 doctortracker-backend
```

## 2. Mobile App Setup (Clinalert)

### Configuration
-   **BLE UUIDs**: Open `lib/services/ble_service.dart` and replace `_serviceUuid` and `_charUuid` with the actual UUIDs of your SmartWatch.
-   **Backend URL**: Open `lib/services/api_service.dart` and update `_baseUrl`.
    -   For Android Emulator: `http://10.0.2.2:8080/api`
    -   For iOS Simulator: `http://localhost:8080/api`
    -   For Physical Device: Use your computer's local IP (e.g., `http://192.168.1.X:8080/api`).
-   **Secrets**: The app uses `flutter_secure_storage` to store the HMAC secret. In a real production app, this should be securely provisioned. For testing, you can hardcode a default or provide a UI to enter it.

### Running the App
1.  Navigate to the root `clinalert` directory.
2.  Install dependencies:
    ```bash
    flutter pub get
    ```
3.  Run the app:
    ```bash
    flutter run
    ```

## Usage Flow

1.  **Login**: (Mocked in current version) Enter any credentials to access the dashboard.
2.  **BLE Scan**: Go to the Dashboard -> Tap "Scan Device".
3.  **Connect**: Select your device from the list.
4.  **Live Data**: View live measurements. Data is automatically saved locally.
5.  **Send to Doctor**: Go to Dashboard -> Tap "Send to Doctor". Select measurements and send.
    -   The app calculates an HMAC-SHA256 signature of the payload.
    -   The backend verifies this signature using the shared secret.

## Security Notes

-   **HMAC**: Ensures data integrity and authenticity. The payload is signed using a shared secret.
-   **Consent**: The app checks for user consent before reading BLE data (implemented in `MeasurementScreen`).
-   **Encryption**: Local data is encrypted using Hive's AES encryption.

## Testing HMAC (cURL Example)

Canonical JSON Payload:
```json
[{"id":"1","patientId":"p1","value":100.0,"type":"heart_rate"}]
```
Calculate HMAC-SHA256 of this string using the secret `mySuperSecretHmacKeyForIntegrityCheck123!`.

Send request:
```bash
curl -X POST http://localhost:8080/api/measurements \
  -H "Content-Type: application/json" \
  -H "X-Signature: <CALCULATED_HEX_SIGNATURE>" \
  -d '[{"id":"1","patientId":"p1","value":100.0,"type":"heart_rate"}]'
```
