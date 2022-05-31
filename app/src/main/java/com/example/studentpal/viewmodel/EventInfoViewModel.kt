package com.example.studentpal.viewmodel

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentpal.common.utils.*
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.UsersDatabase.getEventHost
import com.example.studentpal.view.events.EventInfoActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class is responsible for executing [EventInfoActivity] business logic
 *
 * All code created by the author will be labelled with [My Code], whereas code that was used and adapted from
 * other sources is labelled with [Adapted ]
 */
class EventInfoViewModel: ViewModel() {
    //Event information
    var event: Event? = null
    // Event host
    var host = MutableLiveData<User>()
    /**
     * [My Code]: Sets the host variable by retrieving the host from the
     * database
     */
     fun setHost(creatorID: String) {
         //Coroutine to execute suspend functions
         viewModelScope.launch{
             host.value = getEventHost(creatorID)!!
         }
    }
    /**
     * [Adapted ]: Method creates the Android Sharesheet and prepares the content of the share
     */
    fun shareEvent(activity: EventInfoActivity) {
        //Developers Guide
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Event Name: ${event?.name}\n" +
                        "Description: ${event?.eventDescription}\n" +
                        "Location: ${event?.eventLocation}\n" +
                        "Time: ${event?.eventTime}\n" +
                        "Event Host: ${host.value?.name}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }

    /**
     * [Adapted ]: (CodeWithCal, 2021) Method creates the scheduled notification
     *
     * This code was adapted from a tutorial because it provided the code to enable users
     * to receive notifications for scheduled events
     * @see com.example.studentpal.common.References
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    fun scheduleNotification(activity: EventInfoActivity) {
        val intent = Intent(activity.applicationContext,
            ReminderBroadcast::class.java )
        val title = event?.name
        val message = "This event is scheduled for today"
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        val pendingIntent = PendingIntent.getBroadcast(
            activity.applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = activity
            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //[My Code] : stores the data of event
        val notifyEventTime = event?.eventDate
        if (notifyEventTime != null) {
            //notifies user on the day of the event
            alarmManager
                .setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notifyEventTime,
                pendingIntent
            )
        }
        showAlert(activity)
    }

    /**
     * [My Code]: Displays an alert dialog on the day of the event
     */
    private fun showAlert(activity: EventInfoActivity) {
        AlertDialog.Builder(activity)
            .setTitle("Event Scheduled")
            .setMessage(
                "Title: ${event?.name}\n\n" +
                        "Date: ${event?.let { getCurrentDate(it.eventDate) }}\n" +
                        "Time: ${event?.eventTime}")
            .setPositiveButton("Okay") { _: DialogInterface, _: Int ->
                Toast.makeText(activity, "Notification enabled for ${event?.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Method formats the date (Long) into a human readable string
     */
    fun getCurrentDate(long: Long): String {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        return formatter.format(long)
    }

    /**
     * This code was copied from a tutorial because it provided the code to enable users
     * to receive notifications. (CodeWithCal, 2021)
     *
     * @see com.example.studentpal.common.References
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(activity: EventInfoActivity) {
        val name = "Event Channel"
        val desc = "Event date and time has been scheduled"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = activity.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }




}