package com.example.musicplayer

import android.content.Context

class HiddenManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("hidden_prefs", Context.MODE_PRIVATE)

    fun hide(id: Long) {
        val set = prefs.getStringSet("hidden", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(id.toString())
        prefs.edit().putStringSet("hidden", set).apply()
    }

    fun unhide(id: Long) {
        val set = prefs.getStringSet("hidden", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.remove(id.toString())
        prefs.edit().putStringSet("hidden", set).apply()
    }

    fun isHidden(id: Long): Boolean {
        val set = prefs.getStringSet("hidden", mutableSetOf()) ?: mutableSetOf()
        return set.contains(id.toString())
    }

    fun getAllHidden(): Set<Long> {
        val set = prefs.getStringSet("hidden", mutableSetOf()) ?: mutableSetOf()
        return set.mapNotNull { it.toLongOrNull() }.toSet()
    }
}
