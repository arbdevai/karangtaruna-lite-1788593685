package id.or.karangtaruna.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Central tokens for the dark, compact finance UI. */
object AppColors {
    val background = Color(0xFF0F1110)
    val surface = Color(0xFF151816)
    val elevated = Color(0xFF1B1F1C)
    val subtle = Color(0xFF202420)
    val textPrimary = Color(0xFFF4F5F3)
    val textSecondary = Color(0xFFA7ADA8)
    val textTertiary = Color(0xFF747B76)
    val accent = Color(0xFF6EC9A9)
    val accentStrong = Color(0xFF4FB58F)
    val positive = Color(0xFF3EB489)
    val negative = Color(0xFFFF7F7F)
    val divider = Color(0x14FFFFFF)
    val outline = Color(0x1FFFFFFF)
    val disabled = Color(0x47FFFFFF)
}

private val DarkColors = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = Color(0xFF06251A),
    primaryContainer = Color(0xFF173C2D),
    onPrimaryContainer = AppColors.accent,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.elevated,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.outline,
    error = AppColors.negative,
    errorContainer = Color(0xFF44201F),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val AppTypography = Typography().copy(
    displaySmall = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
)

@Composable fun KarangTarunaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, typography = AppTypography, content = content)
}
