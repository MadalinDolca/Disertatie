package com.madalin.disertatie.camera_preview.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.madalin.disertatie.R
import com.madalin.disertatie.core.presentation.util.Dimens

@Composable
fun CameraPreviewScreen(
    onGoBackWithImage: (Bitmap) -> Unit,
    onGoBack: () -> Unit
) {
    val applicationContext = LocalContext.current.applicationContext
    val cameraController = remember {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            controller = cameraController,
            modifier = Modifier.fillMaxSize()
        )

        CameraControls(
            cameraController = cameraController,
            onGoBackWithImage = onGoBackWithImage,
            onGoBack = onGoBack
        )
    }
}

@Composable
private fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun CameraControls(
    cameraController: LifecycleCameraController,
    onGoBackWithImage: (Bitmap) -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .offset(y = -Dimens.container),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CloseCameraButton(onGoBack = { onGoBack() })

            TakePhotoButton(
                cameraController = cameraController,
                onGoBackWithImage = onGoBackWithImage
            )

            SwitchCameraButton(cameraController = cameraController)
        }
    }
}

@Composable
fun TakePhotoButton(
    cameraController: LifecycleCameraController,
    onGoBackWithImage: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPhotoBeingTaken by remember { mutableStateOf(false) }
    val errorMessage = stringResource(R.string.error_taking_photo)

    IconButton(
        onClick = {
            isPhotoBeingTaken = true
            takePhoto(
                cameraController = cameraController,
                applicationContext = context.applicationContext,
                onSuccess = {
                    isPhotoBeingTaken = false
                    onGoBackWithImage(it)
                },
                onFailure = {
                    isPhotoBeingTaken = false
                    Toast.makeText(context, "$errorMessage: $it", Toast.LENGTH_SHORT).show()
                }
            )
        },
        modifier = modifier.size(75.dp),
        enabled = !isPhotoBeingTaken,
        colors = IconButtonDefaults.filledIconButtonColors()
    ) {
        if (isPhotoBeingTaken) {
            CircularProgressIndicator()
        } else {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = "Take photo",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun CloseCameraButton(
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onGoBack() },
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors()
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Close camera"
        )
    }
}

@Composable
fun SwitchCameraButton(
    cameraController: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { switchCamera(cameraController) },
        modifier = modifier,
        colors = IconButtonDefaults.filledTonalIconButtonColors()
    ) {
        Icon(
            imageVector = Icons.Rounded.Cameraswitch,
            contentDescription = "Switch camera",
        )
    }
}

/**
 * Switches the camera between front and back.
 */
private fun switchCamera(cameraController: LifecycleCameraController) {
    cameraController.cameraSelector = if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
        CameraSelector.DEFAULT_FRONT_CAMERA
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }
}

/**
 * Takes a photo using this [cameraController] with the exact orientation.
 * @param onSuccess Called when the photo capture succeeds with the captured photo as a [Bitmap] parameter.
 * @param onFailure Called when the photo capture fails with the error message as a [String] parameter.
 */
private fun takePhoto(
    cameraController: LifecycleCameraController,
    applicationContext: Context,
    onSuccess: (Bitmap) -> Unit,
    onFailure: (String?) -> Unit
) {
    cameraController.takePicture(
        ContextCompat.getMainExecutor(applicationContext),
        object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                onSuccess(orientateImage(image, cameraController))
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                onFailure(exception.message)
            }
        }
    )
}

/**
 * Rotates the [image] to match the orientation of the device and removes the mirroring of the
 * [front camera][cameraController].
 */
private fun orientateImage(image: ImageProxy, cameraController: LifecycleCameraController): Bitmap {
    val matrix = if (cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
        Matrix().apply {
            postRotate(image.imageInfo.rotationDegrees.toFloat())
            postScale(-1f, 1f) // mirrors only on the X-axis
        }
    } else {
        Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
    }

    val rotatedBitmap = Bitmap.createBitmap(
        image.toBitmap(),
        0, 0,
        image.width, image.height,
        matrix, true
    )

    return rotatedBitmap
}