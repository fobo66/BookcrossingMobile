package com.bookcrossing.mobile.util

import android.support.annotation.StringRes

interface ResourceProvider {
    fun getString(@StringRes stringRes: Int): String
}