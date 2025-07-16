# README Viewer - Android App

A modern Android application for viewing markdown files with a clean, Material Design 3 interface built with Jetpack Compose.

## Features

- 📱 **Modern UI** with Material Design 3 and Jetpack Compose
- 🌙 **Dark Mode** support with dynamic theming
- 📝 **Markdown Rendering** with proper formatting and syntax highlighting
- 📚 **Recent Files** management (stores last 5 opened files)
- 🔍 **Full Screen** viewing mode for distraction-free reading
- ⚙️ **Customizable Font Size** (12px - 24px range)
- 📤 **Share** functionality to share markdown content
- 🎨 **Responsive Design** supporting different screen sizes

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with:

- **Jetpack Compose** for modern declarative UI
- **StateFlow** for reactive state management
- **DataStore** for preferences persistence
- **Navigation Compose** for screen navigation
- **Material Design 3** components and theming

## Project Structure

```
app/src/main/java/com/example/readmeviewer/
├── MainActivity.kt                 # Main activity
├── ContentView.kt                 # Main navigation and bottom bar
├── data/
│   ├── RecentFile.kt             # Data model for recent files
│   ├── MainUiState.kt            # UI state data class
│   └── PreferencesManager.kt     # DataStore preferences management
├── repository/
│   └── FileRepository.kt         # File operations and data access
├── viewmodel/
│   └── MainViewModel.kt          # Business logic and state management
├── ui/
│   ├── components/
│   │   ├── MarkdownWebView.kt    # WebView for markdown rendering
│   │   └── SettingsDialog.kt     # Settings dialog component
│   ├── screens/
│   │   ├── HomeScreen.kt         # Main content viewing screen
│   │   ├── RecentScreen.kt       # Recent files list screen
│   │   └── FullScreenScreen.kt   # Full screen viewing mode
│   └── theme/                    # Material Design 3 theming
└── utils/
    └── MarkdownProcessor.kt      # Markdown to HTML conversion
```

## Key Components

### 1. Home Screen
- File selection using Android's document picker
- Markdown content rendering with WebView
- Font size adjustment slider
- Settings and theme toggle buttons
- Share functionality

### 2. Recent Files Screen
- List of recently opened files (last 5)
- File metadata display (name, last accessed date)
- Swipe-to-delete functionality
- Empty state when no recent files

### 3. Full Screen Mode
- Distraction-free reading experience
- Same markdown rendering as home screen
- Exit button to return to normal view

### 4. Settings
- Font size adjustment (12px - 24px)
- Dark mode toggle
- Persistent settings using DataStore

## Technical Implementation

### Markdown Processing
- Custom markdown processor for basic formatting
- Support for headers, bold, italic, code, links, lists, blockquotes
- Responsive HTML generation with CSS styling
- Dark/light theme aware styling

### State Management
- Centralized state in MainViewModel
- Reactive UI updates using StateFlow
- Proper lifecycle management
- Error handling and loading states

### File Operations
- Android Storage Access Framework integration
- Secure file access with proper permissions
- Recent files persistence using DataStore
- File metadata extraction

### UI/UX Features
- Material Design 3 components
- Bottom navigation for main sections
- Adaptive layouts for different screen sizes
- Proper accessibility support
- Smooth animations and transitions

## Dependencies

- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Latest Material Design components
- **Navigation Compose** - Type-safe navigation
- **DataStore** - Modern data storage solution
- **WebKit** - WebView for markdown rendering
- **Document File** - File operations support

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK API level 27 or higher
- Kotlin support

### Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

### Building the App

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Usage

1. Launch the app
2. Tap "Select File" to choose a markdown file from your device
3. View the rendered markdown content with proper formatting
4. Adjust font size using the slider at the bottom
5. Toggle dark mode using the theme button in the top bar
6. Access recently opened files from the "Recent" tab
7. Use full screen mode for distraction-free reading
8. Share markdown content using the share button

## Testing

The app includes a sample README file (`sample_readme.md`) that demonstrates various markdown features including:
- Headers and text formatting
- Code blocks and syntax highlighting
- Lists and tables
- Links and blockquotes
- Images and emphasis

## Future Enhancements

- PDF export functionality
- Advanced syntax highlighting for code blocks
- Table rendering improvements
- Image loading from markdown
- Search functionality within documents
- Bookmark/favorites system
- Custom themes and color schemes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License.