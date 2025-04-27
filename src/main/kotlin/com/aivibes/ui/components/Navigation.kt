package com.aivibes.ui.components

import kotlinx.html.*

fun SectioningOrFlowContent.navigationComponent() {
    nav(classes = "nav-bar") {
        a(classes = "nav-logo") { +"AI Vibe News" }
        div(classes = "nav-links") {
            a(classes = "nav-link") { +"Home" }
            a(classes = "nav-link") { +"Articles" }
            a(classes = "nav-link") { +"About" }
            a(classes = "nav-link") { +"Contact" }
        }
    }
} 