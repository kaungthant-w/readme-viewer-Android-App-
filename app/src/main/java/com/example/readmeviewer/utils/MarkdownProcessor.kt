package com.example.readmeviewer.utils

class MarkdownProcessor {
    
    fun processMarkdown(
        markdownText: String,
        isDarkMode: Boolean,
        fontSize: Float
    ): String {
        // Simple markdown to HTML conversion for basic formatting
        var html = markdownText
            .replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<h1>$1</h1>")
            .replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<h2>$1</h2>")
            .replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<h3>$1</h3>")
            .replace(Regex("^#### (.+)$", RegexOption.MULTILINE), "<h4>$1</h4>")
            .replace(Regex("^##### (.+)$", RegexOption.MULTILINE), "<h5>$1</h5>")
            .replace(Regex("^###### (.+)$", RegexOption.MULTILINE), "<h6>$1</h6>")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
            .replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
            .replace(Regex("`(.+?)`"), "<code>$1</code>")
            .replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")
            .replace(Regex("^> (.+)$", RegexOption.MULTILINE), "<blockquote>$1</blockquote>")
            .replace(Regex("^- (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
            .replace(Regex("^\\* (.+)$", RegexOption.MULTILINE), "<li>$1</li>")
            .replace("\n", "<br>")
        
        // Wrap list items in ul tags
        html = html.replace(Regex("(<li>.+?</li>)(?!<li>)", RegexOption.DOT_MATCHES_ALL)) { matchResult ->
            val listItems = matchResult.value
            "<ul>$listItems</ul>"
        }
        
        return generateStyledHtml(html, isDarkMode, fontSize)
    }
    
    private fun generateStyledHtml(
        content: String,
        isDarkMode: Boolean,
        fontSize: Float
    ): String {
        val bgColor = if (isDarkMode) "#1e1e1e" else "#ffffff"
        val textColor = if (isDarkMode) "#d4d4d4" else "#000000"
        val codeBg = if (isDarkMode) "#2d2d2d" else "#f4f4f4"
        val linkColor = if (isDarkMode) "#9cdcfe" else "#0066cc"
        val borderColor = if (isDarkMode) "#444" else "#ddd"
        
        val css = """
            <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                background-color: $bgColor;
                color: $textColor;
                padding: 20px;
                line-height: 1.6;
                font-size: ${fontSize}px;
                margin: 0;
            }
            h1, h2, h3, h4, h5, h6 {
                margin-top: 1.5em;
                margin-bottom: 0.5em;
            }
            h1 { 
                font-size: 2em; 
                border-bottom: 1px solid $borderColor; 
                padding-bottom: 0.3em; 
            }
            h2 { font-size: 1.5em; }
            h3 { font-size: 1.25em; }
            h4 { font-size: 1.1em; }
            h5 { font-size: 1em; }
            h6 { font-size: 0.9em; }
            pre {
                background: $codeBg;
                padding: 16px;
                border-radius: 6px;
                overflow-x: auto;
                font-family: 'Courier New', monospace;
                font-size: ${fontSize * 0.9}px;
            }
            code {
                background: $codeBg;
                padding: 2px 4px;
                border-radius: 3px;
                font-family: 'Courier New', monospace;
                font-size: ${fontSize * 0.9}px;
            }
            a { 
                color: $linkColor; 
                text-decoration: none;
            }
            a:hover {
                text-decoration: underline;
            }
            blockquote {
                border-left: 4px solid $borderColor;
                padding-left: 16px;
                margin-left: 0;
                color: ${if (isDarkMode) "#aaa" else "#666"};
                font-style: italic;
            }
            ul {
                padding-left: 20px;
            }
            li {
                margin-bottom: 4px;
            }
            strong {
                font-weight: bold;
            }
            em {
                font-style: italic;
            }
            img {
                max-width: 100%;
                height: auto;
            }
            </style>
        """
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                $css
            </head>
            <body>
                $content
            </body>
            </html>
        """
    }
}