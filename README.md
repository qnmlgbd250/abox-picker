# Abox Picker

Abox Picker 是一款简洁、高效的 Android 取件码管理工具。它通过解析短信内容，自动提取快递驿站的取件信息，帮助用户集中管理待取包裹。

## 🌟 核心功能

- **智能解析**：支持自定义正则表达式规则，根据短信关键词自动匹配并提取取件码。
- **本地存储**：基于 Room 数据库实现，所有数据保存在本地，保护隐私且离线可用。
- **状态管理**：清晰展示“待取件”和“已取件（归档）”记录。
- **高度定制**：用户可以根据不同驿站（如菜鸟驿站、丰巢等）的短信格式，灵活配置匹配规则和优先级。
- **现代 UI**：使用 Jetpack Compose 构建，拥有流畅的交互体验和现代感的视觉设计（如 Glassmorphism 风格）。

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **数据库**: Room (SQLite)
- **依赖注入/处理**: KSP (Kotlin Symbol Processing)
- **架构**: MVVM (Model-View-ViewModel)

## 🚀 快速开始

### 环境要求

- **Android Studio**: Jellyfish (2023.3.1) 或更高版本。
- **JDK**: Java 17 (推荐)。
- **Android SDK**: API Level 26 (Android 8.0) 及以上。

### 构建步骤

1. **克隆项目**:
   ```bash
   git clone git@github.com:qnmlgbd250/abox-picker.git
   ```
2. **在 Android Studio 中打开**:
   选择项目根目录，等待 Gradle 同步完成。
3. **运行/构建**:
   - 直接点击 IDE 中的 **Run** 按钮。
   - 或者使用命令行构建调试包：
     ```bash
     ./gradlew assembleDebug
     ```

## 📝 使用说明

由于各地区、各服务商的短信模板各异，项目**不内置**默认规则。
1. 进入应用设置或规则管理页面。
2. 添加新规则：
   - **识别关键词**: 如 `菜鸟, 驿站`（逗号分隔）。
   - **匹配正则**: 编写用于提取取件码的正则表达式。
3. 导入短信或接收新短信时，应用将自动根据规则进行解析。

## 📄 开源协议

本项目采用 [MIT License](LICENSE) (或根据您的需求修改)。
