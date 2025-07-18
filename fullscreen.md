# Full Screen Toggle Feature Implementation for iOS

## Overview
This document describes how to implement the tap-to-toggle full screen functionality in the iOS README Viewer app, similar to the Android implementation.

## Feature Requirements

### User Experience
- **Normal Mode**: Tap anywhere on the markdown content area to enter full screen mode
- **Full Screen Mode**: Tap anywhere on the markdown content area to exit full screen mode
- **Alternative Controls**: Keep existing full screen button and close button as backup options

## iOS Implementation Guide

### 1. Update ContentView.swift

Add tap gesture recognition to the markdown content area:

```swift
// Add to ContentView.swift
import SwiftUI

struct ContentView: View {
    @State private var isFullScreen = false
    @State private var markdownContent = ""
    
    var body: some View {
        NavigationView {
            VStack {
                if !markdownContent.isEmpty {
                    // Markdown content with tap gesture
                    MarkdownView(content: markdownContent)
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                isFullScreen.toggle()
                            }
                        }
                        .fullScreenCover(isPresented: $isFullScreen) {
                            FullScreenMarkdownView(
                                content: markdownContent,
                                isPresented: $isFullScreen
                            )
                        }
                } else {
                    // Empty state
                    EmptyStateView()
                }
            }
            .navigationTitle("README Viewer")
        }
    }
}
```

### 2. Create FullScreenMarkdownView

Create a dedicated full screen view:

```swift
// Create new file: FullScreenMarkdownView.swift
import SwiftUI

struct FullScreenMarkdownView: View {
    let content: String
    @Binding var isPresented: Bool
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            // Full screen markdown content
            MarkdownView(content: content)
                .onTapGesture {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        isPresented = false
                    }
                }
            
            // Close button (top-right corner)
            VStack {
                HStack {
                    Spacer()
                    Button(action: {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            isPresented = false
                        }
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.6))
                            .clipShape(Circle())
                    }
                    .padding()
                }
                Spacer()
            }
        }
    }
}
```

### 3. Update MarkdownView Component

Ensure the MarkdownView supports tap gestures:

```swift
// Update MarkdownView.swift
import SwiftUI
import WebKit

struct MarkdownView: UIViewRepresentable {
    let content: String
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        webView.isUserInteractionEnabled = true
        webView.backgroundColor = UIColor.systemBackground
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        let htmlContent = convertMarkdownToHTML(content)
        webView.loadHTMLString(htmlContent, baseURL: nil)
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        // Handle web view navigation if needed
    }
    
    private func convertMarkdownToHTML(_ markdown: String) -> String {
        // Convert markdown to HTML
        // You can use a library like Down or implement your own converter
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-family: -apple-system, BlinkMacSystemFont, sans-serif;
                    padding: 16px;
                    line-height: 1.6;
                }
                pre { background-color: #f5f5f5; padding: 12px; border-radius: 8px; }
                code { background-color: #f5f5f5; padding: 2px 4px; border-radius: 4px; }
            </style>
        </head>
        <body>
            \(processMarkdown(markdown))
        </body>
        </html>
        """
    }
    
    private func processMarkdown(_ markdown: String) -> String {
        // Basic markdown processing
        // For production, use a proper markdown library
        return markdown
            .replacingOccurrences(of: "\n# ", with: "\n<h1>")
            .replacingOccurrences(of: "\n## ", with: "\n<h2>")
            .replacingOccurrences(of: "\n### ", with: "\n<h3>")
            .replacingOccurrences(of: "\n", with: "<br>")
    }
}
```

### 4. Add Animation and Transitions

Enhance the user experience with smooth transitions:

```swift
// Add to ContentView.swift
struct ContentView: View {
    @State private var isFullScreen = false
    @State private var markdownContent = ""
    
    var body: some View {
        NavigationView {
            VStack {
                if !markdownContent.isEmpty {
                    MarkdownView(content: markdownContent)
                        .onTapGesture {
                            // Add haptic feedback
                            let impactFeedback = UIImpactFeedbackGenerator(style: .medium)
                            impactFeedback.impactOccurred()
                            
                            withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
                                isFullScreen.toggle()
                            }
                        }
                        .fullScreenCover(isPresented: $isFullScreen) {
                            FullScreenMarkdownView(
                                content: markdownContent,
                                isPresented: $isFullScreen
                            )
                            .transition(.opacity.combined(with: .scale))
                        }
                }
            }
        }
    }
}
```

### 5. Handle Edge Cases

Add proper handling for edge cases:

```swift
// Add to FullScreenMarkdownView.swift
struct FullScreenMarkdownView: View {
    let content: String
    @Binding var isPresented: Bool
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            MarkdownView(content: content)
                .onTapGesture {
                    // Add haptic feedback
                    let impactFeedback = UIImpactFeedbackGenerator(style: .light)
                    impactFeedback.impactOccurred()
                    
                    withAnimation(.easeInOut(duration: 0.3)) {
                        isPresented = false
                    }
                }
                // Handle swipe down to dismiss
                .gesture(
                    DragGesture()
                        .onEnded { value in
                            if value.translation.y > 100 {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    isPresented = false
                                }
                            }
                        }
                )
            
            // Close button
            VStack {
                HStack {
                    Spacer()
                    Button(action: {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            isPresented = false
                        }
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundColor(.white)
                            .background(Color.black.opacity(0.6))
                            .clipShape(Circle())
                    }
                    .padding()
                }
                Spacer()
            }
        }
        .onAppear {
            // Hide status bar in full screen
            UIApplication.shared.isStatusBarHidden = true
        }
        .onDisappear {
            // Show status bar when exiting
            UIApplication.shared.isStatusBarHidden = false
        }
    }
}
```

## Implementation Steps

1. **Update ContentView**: Add tap gesture to markdown content area
2. **Create FullScreenMarkdownView**: Implement dedicated full screen view
3. **Add Animations**: Include smooth transitions and haptic feedback
4. **Handle Gestures**: Support both tap and swipe gestures
5. **Test Functionality**: Verify tap-to-toggle works in both modes

## Key Features

- ✅ Tap to enter full screen from normal mode
- ✅ Tap to exit full screen from full screen mode  
- ✅ Smooth animations and transitions
- ✅ Haptic feedback for better UX
- ✅ Swipe down to dismiss (bonus feature)
- ✅ Status bar management
- ✅ Fallback close button

## Testing Checklist

- [ ] Tap on markdown content enters full screen
- [ ] Tap on markdown content in full screen exits to normal
- [ ] Close button works as alternative
- [ ] Animations are smooth
- [ ] Haptic feedback works
- [ ] Status bar hides/shows correctly
- [ ] Works with different content sizes
- [ ] No conflicts with existing functionality

## Notes

- This implementation matches the Android version's behavior
- Uses SwiftUI's `fullScreenCover` for proper full screen presentation
- Includes additional iOS-specific features like haptic feedback and swipe gestures
- Maintains existing functionality while adding new tap-to-toggle feature