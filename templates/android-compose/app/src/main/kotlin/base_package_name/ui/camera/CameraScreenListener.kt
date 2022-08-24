package com.nevmem.survey.ui.camera

import android.net.Uri

interface CameraScreenListener {
    fun onNewCameraImage(uri: Uri)
}
