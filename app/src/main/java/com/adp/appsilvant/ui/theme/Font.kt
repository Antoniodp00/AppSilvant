package com.adp.appsilvant.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.adp.appsilvant.R

/**
 * Defines the custom Nunito font family.
 * This assumes you have a variable font file named 'nunitovariablefontwght.ttf' 
 * in your res/font folder, which can handle different weights.
 */
val Nunito = FontFamily(
    Font(R.font.nunitovariablefontwght, FontWeight.Normal),
    Font(R.font.nunitovariablefontwght, FontWeight.Bold)
)
