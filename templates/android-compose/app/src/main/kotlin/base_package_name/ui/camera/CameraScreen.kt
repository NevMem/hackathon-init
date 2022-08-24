package com.nevmem.survey.ui.camera

import android.Manifest
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.nevmem.survey.R
import com.nevmem.survey.report.report
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalPermissionsApi
@Composable
private fun WithCameraPermission(
    child: @Composable () -> Unit,
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    when (val status = cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            child()
        }
        is PermissionStatus.Denied -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val textToShow = if (status.shouldShowRationale) {
                        "Приложению нужен доступ к камере, пожалуйста предоставьте его"
                    } else {
                        "Данная функция не будет работать без доступа к камере, пожалуйста предоставьте его"
                    }
                    Text(textToShow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                    }
                }
            }
        }
    }
}

sealed class CameraAction {
    object Capture : CameraAction()
}

@Composable
private fun CameraView(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
) {
    val context = LocalContext.current
    val lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder()
//            .setTargetResolution(android.util.Size(720, 1280))
            .build()
    }
    CameraPreviewView(
        imageCapture = imageCapture,
        lensFacing = lensFacing,
    ) { action ->
        when (action) {
            CameraAction.Capture -> {
                imageCapture.takePicture(context, lensFacing, onImageCaptured, onError)
            }
        }
    }
}

fun ImageCapture.takePicture(
    context: Context,
    lensFacing: Int,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val outputDirectory = context.getOutputDirectory()
    val photoFile = createFile(outputDirectory, "yyyy-MM-dd-HH-mm-ss-SSS", ".jpg")
    val outputFileOptions = getOutputFileOptions(lensFacing, photoFile)

    this.takePicture(
        outputFileOptions,
        Executors.newSingleThreadExecutor(),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(savedUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, _ ->
                }
                onImageCaptured(savedUri)
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

fun getOutputFileOptions(
    lensFacing: Int,
    photoFile: File
): ImageCapture.OutputFileOptions {

    val metadata = ImageCapture.Metadata().apply {
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }

    return ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()
}

fun createFile(baseFolder: File, format: String, extension: String) =
    File(
        baseFolder,
        SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )

fun Context.getOutputDirectory(): File {
    val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
        File(it, this.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else this.filesDir
}

@Composable
private fun CameraPreviewView(
    imageCapture: ImageCapture,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    onAction: (CameraAction) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val preview = Preview.Builder().build()
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    val previewView = remember { PreviewView(context) }
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize()) {
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(onClick = { onAction(CameraAction.Capture) }) {
                Text("Capture")
            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener(
            {
                continuation.resume(cameraProvider.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val surveyScreenViewModel by inject<CameraScreenListener>()
    WithCameraPermission {
        Box {
            CameraView(
                onImageCaptured = { uri ->
                    report("image-capturing", "done with uri $uri")
                    scope.launch {
                        surveyScreenViewModel.onNewCameraImage(uri)
                        navController.popBackStack()
                    }
                },
                onError = {
                    report("image-capturing", "error: $it")
                },
            )
        }
    }
}
