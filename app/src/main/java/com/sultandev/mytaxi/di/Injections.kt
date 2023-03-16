package com.sultandev.mytaxi.di

import android.content.Context
import androidx.room.Room
import com.sultandev.mytaxi.data.database.AppDatabase
import com.sultandev.mytaxi.data.database.dao.LocationDao
import com.sultandev.mytaxi.domain.repository.MapRepository
import com.sultandev.mytaxi.domain.repository.impl.MapRepositoryImpl
import com.sultandev.mytaxi.domain.usecase.MapUseCase
import com.sultandev.mytaxi.domain.usecase.impl.MapUseCaseImpl
import com.sultandev.mytaxi.presentation.map.impl.MapViewModelImpl
import com.sultandev.mytaxi.service.MapService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {

    fun provideAppDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "app_data")
            .fallbackToDestructiveMigration()
            .build()

    fun provideLocationDao(appDatabase: AppDatabase): LocationDao = appDatabase.locationDao()

    single { provideAppDatabase(androidContext()) }
    single { provideLocationDao(get()) }

    single<MapRepository> { MapRepositoryImpl(get()) }

    single<MapUseCase> { MapUseCaseImpl(get()) }
}

val viewModelModule = module {

    viewModel { MapViewModelImpl(get()) }

}
