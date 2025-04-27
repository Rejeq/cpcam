package com.rejeq.cpcam.di

import com.rejeq.cpcam.core.common.MainActivityContract
import com.rejeq.cpcam.ui.MainActivityContractImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideMainActivityContract(): MainActivityContract =
        MainActivityContractImpl()
}
