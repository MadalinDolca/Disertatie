package com.madalin.disertatie.core.domain

import android.app.Application
import com.madalin.disertatie.core.di.appModule
import com.madalin.disertatie.core.di.viewModelModule
import org.koin.core.context.startKoin

class DisertatieApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // initialize Koin with the defined modules
        startKoin {
            modules(appModule, viewModelModule)
        }
    }
}