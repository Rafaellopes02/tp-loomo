package com.example.tp_loomo.utils

import com.example.tp_loomo.R

fun avatarDbValueToResource(value: String?): Any? = when (value) {
    "avatar1" -> R.drawable.avatar1
    "avatar2" -> R.drawable.avatar2
    "avatar3" -> R.drawable.avatar3
    "avatar4" -> R.drawable.avatar4
    "avatar5" -> R.drawable.avatar5
    "avatar6" -> R.drawable.avatar6
    null, "", "null" -> null
    else -> value
}