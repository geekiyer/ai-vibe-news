package com.aivibes.ui.components

import kotlinx.html.*

fun HTML.headerComponent() {
    head {
        title { +"AI Vibe News" }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
        link { 
            rel = "stylesheet"; 
            href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" 
        }
    }
} 