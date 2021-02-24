package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import android.util.Log
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource


private const val TAG = "geofenceBrodcast"

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage =
                    GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, applicationContext.getString(R.string.geofence_entered))
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                sendNotification(triggeringGeofences)
            } else {
                Log.e(TAG, applicationContext.getString(R.string.geofence_transition_invalid_type))
            }
        }

    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        if(triggeringGeofences.isEmpty()){
            Log.i(TAG,"Nothing found !!")
            return
        }
        val requestId = triggeringGeofences[0].requestId
        Log.i(TAG , requestId)
        Log.i(TAG , "send Notification hit")


        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
        //        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                Log.i(TAG , "before send Notification hit")
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
                Log.i(TAG , "after send Notification hit")

            }
        }
    }

}