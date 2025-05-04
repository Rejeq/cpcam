package com.rejeq.cpcam.core.camera.operation

import android.content.Context
import com.rejeq.cpcam.core.camera.source.CameraSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Base interface for camera operations that can be executed synchronously.
 */
interface CameraOperation<Ret> {
    operator fun CameraOpExecutor.invoke(): Ret
}

/**
 * Base interface for camera operations that can be executed asynchronously.
 */
interface AsyncCameraOperation<Ret> {
    suspend fun CameraOpExecutor.invoke(): Ret
}

/**
 * Context holder for all camera operations.
 *
 * For convenience you may want to use it with kotlin delegates, like:
 * ```
 * class MyClass(executor: CameraOpExecutor)
 *          : CameraOperationExecutor by executor {
 *     init {
 *         // So now you can use operations without specifying executor:
 *         CameraOperationImpl().invoke()
 *     }
 * }
 * ```
 */
interface CameraOpExecutor {
    val source: CameraSource
    val context: Context

    operator fun <Ret> CameraOperation<Ret>.invoke(): Ret
    suspend fun <Ret> AsyncCameraOperation<Ret>.invoke(): Ret
}

/**
 * Default executor for camera operations.
 */
class DefaultCameraOpExecutor @Inject constructor(
    @ApplicationContext override val context: Context,
    override val source: CameraSource,
) : CameraOpExecutor {
    override fun <Ret> CameraOperation<Ret>.invoke(): Ret =
        this@DefaultCameraOpExecutor.invoke()

    override suspend fun <Ret> AsyncCameraOperation<Ret>.invoke(): Ret =
        this@DefaultCameraOpExecutor.invoke()
}
