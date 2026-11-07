package com.beenblock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.beenblock.app.ui.MainScreen
import com.beenblock.app.ui.MainViewModel
import com.beenblock.app.ui.theme.BeenBlockTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeenBlockTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
