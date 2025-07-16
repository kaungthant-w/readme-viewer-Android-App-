package com.example.readmeviewer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsDialog(
    fontSize: Float,
    isDarkMode: Boolean,
    onFontSizeChange: (Float) -> Unit,
    onDarkModeToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Settings")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Font Size Slider
                Column {
                    Text(
                        text = "Font Size: ${fontSize.toInt()}px",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 10f..20f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Dark Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onDarkModeToggle() }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}