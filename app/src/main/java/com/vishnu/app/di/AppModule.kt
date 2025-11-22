package com.vishnu.app.di

import com.vishnu.app.data.KtorWeatherApi
import com.vishnu.app.data.WeatherApi
import com.vishnu.app.data.WeatherRepositoryImpl
import com.vishnu.app.domain.GetWeatherUseCase
import com.vishnu.app.domain.WeatherRepository
import com.vishnu.app.ui.WeatherViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<WeatherApi> { KtorWeatherApi(get()) }
    single<WeatherRepository> { WeatherRepositoryImpl(get(), androidContext()) }
    single { GetWeatherUseCase(get()) }

    viewModel { WeatherViewModel(get(), androidApplication()) }
}
