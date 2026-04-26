# Abox Picker

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A smart, lightweight Android app for managing delivery pickup codes. Automatically parses SMS messages and extracts pickup information from package stations.

[简体中文](README_zh.md)

## ✨ Features

- **Smart SMS Parsing**: Custom regex patterns to automatically extract pickup codes from delivery SMS
- **Local Storage**: Privacy-focused with offline-first design using Room database
- **Status Management**: Clear separation of pending and archived pickups
- **Flexible Rules**: Configure matching rules for different package station formats
- **Modern UI**: Built with Jetpack Compose for smooth interactions and glassmorphism design

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Database | Room (SQLite) |
| Dependency Injection | Hilt |
| Build System | Gradle |
| Architecture | MVVM |

## 📋 Requirements

- **Android Studio**: Jellyfish (2023.3.1) or later
- **JDK**: Java 17+
- **Android SDK**: API 26+ (Android 8.0+)

## 🚀 Quick Start

### Clone & Setup

```bash
# Clone repository
git clone https://github.com/qnmlgbd250/abox-picker.git
cd abox-picker

# Build with Gradle
./gradlew assembleDebug
```

### Run

1. Open project in Android Studio
2. Wait for Gradle sync to complete
3. Click **Run** (Alt + R) or build APK:
   ```bash
   ./gradlew assembleRelease
   ```

## 📖 Usage

1. **Open Settings** → Create a new rule
2. **Add Rule Details**:
   - **Keywords**: e.g., `菜鸟,驿站` (comma-separated)
   - **Regex Pattern**: Extract pickup codes matching your SMS format
3. **Receive SMS**: App automatically parses and categorizes messages

### Example Regex Pattern

```regex
取件码[：:]\s*(\S+)
```

This pattern extracts pickup codes in format: `取件码: ABC123`

## 📁 Project Structure

```
abox-picker/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/aboxpicker/
│   │   │       ├── ui/          # Compose UI screens
│   │   │       ├── data/        # Room entities & DAOs
│   │   │       ├── viewmodel/   # MVVM ViewModels
│   │   │       └── utils/       # Regex parsing utilities
│   │   └── res/
│   └── test/
├── build.gradle.kts
└── README.md
```

## 🔐 Privacy & Security

- All data stored locally on device
- No network requests or data collection
- No ads or tracking
- Full offline functionality

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📝 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

## 🐛 Issues & Support

Found a bug or have a suggestion? [Open an issue](https://github.com/qnmlgbd250/abox-picker/issues)

---

**Made with ❤️ by [qnmlgbd250](https://github.com/qnmlgbd250)**
