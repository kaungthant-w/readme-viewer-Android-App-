//

//  ContentView.swift

//  test

//

//  Created by Kyaw Myo Thant on 15/07/2025.

//

import SwiftUI

import UniformTypeIdentifiers

struct ContentView: View {

    @State private var markdownText: String = ""

    @State private var isDarkMode: Bool = false

    @State private var showPicker = false

    @State private var showSettings = false

    @State private var fontSize: CGFloat = 16 // Added font size state

    @State private var isFullScreen: Bool = false // Full screen toggle

    @State private var showShareSheet: Bool = false

    @State private var pdfData: Data? = nil

    @State private var exportPDFRequested: Bool = false

    @State private var recentFiles: [URL] = []

    @State private var selectedFileURL: URL? = nil

    @State private var selectedTab: Int = 0

    var body: some View {

        if isFullScreen && !markdownText.isEmpty {

            ZStack {

                (isDarkMode ? Color.black : Color.white)

                    .ignoresSafeArea()

                VStack(spacing: 0) {

                    // Full screen header with exit button

                    HStack {

                        Button(action: { isFullScreen = false }) {

                            Image(systemName: "arrow.down.right.and.arrow.up.left")

                                .font(.system(size: 20))

                                .padding(10)

                        }

                        Spacer()

                        Text("Full Screen Preview")

                            .font(.headline)

                        Spacer()

                    }

                    .padding()

                    .background(isDarkMode ? Color.black : Color.white)

                    .foregroundColor(isDarkMode ? .white : .black)

                    .shadow(color: Color.black.opacity(0.1), radius: 2, y: 2)

                    AnyView(MarkdownWebView(markdownText: markdownText, isDarkMode: isDarkMode, fontSize: fontSize, exportPDFRequested: $exportPDFRequested, onExportPDF: { data in

                        self.pdfData = data

                        self.showShareSheet = true

                    }))

                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                }

            }

        } else {

            TabView(selection: $selectedTab) {

                // Home Tab

                ZStack {

                    (isDarkMode ? Color.black : Color.white)

                        .ignoresSafeArea()

                    VStack(spacing: 0) {

                        // Header with controls

                        HStack {

                            if !markdownText.isEmpty {

                                Button(action: { markdownText = "" }) {

                                    Image(systemName: "arrow.left")

                                        .font(.system(size: 20))

                                        .padding(10)

                                }

                            }

                            Spacer()

                            Text(markdownText.isEmpty ? "Readme Viewer" : "Preview")

                                .font(.headline)

                            Spacer()

                            HStack(spacing: 16) {

                                Button(action: { showSettings.toggle() }) {

                                    Image(systemName: "textformat.size")

                                        .font(.system(size: 20))

                                        .padding(10)

                                }

                                Button(action: { isDarkMode.toggle() }) {

                                    Image(systemName: isDarkMode ? "sun.max.fill" : "moon.fill")

                                        .font(.system(size: 20))

                                        .padding(10)

                                }

                                // Export PDF button

                                if !markdownText.isEmpty {

                                    Button(action: { exportPDFRequested = true }) {

                                        Image(systemName: "square.and.arrow.up")

                                            .font(.system(size: 20))

                                            .padding(10)

                                    }

                                }

                                // Full screen button

                                if !markdownText.isEmpty {

                                    Button(action: { isFullScreen = true }) {

                                        Image(systemName: "arrow.up.left.and.arrow.down.right")

                                            .font(.system(size: 20))

                                            .padding(10)

                                    }

                                }

                            }

                        }

                        .padding()

                        .background(isDarkMode ? Color.black : Color.white)

                        .foregroundColor(isDarkMode ? .white : .black)

                        .shadow(color: Color.black.opacity(0.1), radius: 2, y: 2)

                        // Main content

                        if markdownText.isEmpty {

                            VStack(spacing: 20) {

                                Spacer()

                                Text("📄 No file selected")

                                    .font(.title2)

                                    .foregroundColor(.gray)

                                Text("Tap below to open a md file")

                                    .foregroundColor(.gray)

                                Button("📂 Choose .md File") {

                                    showPicker = true

                                }

                                .padding()

                                .background(Color.gray)

                                .foregroundColor(.white)

                                .cornerRadius(8)

                                Spacer()

                            }

                        } else {

                            AnyView(MarkdownWebView(markdownText: markdownText, isDarkMode: isDarkMode, fontSize: fontSize, exportPDFRequested: $exportPDFRequested, onExportPDF: { data in

                                self.pdfData = data

                                self.showShareSheet = true

                            }))

                            // Font size slider only when file is loaded

                            HStack {

                                Text("Font Size: ")

                                Slider(value: $fontSize, in: 12...32, step: 1) {

                                    Text("Font Size")

                                }

                                Text("\(Int(fontSize))")

                            }

                            .padding(.horizontal)

                        }

                    }

                }

                .tabItem {

                    Image(systemName: "house")

                    Text("Home")

                }

                .tag(0)

                // Recent Tab

                ZStack {

                    (isDarkMode ? Color.black : Color.white)

                        .ignoresSafeArea()

                    VStack(spacing: 0) {

                        HStack {

                            Spacer()

                            Text("Recent Files")

                                .font(.headline)

                            Spacer()

                        }

                        .padding()

                        .background(isDarkMode ? Color.black : Color.white)

                        .foregroundColor(isDarkMode ? .white : .black)

                        .shadow(color: Color.black.opacity(0.1), radius: 2, y: 2)

                        if recentFiles.isEmpty {

                            Spacer()

                            Text("No recent files.")

                                .foregroundColor(isDarkMode ? .white : .black)

                            Spacer()

                        } else {

                            List {

                                ForEach(recentFiles, id: \.self) { fileURL in

                                    Button(action: {

                                        loadMarkdownFile(from: fileURL)

                                        selectedTab = 0 // Switch to Home tab

                                    }) {

                                        Text(fileURL.lastPathComponent)

                                            .foregroundColor(isDarkMode ? .white : .black)

                                    }

                                }

                            }

                            .listStyle(InsetGroupedListStyle())

                            .background(isDarkMode ? Color.black : Color.white)

                            .environment(\ .colorScheme, isDarkMode ? .dark : .light)

                        }

                    }

                    .background(isDarkMode ? Color.black : Color.white)

                    .foregroundColor(isDarkMode ? .white : .black)

                }

                .tabItem {

                    Image(systemName: "clock")

                    Text("Recent")

                }

                .tag(1)

            }

            .sheet(isPresented: $showPicker) {

                DocumentPicker(markdownText: $markdownText, onPick: { url in

                    if let url = url, url.pathExtension.lowercased() == "md" {

                        addRecentFile(url)

                        selectedFileURL = url

                    } else {

                        // Clear markdownText and selectedFileURL if not md

                        markdownText = ""

                        selectedFileURL = nil

                    }

                })

            }

            .sheet(isPresented: $showSettings) {

                SettingsView(fontSize: $fontSize, isDarkMode: $isDarkMode)

            }

            .sheet(isPresented: $showShareSheet) {

                if let pdfData = pdfData {

                    ShareSheet(activityItems: [pdfData])

                }

            }

        }

    }

    // Add file to recent list

    private func addRecentFile(_ url: URL) {

        if !recentFiles.contains(url) {

            recentFiles.insert(url, at: 0)

            if recentFiles.count > 5 {

                recentFiles = Array(recentFiles.prefix(5))

            }

        } else {

            // Move to front

            recentFiles.removeAll { $0 == url }

            recentFiles.insert(url, at: 0)

        }

    }

    // Load markdown from file URL

    private func loadMarkdownFile(from url: URL) {

        // Only allow .md files

        guard url.pathExtension.lowercased() == "md" else { return }

        if let data = try? Data(contentsOf: url), let text = String(data: data, encoding: .utf8) {

            markdownText = text

            selectedFileURL = url

            addRecentFile(url)

        }

    }

}

#if canImport(UIKit)

import UIKit

struct ShareSheet: UIViewControllerRepresentable {

    var activityItems: [Any]

    var applicationActivities: [UIActivity]? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {

        UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)

    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}

}

#endif

#Preview {

    ContentView()

}