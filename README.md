# 🔍 Back2Me - Lost and Found Mobile Application

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Java-orange?style=for-the-badge&logo=java" alt="Language">
  <img src="https://img.shields.io/badge/Backend-Firebase-yellow?style=for-the-badge&logo=firebase" alt="Backend">
  <img src="https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge" alt="Status">
</p>

## 📖 Overview

**Back2Me** is a mobile application designed to help people report, search, and recover lost items. The app connects users who have lost belongings with those who have found them, creating a community-driven platform for reuniting people with their possessions.

## 🎯 Problem Statement

Every day, countless people lose personal belongings in universities, public spaces, and workplaces. Currently, there is no centralized, efficient system for reporting and finding lost items. People resort to scattered social media posts or bulletin boards, leading to low recovery rates and frustration.

**Back2Me** solves this by providing a centralized platform where users can:
- Post lost or found items with photos and locations
- Search for their lost belongings
- Claim items through a verification process
- Communicate directly with finders/owners

## ✨ Features

### 🔐 Authentication
- User registration with email and password
- Secure login with Firebase Authentication
- Forgot password functionality (email reset)
- User profile management

### 📦 Item Management
- Post **Lost** items with details and photos
- Post **Found** items to help others
- Upload images via camera or gallery
- Edit and delete own listings
- View item details with full information

### 🔍 Search & Discovery
- Search items by name, location, or description
- Browse recently posted items
- Filter by status (Lost/Found)
- View older posts

### ✋ Claims System
- Submit claims with verification message
- View all claims on your posted items
- Approve or reject claims
- Mark items as resolved

### 💬 Messaging
- Real-time in-app messaging
- Direct chat between users
- Conversation list with message preview
- Message timestamps

### 🎨 UI/UX
- **Light and Dark mode** support
- Material Design components
- Bottom navigation for easy access
- Loading indicators and empty states
- Green theme throughout the app

## 📱 Screenshots

| Home Screen | Add Item | Item Details |
|:-----------:|:--------:|:------------:|
| Browse all lost & found items | Post new lost/found item | View full item information |

| Messages | Chat | Settings |
|:--------:|:----:|:--------:|
| Conversation list | Real-time messaging | Dark mode toggle |

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| **Platform** | Android |
| **Language** | Java |
| **Min SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 34 (Android 14) |
| **Authentication** | Firebase Authentication |
| **Database** | Cloud Firestore |
| **Image Storage** | Cloudinary |
| **Image Loading** | Glide |
| **UI Framework** | Material Design Components |

## 📂 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/back2me/
│   │   ├── auth/
│   │   │   ├── LoginActivity.java
│   │   │   └── SignupActivity.java
│   │   ├── adapters/
│   │   │   ├── ItemsAdapter.java
│   │   │   ├── MessagesAdapter.java
│   │   │   └── ConversationsAdapter.java
│   │   ├── models/
│   │   │   ├── Item.java
│   │   │   ├── Message.java
│   │   │   ├── Conversation.java
│   │   │   ├── Claim.java
│   │   │   └── UserProfile.java
│   │   ├── repositories/
│   │   │   ├── ItemRepository.java
│   │   │   ├── ChatRepository.java
│   │   │   └── ClaimRepository.java
│   │   ├── MainActivity.java
│   │   ├── HomeFragment.java
│   │   ├── AddEditItemActivity.java
│   │   ├── ItemDetailActivity.java
│   │   ├── ChatActivity.java
│   │   ├── ConversationsActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── EditProfileActivity.java
│   │   ├── MyItemsActivity.java
│   │   └── Back2MeApplication.java
│   └── res/
│       ├── layout/          # XML layouts
│       ├── values/          # Colors, strings, themes (Light)
│       ├── values-night/    # Dark mode colors and themes
│       └── drawable/        # Icons and shapes
└── build.gradle
```

## 🗄️ Database Schema (Firestore)

### Users Collection
```
users/{userId}
├── displayName: String
├── email: String
├── photoUrl: String
└── createdAt: Timestamp
```

### Items Collection
```
items/{itemId}
├── name: String
├── description: String
├── location: String
├── status: String ("lost" | "found" | "resolved")
├── imageUrl: String
├── createdBy: String (userId)
├── ownerName: String
└── createdDate: String (ISO timestamp)
```

### Claims Collection
```
claims/{claimId}
├── itemId: String
├── claimerId: String
├── claimerName: String
├── message: String
├── status: String ("pending" | "approved" | "rejected")
└── createdAt: Timestamp
```

### Conversations Collection
```
conversations/{conversationId}
├── participants: Array<String>
├── participantNames: Map<String, String>
├── itemId: String
├── itemName: String
├── lastMessage: String
├── lastMessageTime: String
└── messages/ (subcollection)
    └── {messageId}
        ├── senderId: String
        ├── senderName: String
        ├── text: String
        └── timestamp: String
```

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android device/emulator (API 24+)
- Firebase account
- Cloudinary account

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Stuwvy/Final-MAD-Lost-and-Found-app.git
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned folder

3. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app with package name `com.example.back2me`
   - Download `google-services.json` and place it in the `app/` folder
   - Enable Email/Password authentication
   - Create Firestore database

4. **Configure Cloudinary**
   - Create account at [Cloudinary](https://cloudinary.com)
   - Update cloud name and upload preset in the app

5. **Build and Run**
   ```bash
   ./gradlew build
   ```
   Or click "Run" in Android Studio

## 📦 Dependencies

```gradle
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    
    // Cloudinary
    implementation 'com.cloudinary:cloudinary-android:2.3.1'
    
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    
    // CircleImageView
    implementation 'de.hdodenhof:circleimageview:3.1.0'
}
```

## 👥 Team Members

| Name | Role |
|------|------|
| Mai Soklyna | Team Lead / Developer |
| Roeun Monorom | Design / Add Items |
| Rin Monyroth | Test App and Report Error |
| Nov Singju | Test App and Report Error |
| Po Ratana | Documentation and Presentation |

## 📄 License

This project is developed for educational purposes as part of the Mobile Application Development course.

## 🙏 Acknowledgments

- [Firebase](https://firebase.google.com) - Backend services
- [Cloudinary](https://cloudinary.com) - Image storage
- [Material Design](https://material.io) - UI components
- [Glide](https://github.com/bumptech/glide) - Image loading library

---

<p align="center">
  Made with ❤️ by Back2Me Team
</p>
