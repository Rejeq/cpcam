package com.rejeq.cpcam.core.common.di

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.rejeq.cpcam.core.common.DndListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Retention(AnnotationRetention.BINARY)
@Qualifier
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
class CommonModule {
    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default,
    )

    @Provides
    fun provideDndListener(@ApplicationContext context: Context) = DndListener(
        context,
        notificationManager = NotificationManagerCompat.from(context),
    )
}
