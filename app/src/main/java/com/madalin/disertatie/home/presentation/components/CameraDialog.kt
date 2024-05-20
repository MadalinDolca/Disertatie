package com.madalin.disertatie.home.presentation.components

import androidx.annotation.OptIn
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalVideo::class)
@Composable
fun CameraDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val applicationContext = LocalContext.current.applicationContext
    val controller = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.clip(MaterialTheme.shapes.medium)) {
            CameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}