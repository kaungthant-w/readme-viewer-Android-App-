package com.example.readmeviewer.data

import android.net.Uri

data class RecentFile(
    val uriString: String,
    val name: String,
    val lastAccessed: Long
) {
    val uri: Uri get() = Uri.parse(uriString)
    
    constructor(uri: Uri, name: String, lastAccessed: Long) : this(
        uri.toString(),
        name,
        lastAccessed
    )
}