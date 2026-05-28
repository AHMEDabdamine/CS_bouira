package com.example.i18n

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    private const val PREF_NAME = "locale_pref"
    private const val KEY_LANGUAGE = "selected_language"

    val SUPPORTED_LOCALES = listOf("en", "fr", "ar")

    fun onAttach(base: Context): Context {
        val lang = getPersistedLanguage(base)
        return updateBaseContext(base, lang)
    }

    fun setLanguage(context: Context, languageCode: String) {
        persistLanguage(context, languageCode)
        val activity = context as? Activity ?: return
        activity.recreate()
    }

    fun getPersistedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: "en"
    }

    private fun persistLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    private fun updateBaseContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
