package com.nevmem.survey.service.camera

import android.net.Uri
import com.nevmem.survey.ui.camera.CameraScreenListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class CameraDataListener(
    private val background: CoroutineScope,
) : CameraScreenListener {
    private val mutableUris = MutableSharedFlow<Uri>()
    val uris: Flow<Uri> = mutableUris

    override fun onNewCameraImage(uri: Uri) {
        background.launch {
            mutableUris.emit(uri)
        }
    }
}
