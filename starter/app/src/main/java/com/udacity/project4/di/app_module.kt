package com.udacity.project4.di

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
    viewModel {
        RemindersListViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
    //Declare singleton definitions to be later injected using by inject()
    single {
        //This view model is declared singleton to be used across multiple fragments
        SaveReminderViewModel(
            get(),
            get() as ReminderDataSource
        )
    }
}
