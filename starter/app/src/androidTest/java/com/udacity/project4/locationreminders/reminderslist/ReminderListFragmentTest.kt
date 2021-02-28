package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val repository: FakeAndroidTestRepository by inject()
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeAndroidTestRepository
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as FakeAndroidTestRepository
                )
            }
            single { LocalDB.createRemindersDao(appContext) }
            single { FakeAndroidTestRepository() }

        }

        startKoin {
            modules(listOf(myModule))
        }

    }

    @After
    fun close() = runBlockingTest {
        repository.deleteAllReminders()
    }

    @Test
    fun displayReminderInUi() = runBlockingTest {
        // GIVEN - Add Reminder to the DB
        val reminder = ReminderDTO("test1", "description for test 1", "city 1", 17.97240, 87.30901)
        repository.saveReminder(reminder)
        // WHEN - Reminder List fragment launched to display reminder
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))
    }
    @Test
    fun navigationToSaveReminderTest(){
        // GIVEN - On the Reminder list screen
        val scenario =launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        // WHEN - Click on the FAB
        onView(withId(R.id.addReminderFAB)).perform(click())
        // THEN - Verify that we navigate to the save Reminder screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )

    }
    @Test
    fun noData_showNoDataTextToUser(){
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}