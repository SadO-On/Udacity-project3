package com.udacity.project4

import android.app.Application
import android.os.IBinder
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.bytebuddy.matcher.ElementMatchers.not
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

import  org.hamcrest.Matchers.not

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    class ToastMatcher : TypeSafeMatcher<Root?>() {
        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }

        override fun matchesSafely(item: Root?): Boolean {
            val type: Int = item?.windowLayoutParams?.get()!!.type
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                var windowToken: IBinder = item?.decorView.windowToken
                var appToken: IBinder = item.decorView.applicationWindowToken
                if (windowToken == appToken) {
                    return true
                }
            }
            return false
        }

    }

    @Test
    fun testAddingReminderProcess() = runBlocking {
        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click FAb to add new Reminder
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(click())
        // Fill the require filed
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(replaceText("NEW RIMINDER"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(replaceText("NEW RIMINDER DESC"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(replaceText("NEW RIMINDER DESC"))
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.map_fragment)).perform(longClick())
        delay(2000)
        Espresso.onView(ViewMatchers.withId(R.id.save_btn)).perform(click())
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(click())
        delay(2000)
        //TEST THE TOAST
        Espresso.onView(withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(not(`is`(getActivity(appContext)?.window?.decorView))))
            .check(matches(isDisplayed()))
//        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
//            .check(matches(withText(R.string.whatever_is_your_text)))

        // Verify reminder is displayed on screen in the reminder list.
        Espresso.onView(withText("NEW RIMINDER")).check(matches(isDisplayed()))
        // Make sure the activity is closed before resetting the db.
        activityScenario.close()

    }

}
