package com.shieldstream.tv.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import com.shieldstream.tv.ui.R

val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

private fun TextStyle.withInter(weight: FontWeight = fontWeight ?: FontWeight.Normal) = copy(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSynthesis = FontSynthesis.None,
)

internal val AppTypography = with(Typography()) {
    Typography(
        displayLarge = displayLarge.withInter(FontWeight.SemiBold),
        displayMedium = displayMedium.withInter(FontWeight.SemiBold),
        displaySmall = displaySmall.withInter(FontWeight.SemiBold),
        headlineLarge = headlineLarge.withInter(FontWeight.SemiBold),
        headlineMedium = headlineMedium.withInter(FontWeight.SemiBold),
        headlineSmall = headlineSmall.withInter(FontWeight.SemiBold),
        titleLarge = titleLarge.withInter(FontWeight.SemiBold),
        titleMedium = titleMedium.withInter(),
        titleSmall = titleSmall.withInter(),
        bodyLarge = bodyLarge.withInter(),
        bodyMedium = bodyMedium.withInter(),
        bodySmall = bodySmall.withInter(),
        labelLarge = labelLarge.withInter(),
        labelMedium = labelMedium.withInter(),
        labelSmall = labelSmall.withInter(),
    )
}
