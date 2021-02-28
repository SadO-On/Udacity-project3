package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest  {
    private lateinit var database: RemindersDatabase
    private lateinit var remindersDao : RemindersDao

    @Before
    fun initDb() {
        stopKoin()
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersDao = database.reminderDao()
    }

    @After
    fun closeDb() = database.close()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun insertReminderAndRetrievedById() = runBlockingTest {
        // GIVEN - Insert a Reminder.
        val reminder =ReminderDTO("test1", "description for test 1", "city 1", 17.97240, 87.30901)
        database.reminderDao().saveReminder(reminder)
        // WHEN - Get the Reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)
        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO , (not(nullValue())))
        assertThat(loaded.id ,`is`(reminder.id))
        assertThat(loaded.title , `is`(reminder.title))
        assertThat(loaded.description , `is`(reminder.description))
        assertThat(loaded.location , `is`(reminder.location))
        assertThat(loaded.latitude,`is`(reminder.latitude))
        assertThat(loaded.longitude,`is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders_testDataNotFoundScenarioWhenDeleteAll() = runBlockingTest{
        val reminder =ReminderDTO("test1", "description for test 1", "city 1", 17.97240, 87.30901)
        remindersDao.saveReminder(reminder)
        assertThat(remindersDao.getReminders(), hasItem(reminder))
        remindersDao.deleteAllReminders()
        assertThat(remindersDao.getReminders().isEmpty(), `is`(true))
    }

}