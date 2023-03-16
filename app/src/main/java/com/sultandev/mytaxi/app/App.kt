package com.sultandev.mytaxi.app

import android.app.Application
import com.sultandev.mytaxi.BuildConfig
import com.sultandev.mytaxi.di.dataModule
import com.sultandev.mytaxi.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            androidFileProperties()
            koin.loadModules(listOf(dataModule, viewModelModule))
        }


    }

    companion object {
        lateinit var instance: App
    }
}