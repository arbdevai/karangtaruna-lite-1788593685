package id.or.karangtaruna.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF176B53), onPrimary = Color.White, primaryContainer = Color(0xFFD3F0E4), onPrimaryContainer = Color(0xFF002117),
    background = Color(0xFFF4F5F6), surface = Color.White, onBackground = Color(0xFF151515), onSurface = Color(0xFF151515),
    onSurfaceVariant = Color(0xFF737373), outline = Color(0xFFE5E5E5), error = Color(0xFFB5443D), errorContainer = Color(0xFFFFDAD6),
)
private val DarkColors = darkColorScheme(primary = Color(0xFF9BD4BD), onPrimary = Color(0xFF00382A), background = Color(0xFF121414), surface = Color(0xFF1B1D1D), onBackground = Color(0xFFE4E5E3), onSurface = Color(0xFFE4E5E3), onSurfaceVariant = Color(0xFFB9BDB9), outline = Color(0xFF3D4240), error = Color(0xFFFFB4AB))
private val AppTypography = Typography().let { it.copy(headlineSmall = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold), titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Bold), titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), bodyLarge = TextStyle(fontSize = 15.sp), labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)) }

@Composable fun KarangTarunaTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, typography = AppTypography, content = content) }
