package com.example.neatnest.di

import com.example.neatnest.data.local.AppDatabase
import com.example.neatnest.data.local.NeatNestPreferences
import com.example.neatnest.data.repository.FileRepository
import com.example.neatnest.data.repository.NotificationRepository
import com.example.neatnest.ui.assethub.AssetHubViewModel
import com.example.neatnest.ui.main.DashboardViewModel
import com.example.neatnest.ui.onboarding.OnboardingViewModel
import com.example.neatnest.ui.signalcleaner.SignalCleanerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// koin dependency graph
val appModule = module {

    // database and preferences
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().processedFileDao() }
    single { get<AppDatabase>().processedNotificationDao() }
    single { get<AppDatabase>().trackedFolderDao() }
    single { NeatNestPreferences(androidContext()) }

    // repositories
    single { FileRepository(get(), get(), get()) }
    single { NotificationRepository(get()) }

    // viewmodels
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { AssetHubViewModel(get(), get()) }
    viewModel { SignalCleanerViewModel(get()) }
}
