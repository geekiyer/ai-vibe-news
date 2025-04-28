package com.aivibes.ui.components

import kotlinx.html.SectioningOrFlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.nav

fun SectioningOrFlowContent.navigationComponent() {
    nav(classes = "nav-bar") {
        a(classes = "nav-logo") { +"vibeai.news" }
        div(classes = "nav-links") {
            a(classes = "nav-link") { +"Home" }
            a(classes = "nav-link") { +"Articles" }
            a(classes = "nav-link") { +"About" }
            a(classes = "nav-link") { +"Contact" }
        }
    }
} 