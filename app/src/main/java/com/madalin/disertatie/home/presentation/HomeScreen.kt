package com.madalin.disertatie.home.presentation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Button(onClick = { viewModel.logout() }) {
                Text(text = "logout")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Button(onClick = {
                viewModel.showDialog()
                Log.d("HomeScreen", "Button clicked")
            }) {
                Text(text = "Wtf")
            }
        }
    }
}

/*
@LightDarkPreview
@Composable
fun HomeScreenPreview() {
    DisertatieTheme {
        Surface {
            HomeScreen(
                viewModel = HomeViewModel(GlobalDriver(MockFirebaseUserRepositoryImpl)),
                onGoToLoginClick = {}
            )
        }
    }
}*/
