package com.ovais.batterymonitorer.feature.home.di

import com.ovais.batterymonitorer.feature.home.data.BatteryRepository
import com.ovais.batterymonitorer.feature.home.domain.DefaultBatteryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        impl: DefaultBatteryRepository
    ): BatteryRepository
}
