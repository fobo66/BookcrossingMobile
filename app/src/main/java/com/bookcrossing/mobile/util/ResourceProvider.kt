package com.bookcrossing.mobile.util

import androidx.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes stringRes: Int): String
}