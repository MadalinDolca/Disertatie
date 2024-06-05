package com.madalin.disertatie.map.presentation.components.icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun rememberLineStartCircle(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "line_start_circle",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(13.333f, 27.292f)
                quadToRelative(3.042f, 0f, 5.167f, -2.125f)
                reflectiveQuadTo(20.625f, 20f)
                quadToRelative(0f, -3.042f, -2.125f, -5.167f)
                reflectiveQuadToRelative(-5.167f, -2.125f)
                quadToRelative(-3.041f, 0f, -5.166f, 2.125f)
                quadTo(6.042f, 16.958f, 6.042f, 20f)
                reflectiveQuadToRelative(2.125f, 5.167f)
                quadToRelative(2.125f, 2.125f, 5.166f, 2.125f)
                close()
                moveToRelative(0f, 2.625f)
                quadToRelative(-4.125f, 0f, -7.021f, -2.896f)
                quadTo(3.417f, 24.125f, 3.417f, 20f)
                reflectiveQuadToRelative(2.895f, -7.042f)
                quadToRelative(2.896f, -2.916f, 7.021f, -2.916f)
                quadToRelative(3.792f, 0f, 6.563f, 2.5f)
                reflectiveQuadToRelative(3.271f, 6.125f)
                horizontalLineToRelative(13.458f)
                verticalLineToRelative(2.625f)
                horizontalLineTo(23.167f)
                quadToRelative(-0.5f, 3.666f, -3.271f, 6.146f)
                quadToRelative(-2.771f, 2.479f, -6.563f, 2.479f)
                close()
                moveToRelative(0f, -9.917f)
                close()
            }
        }.build()
    }
}