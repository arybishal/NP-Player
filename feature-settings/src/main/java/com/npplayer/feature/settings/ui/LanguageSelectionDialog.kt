package com.npplayer.feature.settings.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.npplayer.core.ui.theme.NPYellow

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String
)

val SupportedLanguages = listOf(
    LanguageItem("en", "English", "English (Default)"),
    LanguageItem("ne", "नेपाली", "Nepali"),
    LanguageItem("hi", "हिन्दी", "Hindi"),
    LanguageItem("es", "Español", "Spanish"),
    LanguageItem("fr", "Français", "French"),
    LanguageItem("de", "Deutsch", "German"),
    LanguageItem("ar", "العربية", "Arabic"),
    LanguageItem("bn", "বাংলা", "Bengali"),
    LanguageItem("ja", "日本語", "Japanese"),
    LanguageItem("zh", "简体中文", "Simplified Chinese")
)

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = NPYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "App Language",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SupportedLanguages) { lang ->
                    val isSelected = currentLanguageCode.equals(lang.code, ignoreCase = true)
                    Surface(
                        onClick = {
                            onSelectLanguage(lang.code)
                            try {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.code))
                            } catch (_: Exception) {}
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NPYellow.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) NPYellow else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onSelectLanguage(lang.code)
                                    try {
                                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.code))
                                    } catch (_: Exception) {}
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NPYellow,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.englishName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = NPYellow, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
