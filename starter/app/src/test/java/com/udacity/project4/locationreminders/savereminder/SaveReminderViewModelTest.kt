package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)

    }

    @Test
    fun saveReminder_retrievedSavedReminder()= mainCoroutineRule.runBlockingTest{
        //GIVEN - new Reminder data
        val reminder = ReminderDataItem("test1","description for test 1","My city",17.97240,87.30901)
        //WHEN - save reminder
        saveReminderViewModel.saveReminder(reminder)
        //THEN - Reminder Saved successfully
       val retrieved = fakeDataSource.getReminder(reminder.id)
         assertThat(retrieved , (not(nullValue())))
    }
    @Test
    fun check_loading_showLoadingValueChangedBeforeAndAfterSaveReminder(){
        //GIVEN-new Reminder data
        val reminder = ReminderDataItem("test1","description for test 1","My city",17.97240,87.30901)
        //WHEN - Save reminder show loading value changed to true before start
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue() , `is`(true))
        //THEN- showLoading value changed to false after finish
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue() , `is`(false))
    }

    @Test
    fun validateEnteredData_validateReminderTitleAndLocationShouldNotBeNull(){
        //GIVEN - new Reminder with null values
        val reminder = ReminderDataItem("","description for test 1",null,17.97240,87.30901)
        //WHEN - save reminder
        saveReminderViewModel.validateAndSaveReminder(reminder)
        //THEN - Show snackBar with appropriate message
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue() , (not(nullValue())))
    }


}