package com.rejeq.cpcam.core.camera.di

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import com.rejeq.cpcam.core.camera.operation.CameraOpExecutor
import com.rejeq.cpcam.core.camera.operation.DefaultCameraOpExecutor
import com.rejeq.cpcam.core.camera.source.CameraLifecycle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class CameraManagerService

@Retention(AnnotationRetention.BINARY)
@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class MainExecutor

@Module
@InstallIn(SingletonComponent::class)
internal object CameraModule {
    @Singleton
    @Provides
    @CameraManagerService
    fun provideCameraManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    @SuppressLint("RestrictedApi")
    @Provides
    @MainExecutor
    fun provideMainExecutor(): Executor = CameraXExecutors.mainThreadExecutor()

    @Provides
    @Singleton
    fun provideCameraLifecycle(
        @ApplicationContext context: Context,
        @MainExecutor executor: Executor,
    ) = CameraLifecycle(
        cameraProviderFuture = ProcessCameraProvider.getInstance(context),
        executor = executor,
    )

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        fun bindCameraOpExecutor(
            impl: DefaultCameraOpExecutor,
        ): CameraOpExecutor
    }
}
