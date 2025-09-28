package com.rejeq.cpcam.di

import com.rejeq.cpcam.core.common.MainActivityContract
import com.rejeq.cpcam.core.ui.PermissionStorage
import com.rejeq.cpcam.ui.MainActivityContractImpl
import com.rejeq.cpcam.ui.RootPermissionStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    @Binds
    fun bindMainActivityContract(
        impl: MainActivityContractImpl,
    ): MainActivityContract

    @Binds
    fun bindRootPermissionStorage(
        impl: RootPermissionStorage,
    ): PermissionStorage
}
