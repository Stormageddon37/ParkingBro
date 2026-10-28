package com.bresler.parkingbro.di

import com.bresler.parkingbro.data.AndroidLocationProvider
import com.bresler.parkingbro.data.DataStoreParkingStorage
import com.bresler.parkingbro.data.MediaStoreImageStore
import com.bresler.parkingbro.data.SystemClock
import com.bresler.parkingbro.domain.Clock
import com.bresler.parkingbro.domain.ImageStore
import com.bresler.parkingbro.domain.LocationProvider
import com.bresler.parkingbro.domain.ParkingStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds each domain interface to its Android implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProvidersModule {

    @Binds
    abstract fun bindLocationProvider(impl: AndroidLocationProvider): LocationProvider

    @Binds
    @Singleton
    abstract fun bindParkingStorage(impl: DataStoreParkingStorage): ParkingStorage

    @Binds
    abstract fun bindImageStore(impl: MediaStoreImageStore): ImageStore

    @Binds
    abstract fun bindClock(impl: SystemClock): Clock
}
