package com.bookcrossing.mobile.util

import android.content.Context
import javax.inject.Inject

class ResourceProviderImpl @Inject constructor(
        private val context: Context
) : ResourceProvider {
    override fun getString(stringRes: Int): String = context.getString(stringRes)
}