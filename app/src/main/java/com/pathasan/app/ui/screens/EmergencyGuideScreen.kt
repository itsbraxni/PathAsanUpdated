package com.pathasan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Landslide
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.GuideStep
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed

@Composable
fun EmergencyGuideScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val fil = language == AppLanguage.FILIPINO
    var topic by remember { mutableStateOf("Earthquake") }
    val earthquake = if (fil) listOf(
        GuideStep("Bago", "Ihanda ang emergency kit, alamin ang ligtas na lugar, at ikabit ang mabibigat na gamit."),
        GuideStep("Habang lumilindol", "Dumapa, magtago, at kumapit. Lumayo sa bintana at huwag gumamit ng elevator."),
        GuideStep("Pagkatapos", "Asahan ang aftershock. Suriin ang pinsala at sundin ang opisyal na evacuation instruction."),
    ) else listOf(
        GuideStep("Before", "Prepare an emergency kit, identify safe places, and secure heavy furniture."),
        GuideStep("During", "Drop, cover, and hold on. Stay away from windows and do not use elevators."),
        GuideStep("After", "Expect aftershocks. Check for damage and follow official evacuation instructions."),
    )
    val landslide = if (fil) listOf(
        GuideStep("Bago", "Bantayan ang bitak sa lupa, pagtabingi ng puno, at biglang pagbabago ng daloy ng tubig."),
        GuideStep("Habang gumuguho", "Lumayo agad sa slide path at pumunta sa mas mataas at matatag na lupa."),
        GuideStep("Pagkatapos", "Huwag tumawid sa apektadong lugar. Hintayin ang clearance ng awtoridad."),
    ) else listOf(
        GuideStep("Before", "Watch for ground cracks, tilting trees, and sudden changes in water flow."),
        GuideStep("During", "Move immediately away from the slide path toward higher, stable ground."),
        GuideStep("After", "Do not cross the affected area. Wait for clearance from authorities."),
    )
    val steps = if (topic == "Earthquake") earthquake else landslide

    PathAsanScaffold(
        title = stringResource(R.string.emergency_guide),
        currentRoute = "guide",
        onBack = onBack,
        onNavigate = onNavigate,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = topic == "Earthquake",
                        onClick = { topic = "Earthquake" },
                        label = { Text(if (fil) "Lindol" else "Earthquake") },
                        leadingIcon = { Icon(Icons.Outlined.Warning, null) },
                    )
                    FilterChip(
                        selected = topic == "Landslide",
                        onClick = { topic = "Landslide" },
                        label = { Text(if (fil) "Pagguho" else "Landslide") },
                        leadingIcon = { Icon(Icons.Outlined.Landslide, null) },
                    )
                }
            }
            item {
                Text(
                    if (fil) "Alamin ang gagawin" else "Know what to do",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(if (fil) "Ang simpleng kilos ay makapagliligtas ng buhay." else "Simple actions can save lives.", color = PathAsanMuted)
            }
            itemsIndexed(steps) { index, step ->
                SectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${index + 1}", color = PathAsanRed, fontWeight = FontWeight.ExtraBold)
                        Column {
                            Text(step.title, fontWeight = FontWeight.ExtraBold)
                            Text(step.body, color = PathAsanMuted, modifier = Modifier.padding(top = 5.dp))
                        }
                    }
                }
            }
            item {
                SectionCard {
                    Text(if (fil) "Tandaan" else "Remember", color = PathAsanRed, fontWeight = FontWeight.ExtraBold)
                    Text(
                        if (fil) "Tulungan ang mga bata, nakatatanda, at persons with disabilities."
                        else "Assist children, older adults, and persons with disabilities.",
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
        }
    }
}
