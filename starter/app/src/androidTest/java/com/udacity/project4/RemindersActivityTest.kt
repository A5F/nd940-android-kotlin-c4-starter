package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application


    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


//    add End to End testing to the app

    @Test
    fun testReminderActivityWithNoReminder() {
        runBlocking { repository.deleteAllReminders() }

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.noDataTextView)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(
                        (getApplicationContext() as Application).getString(
                            R.string.no_data
                        )
                    )
                )
            )
        activityScenario.close()
    }

    @Test
    fun testDisplayDataOnReminderActivity() {
        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )
        runBlocking { repository.saveReminder(testData) }

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.withText(testData.title)))

        Espresso.onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.description)).check(ViewAssertions.matches(ViewMatchers.withText(testData.description)))

        Espresso.onView(withId(R.id.location))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.location))
            .check(ViewAssertions.matches(ViewMatchers.withText(testData.location)))
    }

    @Test
    fun testAddReminderAndSave() {
        runBlocking { repository.deleteAllReminders() }

        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        var decorView: View?= null
        activityScenario.onActivity {
            decorView = it.window.decorView
        }

        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.noDataTextView)).check(
                ViewAssertions.matches(
                    ViewMatchers.withText(
                        (getApplicationContext() as Application).getString(
                            R.string.no_data
                        )
                    )
                )
            )

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        val viewModel: SaveReminderViewModel = get()
        viewModel.reminderSelectedLocationStr.postValue(testData.location)
        viewModel.latitude.postValue(testData.latitude)
        viewModel.longitude.postValue(testData.longitude)
        viewModel.skipLocationForTesting = true

        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(testData.title))
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(testData.description))
        Espresso.closeSoftKeyboard()
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(appContext.getString(R.string.reminder_saved)))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(decorView)))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.title)).check(ViewAssertions.matches(ViewMatchers.withText(testData.title)))

        Espresso.onView(withId(R.id.description))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.description))
            .check(ViewAssertions.matches(ViewMatchers.withText(testData.description)))

        Espresso.onView(withId(R.id.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(withId(R.id.location)).check(ViewAssertions.matches(ViewMatchers.withText(testData.location)))
        activityScenario.close()
    }

    @Test
    fun testShowTitleErrorSnackbar() {

        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        val viewModel: SaveReminderViewModel = get()
        viewModel.reminderSelectedLocationStr.postValue(testData.location)
        viewModel.latitude.postValue(testData.latitude)
        viewModel.longitude.postValue(testData.longitude)
        viewModel.skipLocationForTesting = true

        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(testData.description))
        Espresso.closeSoftKeyboard()
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))
        activityScenario.close()
    }

    @Test
    fun testShowLocationErrorSnackbar() {

        val testData = ReminderDTO(
            "Title",
            "Description",
            "location",
            0.0, 0.0
        )

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        val viewModel: SaveReminderViewModel = get()
        viewModel.latitude.postValue(testData.latitude)
        viewModel.longitude.postValue(testData.longitude)
        viewModel.skipLocationForTesting = true

        Espresso.onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(testData.title))
        Espresso.onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(testData.description))
        Espresso.closeSoftKeyboard()
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_select_location)))
        activityScenario.close()
    }
}
