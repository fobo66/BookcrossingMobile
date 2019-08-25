package com.bookcrossing.mobile.util

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.util.Locale
import javax.inject.Inject

class LocaleProviderImpl @Inject constructor(
        private val resources: Resources
) : LocaleProvider {
    override val currentLocale: Locale
        get() = ConfigurationCompat.getLocales(resources.configuration)[0]
}