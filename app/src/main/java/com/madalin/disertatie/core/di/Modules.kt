package com.madalin.disertatie.core.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.madalin.disertatie.auth.data.FirebaseAuthRepositoryImpl
import com.madalin.disertatie.auth.domain.repository.FirebaseAuthRepository
import com.madalin.disertatie.auth.presentation.login.LoginViewModel
import com.madalin.disertatie.auth.presentation.password_reset.PasswordResetViewModel
import com.madalin.disertatie.auth.presentation.register.RegisterViewModel
import com.madalin.disertatie.core.data.FirebaseContentRepositoryImpl
import com.madalin.disertatie.core.data.FirebaseUserRepositoryImpl
import com.madalin.disertatie.core.domain.SuggestionGenerator
import com.madalin.disertatie.core.domain.repository.FirebaseContentRepository
import com.madalin.disertatie.core.domain.repository.FirebaseUserRepository
import com.madalin.disertatie.core.presentation.GlobalDriver
import com.madalin.disertatie.core.presentation.MainActivityViewModel
import com.madalin.disertatie.map.data.repository.WeatherRepositoryImpl
import com.madalin.disertatie.map.domain.repository.WeatherRepository
import com.madalin.disertatie.map.presentation.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin modules to define application components to be injected.
 */
val appModule = module {
    // firebase instances
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }

    // firebase repositories
    single<FirebaseAuthRepository> { FirebaseAuthRepositoryImpl() } // if FirebaseAuthRepository is used as a parameter
    single<FirebaseUserRepository> { FirebaseUserRepositoryImpl() }
    single<FirebaseContentRepository> { FirebaseContentRepositoryImpl(get(), get(), get()) }

    // other repositories
    single<WeatherRepository> { WeatherRepositoryImpl() }

    // other instances
    single { GlobalDriver(get()) }
    single { SuggestionGenerator() }
}

val viewModelModule = module {
    viewModel { MainActivityViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { PasswordResetViewModel(get()) }
    viewModel { MapViewModel(get(), get(), get(), get(), get()) }
}