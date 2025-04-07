package com.rejeq.cpcam.core.device.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.rejeq.cpcam.core.device.DndListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DeviceModule {
    @Provides
    fun provideDndListener(@ApplicationContext context: Context) = DndListener(
        context,
        notificationManager = NotificationManagerCompat.from(context),
    )
}
