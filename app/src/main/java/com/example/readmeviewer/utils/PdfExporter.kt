package com.example.readmeviewer.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfExporter(private val context: Context) {
    
    companion object {
        private const val TAG = "PdfExporter"
        private const val PAGE_WIDTH = 595  // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50
        private const val LINE_HEIGHT = 20
        private const val FONT_SIZE = 12f
    }
    
    suspend fun exportTextToPdf(
        textContent: String,
        fileName: String = "TextFile"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var pdfDocument: PdfDocument? = null
        var fileOutputStream: FileOutputStream? = null
        
        try {
            Log.d(TAG, "Starting text to PDF export...")
            
            // Validate input
            if (textContent.isBlank()) {
                return@withContext Result.failure(Exception("No content to export"))
            }
            
            // Create PDF file
            val pdfFile = File(context.cacheDir, "${fileName}_${System.currentTimeMillis()}.pdf")
            Log.d(TAG, "Creating PDF file: ${pdfFile.absolutePath}")
            
            // Create PDF document using Android's built-in PDF API
            pdfDocument = PdfDocument()
            
            // Process text and create pages (simpler processing for plain text)
            val lines = prepareTextLinesSimple(textContent)
            createPdfPages(pdfDocument, lines)
            
            // Write to file
            fileOutputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fileOutputStream)
            
            Log.d(TAG, "PDF created successfully, size: ${pdfFile.length()} bytes")
            
            // Get URI for the PDF file
            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            
            Result.success(pdfUri)
            
        } catch (e: Exception) {
            Log.e(TAG, "Text to PDF export failed: ${e.message}", e)
            Result.failure(Exception("Text to PDF Export Failed: ${e.message}"))
        } finally {
            // Clean up resources
            try {
                pdfDocument?.close()
                fileOutputStream?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Cleanup error: ${e.message}")
            }
        }
    }

    suspend fun exportMarkdownToPdfSimple(
        markdownText: String,
        fileName: String = "README"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var pdfDocument: PdfDocument? = null
        var fileOutputStream: FileOutputStream? = null
        
        try {
            Log.d(TAG, "Starting simple PDF export...")
            
            // Validate input
            if (markdownText.isBlank()) {
                return@withContext Result.failure(Exception("No content to export"))
            }
            
            // Create PDF file
            val pdfFile = File(context.cacheDir, "${fileName}_${System.currentTimeMillis()}.pdf")
            Log.d(TAG, "Creating PDF file: ${pdfFile.absolutePath}")
            
            // Create PDF document using Android's built-in PDF API
            pdfDocument = PdfDocument()
            
            // Process text and create pages
            val lines = prepareTextLines(markdownText)
            createPdfPages(pdfDocument, lines)
            
            // Write to file
            fileOutputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fileOutputStream)
            
            Log.d(TAG, "PDF created successfully, size: ${pdfFile.length()} bytes")
            
            // Get URI for the PDF file
            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            
            Result.success(pdfUri)
            
        } catch (e: Exception) {
            Log.e(TAG, "PDF export failed: ${e.message}", e)
            Result.failure(Exception("PDF Export Failed: ${e.message}"))
        } finally {
            // Clean up resources
            try {
                pdfDocument?.close()
                fileOutputStream?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Cleanup error: ${e.message}")
            }
        }
    }
    
    private fun prepareTextLinesSimple(textContent: String): List<String> {
        val lines = mutableListOf<String>()
        
        try {
            val inputLines = textContent.split("\n")
            
            for (line in inputLines) {
                // For plain text, just wrap long lines and add them as-is
                val wrappedLines = wrapText(line, 80)
                lines.addAll(wrappedLines)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error preparing simple text lines: ${e.message}")
            // Add fallback content
            lines.add("Content processing error occurred.")
            lines.add("Original content:")
            lines.add(textContent.take(1000))  // Limit to first 1000 chars
        }
        
        return lines
    }
    
    private fun prepareTextLines(markdownText: String): List<String> {
        val lines = mutableListOf<String>()
        
        try {
            val inputLines = markdownText.split("\n")
            
            for (line in inputLines) {
                val trimmedLine = line.trim()
                
                when {
                    // Headers - make them bold by adding prefix
                    trimmedLine.startsWith("# ") -> {
                        val text = trimmedLine.substring(2).trim()
                        lines.add("■ $text")  // Use symbol for header
                        lines.add("")  // Add space after header
                    }
                    
                    trimmedLine.startsWith("## ") -> {
                        val text = trimmedLine.substring(3).trim()
                        lines.add("▪ $text")  // Use symbol for subheader
                        lines.add("")
                    }
                    
                    trimmedLine.startsWith("### ") -> {
                        val text = trimmedLine.substring(4).trim()
                        lines.add("• $text")  // Use bullet for sub-subheader
                        lines.add("")
                    }
                    
                    // List items
                    trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                        val text = trimmedLine.substring(2).trim()
                        val processedText = processSimpleMarkdown(text)
                        lines.add("  • $processedText")
                    }
                    
                    // Blockquotes
                    trimmedLine.startsWith("> ") -> {
                        val text = trimmedLine.substring(2).trim()
                        val processedText = processSimpleMarkdown(text)
                        lines.add("  \" $processedText")
                    }
                    
                    // Regular paragraphs
                    trimmedLine.isNotEmpty() -> {
                        val processedText = processSimpleMarkdown(trimmedLine)
                        // Split long lines to fit page width
                        val wrappedLines = wrapText(processedText, 80)
                        lines.addAll(wrappedLines)
                        lines.add("")  // Add space after paragraph
                    }
                    
                    // Empty lines
                    else -> {
                        lines.add("")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error preparing text lines: ${e.message}")
            // Add fallback content
            lines.add("Content processing error occurred.")
            lines.add("Original content:")
            lines.add(markdownText.take(1000))  // Limit to first 1000 chars
        }
        
        return lines
    }
    
    private fun processSimpleMarkdown(text: String): String {
        return try {
            var processed = text
            
            // Remove markdown syntax (keep text only)
            processed = processed.replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")  // Bold
            processed = processed.replace(Regex("\\*(.+?)\\*"), "$1")        // Italic
            processed = processed.replace(Regex("`(.+?)`"), "[$1]")          // Code
            processed = processed.replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1") // Links
            
            processed
        } catch (e: Exception) {
            Log.w(TAG, "Error processing markdown: ${e.message}")
            text  // Return original if processing fails
        }
    }
    
    private fun wrapText(text: String, maxLength: Int): List<String> {
        return try {
            if (text.length <= maxLength) {
                listOf(text)
            } else {
                val words = text.split(" ")
                val lines = mutableListOf<String>()
                var currentLine = ""
                
                for (word in words) {
                    if ((currentLine + word).length <= maxLength) {
                        currentLine += if (currentLine.isEmpty()) word else " $word"
                    } else {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine)
                        }
                        currentLine = word
                    }
                }
                
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                
                lines
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error wrapping text: ${e.message}")
            listOf(text)  // Return original as single line if wrapping fails
        }
    }
    
    private fun createPdfPages(pdfDocument: PdfDocument, lines: List<String>) {
        try {
            var pageNumber = 1
            var currentLineIndex = 0
            
            while (currentLineIndex < lines.size) {
                // Create new page
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                
                // Set up paint for text
                val paint = Paint().apply {
                    textSize = FONT_SIZE
                    color = android.graphics.Color.BLACK
                    isAntiAlias = true
                }
                
                // Draw text on page
                var yPosition = MARGIN + LINE_HEIGHT
                var linesOnPage = 0
                val maxLinesPerPage = (PAGE_HEIGHT - 2 * MARGIN) / LINE_HEIGHT
                
                while (currentLineIndex < lines.size && linesOnPage < maxLinesPerPage) {
                    val line = lines[currentLineIndex]
                    
                    try {
                        // Draw the line
                        canvas.drawText(
                            line,
                            MARGIN.toFloat(),
                            yPosition.toFloat(),
                            paint
                        )
                        
                        yPosition += LINE_HEIGHT
                        linesOnPage++
                        currentLineIndex++
                        
                    } catch (e: Exception) {
                        Log.w(TAG, "Error drawing line: $line, error: ${e.message}")
                        currentLineIndex++  // Skip problematic line
                    }
                }
                
                // Add page number
                try {
                    val pageText = "Page $pageNumber"
                    canvas.drawText(
                        pageText,
                        (PAGE_WIDTH - MARGIN).toFloat(),
                        (PAGE_HEIGHT - MARGIN / 2).toFloat(),
                        paint
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error drawing page number: ${e.message}")
                }
                
                // Finish page
                pdfDocument.finishPage(page)
                pageNumber++
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PDF pages: ${e.message}")
            // Create a simple error page
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    textSize = FONT_SIZE
                    color = android.graphics.Color.BLACK
                }
                
                canvas.drawText("PDF generation error occurred.", MARGIN.toFloat(), (MARGIN + LINE_HEIGHT).toFloat(), paint)
                canvas.drawText("Please try again.", MARGIN.toFloat(), (MARGIN + LINE_HEIGHT * 2).toFloat(), paint)
                
                pdfDocument.finishPage(page)
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Even fallback page creation failed: ${fallbackError.message}")
            }
        }
    }
}