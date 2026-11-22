package com.pathasan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pathasan.app.data.TextSize
import com.pathasan.app.navigation.AppNavigation
import com.pathasan.app.ui.theme.PathAsanRed
import com.pathasan.app.ui.theme.PathAsanTheme
import com.pathasan.app.viewmodel.AppViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val app = application as PathAsanApplication
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.factory(app))
            val state by appViewModel.uiState.collectAsStateWithLifecycle()
            val density = LocalDensity.current
            val fontScale = when (state.preferences.textSize) {
                TextSize.STANDARD -> 1f
                TextSize.LARGE -> 1.15f
                TextSize.EXTRA_LARGE -> 1.3f
            }

            PathAsanTheme {
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                    if (state.restored) {
                        AppNavigation(state, appViewModel)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PathAsanRed)
                        }
                    }
                }
            }
        }
    }
}
