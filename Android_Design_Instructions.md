# Android Design Instructions for README Viewer App

## Overview
Convert the iOS README Viewer app to Android using modern Android development practices with Jetpack Compose for UI and Material Design 3 guidelines.

## Project Structure
```
app/
├── src/main/
│   ├── java/com/readmeviewer/
│   │   ├── MainActivity.kt
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   ├── components/
│   │   │   │   ├── MarkdownWebView.kt
│   │   │   │   ├── DocumentPicker.kt
│   │   │   │   └── SettingsDialog.kt
│   │   │   └── screens/
│   │   │       ├── HomeScreen.kt
│   │   │       ├── RecentScreen.kt
│   │   │       └── FullScreenScreen.kt
│   │   ├── data/
│   │   │   ├── RecentFilesManager.kt
│   │   │   └── PreferencesManager.kt
│   │   └── utils/
│   │       └── MarkdownProcessor.kt
│   └── res/
│       ├── values/
│       │   ├── colors.xml
│       │   ├── strings.xml
│       │   └── themes.xml
│       └── drawable/
└── build.gradle
```

## UI Design Guidelines

### 1. Material Design 3 Implementation
- Use Material 3 color system with dynamic theming support
- Implement proper elevation and shadows using Material components
- Follow Material Design typography scale
- Use Material icons from the official icon set

### 2. Navigation Structure
- **Bottom Navigation**: Replace iOS TabView with BottomNavigation
  - Home tab: House icon → `Icons.Default.Home`
  - Recent tab: Clock icon → `Icons.Default.History`
- **Top App Bar**: Replace iOS navigation header
  - Use `TopAppBar` with proper title and action buttons
  - Include overflow menu for additional actions

### 3. Screen Layouts

#### Home Screen Design
```kotlin
// Layout structure
Column {
    TopAppBar(
        title = { Text("README Viewer") },
        actions = {
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.TextFormat, "Font Size")
            }
            IconButton(onClick = { /* Dark Mode */ }) {
                Icon(if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode, "Theme")
            }
            if (hasContent) {
                IconButton(onClick = { /* Export PDF */ }) {
                    Icon(Icons.Default.Share, "Export")
                }
                IconButton(onClick = { /* Full Screen */ }) {
                    Icon(Icons.Default.Fullscreen, "Full Screen")
                }
            }
        }
    )
    
    // Content area
    if (markdownText.isEmpty()) {
        EmptyStateContent()
    } else {
        MarkdownWebView()
        FontSizeSlider()
    }
}
```

#### Empty State Design
- Use Material Design empty state patterns
- Center-aligned content with illustration
- Primary action button with Material styling
- Subtle secondary text

#### Recent Files Screen
- Use `LazyColumn` for file list
- Material `Card` components for each file item
- Swipe-to-delete functionality with confirmation
- Empty state when no recent files

### 4. Component Design Specifications

#### Buttons
- **Primary Actions**: Use `Button` with Material styling
- **Icon Buttons**: Use `IconButton` with proper touch targets (48dp minimum)
- **FAB**: Consider using FloatingActionButton for primary file selection

#### Cards and Lists
- Use `Card` with proper elevation for recent files
- Implement proper list item heights (minimum 48dp)
- Add ripple effects for touch feedback

#### Dialogs and Sheets
- **Settings**: Use `AlertDialog` or `BottomSheetDialog`
- **File Picker**: Integrate with Android's document picker
- **Share Sheet**: Use Android's native sharing intent

### 5. Typography and Spacing
```kotlin
// Typography scale
h1 = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
h2 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
body1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)

// Spacing system
val spacing = Spacing(
    xs = 4.dp,
    sm = 8.dp,
    md = 16.dp,
    lg = 24.dp,
    xl = 32.dp
)
```

### 6. Color Scheme
```kotlin
// Light theme colors
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

// Dark theme colors
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)
```

### 7. Responsive Design
- Support different screen sizes (phone, tablet, foldable)
- Use adaptive layouts with `WindowSizeClass`
- Implement proper landscape orientation support
- Consider split-screen and multi-window scenarios

### 8. Accessibility
- Ensure all interactive elements have content descriptions
- Support TalkBack screen reader
- Maintain proper color contrast ratios
- Implement semantic markup for markdown content
- Support dynamic text sizing

### 9. Animation and Transitions
- Use Material Motion principles
- Implement shared element transitions between screens
- Add subtle animations for state changes
- Use proper duration and easing curves

### 10. Status Bar and System UI
- Handle system bar colors appropriately
- Support edge-to-edge display
- Implement proper insets handling
- Consider gesture navigation compatibility

## Key Design Differences from iOS

1. **Navigation**: Bottom navigation instead of tab view
2. **App Bar**: Material top app bar instead of custom header
3. **Buttons**: Material button styling with proper elevation
4. **Typography**: Material Design type scale
5. **Colors**: Material 3 dynamic color system
6. **Spacing**: 8dp grid system
7. **Elevation**: Material elevation system instead of shadows
8. **Ripple Effects**: Android-specific touch feedback
9. **System Integration**: Android-specific sharing and file picking

## Implementation Priority
1. Basic navigation structure with bottom navigation
2. Home screen with empty state and content view
3. WebView integration for markdown rendering
4. Settings dialog implementation
5. Recent files screen with persistence
6. Full-screen mode
7. PDF export functionality
8. Dark theme implementation
9. Accessibility improvements
10. Performance optimizations