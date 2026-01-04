# YDJob - Career & Recruitment Platform

![Language](https://img.shields.io/badge/Language-Kotlin-purple) ![Platform](https://img.shields.io/badge/Platform-Android-green) ![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue) ![Firebase](https://img.shields.io/badge/Backend-Firebase-orange)

**YDJob** is a modern Android application designed to bridge the gap between Job Seekers and Companies. Built with Kotlin and following the MVVM architecture, it provides a seamless experience for finding dream jobs and recruiting top talents.

---

## 📱 Features

The application supports two distinct user roles with specific functionalities:

### 🧑‍💼 For Job Seekers
* **Real-time Job Listing:** Browse recent and recommended jobs updated in real-time.
* **Smart Search:** Filter jobs by title, location, or category.
* **Easy Apply:** Apply for jobs instantly by attaching a CV/Resume.
* **Profile Management:** Manage personal details, skills, and experience.
* **Cloudinary Integration:** Securely upload and store PDF Resumes.

### 🏢 For Companies (Recruiters)
* **Job Posting:** Create and publish detailed job vacancies.
* **Applicant Management:** View a list of applicants for each job.
* **Applicant Profiling:** View detailed applicant profiles (Photo, Bio, Skills).
* **Resume Download:** Download applicant CVs (PDF) directly to the device.
* **Direct Contact:** Call or Email candidates directly from the app.

---

## 🛠 Tech Stack & Libraries

* **Language:** Kotlin
* **Minimum SDK:** API 27 (Android 8.1)
* **Target SDK:** API 36
* **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
* **UI:** XML Layouts, ViewBinding, Material Design Components
* **Backend (Firebase):**
    * Firebase Auth (Login/Register)
    * Firebase Firestore (NoSQL Database)
    * Firebase Storage (Image storage)
* **Media Management:**
    * **Cloudinary:** For handling Resume/CV uploads (PDF).
    * **Glide:** For efficient image loading and caching.
* **Asynchronous:** Kotlin Coroutines & LiveData.

---

## 🚀 Installation & Setup

To run this project locally, you need to configure Firebase and Cloudinary.

### 1. Clone the Repository
```bash
git clone [https://github.com/yourusername/YDJob.git](https://github.com/yourusername/YDJob.git)
