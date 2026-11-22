package com.pathasan.app.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.AppPreferences
import com.pathasan.app.data.TextSize
import com.pathasan.app.ui.components.DemoNotice
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.PrimaryButton
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onLanguageChange: (AppLanguage) -> Unit,
    onTextSizeChange: (TextSize) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val fil = preferences.language == AppLanguage.FILIPINO
    PathAsanScaffold(title = if (fil) "Mga Setting" else "Settings", onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (fil) "Wika" else "Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = preferences.language == language,
                        onClick = {
                            onLanguageChange(language)
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
                        },
                        label = { Text(if (language == AppLanguage.ENGLISH) "English" else "Filipino") },
                    )
                }
            }
            Text(if (fil) "Laki ng text" else "Text size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextSize.entries.forEach { size ->
                    FilterChip(
                        selected = preferences.textSize == size,
                        onClick = { onTextSizeChange(size) },
                        label = { Text(size.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            SectionCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (fil) "Mga Notification" else "Notifications", fontWeight = FontWeight.ExtraBold)
                        Text(
                            if (fil) "Preference at paghahanda lamang. Hindi pa konektado ang push notifications."
                            else "Preference and setup only. Push notifications are not connected.",
                            color = PathAsanMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = onNotificationsChange,
                    )
                }
            }
            SectionCard {
                Text(if (fil) "Tungkol sa PathAsan" else "About PathAsan", fontWeight = FontWeight.ExtraBold)
                Text("Version 1.0", color = PathAsanMuted, modifier = Modifier.padding(top = 6.dp))
                Text(
                    if (fil) "Native Android demonstration build para sa Baguio City disaster preparedness."
                    else "Native Android demonstration build for Baguio City disaster preparedness.",
                    color = PathAsanMuted,
                )
            }
            DemoNotice(
                if (fil) "Hindi kapalit ang app na ito ng opisyal na emergency guidance."
                else "This app is not a substitute for official emergency guidance."
            )
            PrimaryButton(if (fil) "Mag-log Out" else "Log Out", onLogout)
        }
    }
}
