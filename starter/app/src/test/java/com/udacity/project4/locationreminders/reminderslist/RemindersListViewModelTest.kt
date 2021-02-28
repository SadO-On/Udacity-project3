package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

/**
 * I think you're in the right truck but there's some point you need to remember:
 * You need a mainCoroutine rule file so you can to change values. [Done]
 * You need to check if a error returned or the loading is appeared in every method
 * Try to cover many cases
 * use stop koin in the setupViewModel
 * This answer will help you back to it whenever you fell lost https://knowledge.udacity.com/questions/497437
 * Also here are some test cases you should write it:
 * 1-loadReminder - main case with loading
 * 2-loadReminder - error case with loading
 */


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        var reminders: MutableList<ReminderDTO> = mutableListOf(
            ReminderDTO("test1", "description for test 1", "city 1", 17.97240, 87.30901),
            ReminderDTO("test2", "description for test 2", "city 2", -24.85644, 44.89240),
            ReminderDTO("test3", "description for test 3", "city 3", 35.14860, -104.40766)
        )
        fakeDataSource = FakeDataSource(reminders)
        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun check_loading_showLoadingValueChangedBeforeAndAfterLoadReminders() {
        //WHEN - load reminders show loading value changed to true before start
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        Assert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        //THEN- showLoading value changed to false after finish
        mainCoroutineRule.resumeDispatcher()
        Assert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }
    @Test
    fun shouldReturnError_showingErrorIfSomethingWentWrongWhenLoadReminders(){
        //GIVEN - change error value in FakeRepository
        fakeDataSource.setReturnError(true)
        //WHEN - try to load Reminders
        reminderListViewModel.loadReminders()
        //THEN - Error message appeared
        assertThat(reminderListViewModel.showSnackBar.value , (not(nullValue())))
    }

    @Test
    fun invalidateShowNoData_showNoDataIfTheReminderListEmpty()= mainCoroutineRule.runBlockingTest{
        //GIVEN - empty the data source
        fakeDataSource.deleteAllReminders()
        //WHEN - call load reminder
        reminderListViewModel.loadReminders()
        //THEN - showNoData message to user
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue() , `is`(true))
    }


}