package com.pathasan.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pathasan.app.R
import com.pathasan.app.ui.theme.PathAsanLine
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.ui.theme.PathAsanWhite

@Composable
fun PathAsanLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pathasan_logo),
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier,
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PathAsanRed),
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth().height(50.dp),
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().border(1.dp, PathAsanLine, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PathAsanWhite),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

data class BottomDestination(@StringRes val label: Int, val route: String, val icon: ImageVector)

val bottomDestinations = listOf(
    BottomDestination(R.string.home, "home", Icons.Outlined.Home),
    BottomDestination(R.string.routes, "routes", Icons.Outlined.Route),
    BottomDestination(R.string.offline, "offline_maps", Icons.Outlined.Map),
    BottomDestination(R.string.alerts, "alerts", Icons.Outlined.Notifications),
    BottomDestination(R.string.guide, "guide", Icons.Outlined.Book),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathAsanScaffold(
    title: String,
    currentRoute: String? = null,
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
    action: @Composable (() -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                        }
                    } else {
                        PathAsanLogo(Modifier.size(width = 76.dp, height = 46.dp))
                    }
                },
                actions = { action?.invoke() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PathAsanWhite),
            )
        },
        bottomBar = {
            if (currentRoute != null) {
                NavigationBar(containerColor = PathAsanWhite) {
                    bottomDestinations.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { if (currentRoute != item.route) onNavigate(item.route) },
                            icon = { Icon(item.icon, null) },
                            label = { Text(stringResource(item.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PathAsanRed,
                                selectedTextColor = PathAsanRed,
                                indicatorColor = Color(0xFFFDECEE),
                            ),
                        )
                    }
                }
            }
        },
        content = content,
    )
}

@Composable
fun DemoNotice(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .border(1.dp, PathAsanLine, RoundedCornerShape(10.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(stringResource(R.string.demo_data), color = PathAsanRed, fontWeight = FontWeight.ExtraBold)
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun FormError(message: String) {
    Spacer(Modifier.height(4.dp))
    Text(message, color = PathAsanRed, style = MaterialTheme.typography.bodySmall)
}
