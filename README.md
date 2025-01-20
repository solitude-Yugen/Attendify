# Attendify

Attendify is an automated attendance system that leverages image processing and face recognition technologies to streamline attendance management in educational institutions. By utilizing Python libraries and a Flask server for face recognition, Attendify ensures accurate, efficient, and real-time attendance tracking.

#  Key Features

- *Face Recognition*: Uses Python scripts with OpenCV, MTCNN, and FaceNet for recognizing faces from group photos.
- *Automated Attendance*: Attendance is automatically recorded and stored in the Firebase Realtime Database.
- *Editable Records*: Easily edit attendance records as needed.
- *Defaulter Lists*: Automatically generates a list of students with low attendance.
- *Attendance Tracking*: Track attendance percentages and generate daily/monthly reports.
- *Reports and Visualizations*: Provides bar and pie charts for attendance analytics.
- *Cross-Platform Compatibility*: The Android app is developed in Java with real-time updates powered by Firebase.

 # Getting Started

Follow these steps to set up and use Attendify on your local machine:

 # Prerequisites

1. *Python*: Install Python 3.8 or later.
2. *Flask*: Install Flask by running:
   ```bash
   pip install flask
   ```
3. *Android Studio*: Install Android Studio to run the Attendify app.
4. *Firebase*: Set up a Firebase project and configure the Realtime Database.

 # Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/attendify.git
   ```
2. Navigate to the project directory:
   ```bash
   cd attendify
   ```
3. Install required Python libraries:
   ```bash
   pip install -r requirements.txt
   ```
4. Update the IP address and port number in the Flask server script (`server.py`) to match your system's configuration:
   ```python
   app.run(host="<your_ip_address>", port=<your_port_number>)
   ```
5. Start the Flask server:
   ```bash
   python server.py
   ```

 # Running the Android App

1. Open the `Attendify` project in Android Studio.
2. Update the backend server URL in the app's source code to match your Flask server's IP address and port number.
3. Build and run the app on an Android device or emulator.

#  Project Structure

- *Python Scripts*: Handles face detection and recognition.
- *Flask Server*: Communicates with the Android app for face recognition.
- *Android App*: Provides a user-friendly interface for attendance management.
- *Firebase Realtime Database*: Stores attendance data.

#  System Requirements

- *Operating System*: Windows, macOS, or Linux
- *Python*: Version 3.8 or later
- *Android Device*: Android 5.0 (Lollipop) or later

#  Usage

1. Launch the Flask server to enable face recognition.
2. Open the Attendify app on your Android device.
3. Log in using the appropriate portal (staff or student).
4. Use the app to mark attendance, view reports, and manage records.

#  Notes

- Ensure that your system's IP address and port number are correctly updated in both the Flask server script and the Android app configuration.
- The face recognition functionality requires group photos with clear visibility of faces.

#  Contributing

We welcome contributions to enhance Attendify. Please fork the repository, make your changes, and submit a pull request.

 # Contact

For inquiries or support, please contact [vishruti.jadhav01@example.com].

---
Thank you for using Attendify! Together, let's make attendance management efficient and seamless.

