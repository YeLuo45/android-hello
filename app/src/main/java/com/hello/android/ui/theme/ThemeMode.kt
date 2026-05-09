package com.hello.android.ui.theme

/**
 * Theme mode options for the app.
 * Unifies theme mode selection across the app.
 */
enum class ThemeMode(val value: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2);

    companion object {
        fun fromValue(value: Int): ThemeMode = when (value) {
            0 -> LIGHT
            1 -> DARK
            2 -> SYSTEM
            else -> SYSTEM
        }
    }
}
