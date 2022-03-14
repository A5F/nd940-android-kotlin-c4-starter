package com.udacity.project4

import android.app.Application
import com.udacity.project4.di.appModule
import com.udacity.project4.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(appModule, dataModule))
        }
    }
}