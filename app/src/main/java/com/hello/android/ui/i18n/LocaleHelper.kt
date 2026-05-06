package com.hello.android.ui.i18n

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("SYSTEM", "Follow System"),
    CHINESE("zh", "中文"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}

object LocaleHelper {

    fun setLocale(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.SYSTEM -> Locale.getDefault()
            AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
            AppLanguage.ENGLISH -> Locale.ENGLISH
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getCurrentLocale(context: Context): Locale {
        return context.resources.configuration.locales.get(0)
    }
}
