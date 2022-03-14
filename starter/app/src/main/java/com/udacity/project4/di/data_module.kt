package com.udacity.project4.di

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { RemindersLocalRepository(get()) as ReminderDataSource }

    single { LocalDB.createRemindersDao(androidContext()) }

}
