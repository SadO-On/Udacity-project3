package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var localDataSource: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin()
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }
    @After
    fun closeDB() = database.close()

    @Test
    fun saveReminder_retrieveReminder() = runBlocking{
        // GIVEN - A new Reminder saved in the database.
        val newReminder = ReminderDTO("test1","description for test 1","My city",17.97240,87.30901)
        localDataSource.saveReminder(newReminder)
        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminder(newReminder.id)
        // THEN - Same Reminder is returned.
        assertThat(result.succeeded , `is`(true))
        result as Result.Success
        assertThat(result.data.title , `is`("test1"))
        assertThat(result.data.description , `is`("description for test 1"))
        assertThat(result.data.location , `is`("My city"))
        assertThat(result.data.latitude , `is`(17.97240))
        assertThat(result.data.longitude , `is`(87.30901))
        assertThat(result.data.id , `is`(newReminder.id))
    }
    @Test
    fun dataNotFound_retrieveNotFoundReminder() = runBlocking{
        // GIVEN - A new Reminder saved in the database.
        val newReminder = ReminderDTO("test1","description for test 1","My city",17.97240,87.30901)
        localDataSource.saveReminder(newReminder)
        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminder("DummyID")
        // THEN - Same Reminder is returned.
        assertThat(result.failed , `is`(true))
        result as Result.Error
        assertThat(result.message , not(nullValue()))

    }


}