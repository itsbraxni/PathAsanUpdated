package com.pathasan.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.EmergencyContact
import com.pathasan.app.data.EmergencyContacts
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.SectionCard
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed

@Composable
fun EmergencyContactsScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val fil = language == AppLanguage.FILIPINO
    val context = LocalContext.current

    PathAsanScaffold(
        title = if (fil) "Mga Emergency Contact" else "Emergency Contacts",
        currentRoute = "contacts",
        onBack = onBack,
        onNavigate = onNavigate,
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(EmergencyContacts.verified, key = { it.nameEnglish }) { contact ->
                EmergencyContactRow(
                    contact = contact,
                    fil = fil,
                    onCall = { number ->
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}"))
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun EmergencyContactRow(
    contact: EmergencyContact,
    fil: Boolean,
    onCall: (String) -> Unit,
) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (fil) contact.nameFilipino else contact.nameEnglish,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    contact.phoneNumber
                        ?: if (fil) "Walang available na contact information"
                        else "Contact information unavailable",
                    color = if (contact.phoneNumber == null) PathAsanMuted else PathAsanRed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            contact.phoneNumber?.let { number ->
                Button(
                    onClick = { onCall(number) },
                    colors = ButtonDefaults.buttonColors(containerColor = PathAsanRed),
                ) {
                    Icon(Icons.Outlined.Call, null)
                    Text(if (fil) "Tumawag" else "Call", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}
