
package com.example.readmeviewer.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun FeedbackScreen(onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var feedback by remember { mutableStateOf(TextFieldValue()) }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Send us your thoughts",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            label = { Text("Your feedback", color = MaterialTheme.colorScheme.onBackground) },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("kyawmyothant.dev@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "README Viewer Feedback")
                        putExtra(Intent.EXTRA_TEXT, feedback.text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
                    onSubmit(feedback.text)
                },
                enabled = feedback.text.isNotBlank()
            ) {
                Text("Submit")
            }
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        }
    }
}
