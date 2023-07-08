package com.mustafamujawar.libstack.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class CardData(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val onClick: () -> Unit
)
