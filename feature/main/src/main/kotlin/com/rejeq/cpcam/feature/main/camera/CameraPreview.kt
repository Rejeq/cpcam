package com.rejeq.cpcam.feature.main.camera

import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import android.view.ViewGroup
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.ui.AutoFitSurfaceView

@Composable
fun CameraPreview(
    request: SurfaceRequestWrapper,
    modifier: Modifier = Modifier,
) {
    // HACK: Because of SurfaceView, some animations that perform anything
    // related to CameraPreview (transitions, scaling, ...) will be out of sync.
    // But if we draw something before SurfaceView, it will magically work.
    // If it's not gonna work on some devices:
    //  1) Replace SurfaceView to blurred image of last camera capture, and when
    //      animation ends - replace image back to SurfaceView
    //  2) Replace SurfaceView to TextureView, but this will reduce performance
    Surface(modifier = modifier) {
        val lifecycleHandler = remember { surfaceLifecycleHandler() }

        LaunchedEffect(request) {
            lifecycleHandler.setRequest(request)
        }

        // TODO: Use compose layout modifiers instead of custom
        //  AutoFitSurfaceView
        AndroidView(
            factory = { ctx ->
                AutoFitSurfaceView(ctx).apply {
                    // TODO: Determine resolution
                    setSize(Size(1920, 1080))

                    holder.addCallback(lifecycleHandler)
                    // TODO: Use deferred surface via
                    //  finalizeOutputConfigurations if available
                    //  https://developer.android.com/reference/android/hardware/camera2/CameraCaptureSession#finalizeOutputConfigurations(java.util.List%3Candroid.hardware.camera2.params.OutputConfiguration%3E)

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                }
            },
            modifier = Modifier.clipToBounds(),
        )
    }
}

private fun surfaceLifecycleHandler() = object : SurfaceHolder.Callback {
    private var isCreated = false
    private var request: SurfaceRequestWrapper? = null
    private var holder: SurfaceHolder? = null

    fun setRequest(request: SurfaceRequestWrapper) {
        if (request == this.request) {
            return
        }

        this.request?.invalidate()
        this.request = request

        val holder = this.holder
        if (isCreated && holder != null) {
            provideSurface(holder)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
        isCreated = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface Destroyed")

        isCreated = false
        this.holder = null
        request?.invalidate()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) {
        Log.d(TAG, "Surface size is changed to: $width, $height")

        provideSurface(holder)
        this.holder = holder
    }

    private fun provideSurface(holder: SurfaceHolder) {
        request?.provideSurface(holder.surface, Runnable::run) { result ->
            Log.i(TAG, "provideSurface result: $result")
        }
    }
}

private const val TAG = "CameraPreview"
