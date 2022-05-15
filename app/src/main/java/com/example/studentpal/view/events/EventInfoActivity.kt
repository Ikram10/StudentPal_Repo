package com.example.studentpal.view.events

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityEventInfoBinding
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.messages.ChatLogActivity
import com.example.studentpal.viewmodel.EventInfoViewModel
import com.example.studentpal.viewmodel.MyProfileViewModel

/**
 * This activity is responsible for displaying the events information
 *
 * Users will be able to navigate to a map display or a chat log with the
 * event host
 *
 * [My Code ]: All code displayed in this class was created by the author
 *
 */
class EventInfoActivity : BaseActivity() {
    private var binding: ActivityEventInfoBinding? = null

    // ViewModel
    private lateinit var viewModel: EventInfoViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //initialises View Model
        viewModel = ViewModelProvider(this)[EventInfoViewModel::class.java]

        //Observes the host
        viewModel.host.observe(this) {
            // this method is called whenever the host value is modified
            populateEventInformation()
        }
        //catches the event document id sent from main activity
        if (intent.hasExtra(Constants.EVENT_DETAIL)) {
            viewModel.event = intent.getParcelableExtra(Constants.EVENT_DETAIL)!!
        }

        // Set the event host
        viewModel.event?.let { viewModel.setHost(it.creatorID) }

        viewModel.event?.let { setupActionBar(it.name) }

        populateEventInformation()

        // Location button clicked
        binding?.ibEventLocation?.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(Constants.EVENT_DETAIL, viewModel.event)
            startActivity(intent)
        }

        // Message host button clicked
        binding?.ibHostMessage?.setOnClickListener {
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(Constants.USER_KEY, viewModel.host.value)
            startActivity(intent)
        }

        // Share button clicked
        binding?.ibShare?.setOnClickListener {
            viewModel.shareEvent(this)
        }

        viewModel.createNotificationChannel(this)

        //Notification button clicked
        binding?.ibNotifyMe?.setOnClickListener {
            viewModel.scheduleNotification(this)

        }
    }

    /**
     * A method to populate the activities views, with event's information
     */
    private fun populateEventInformation() {
        binding?.ivEventInfo?.let {
            // loads the events image with glide
            Glide
                .with(this)
                .load(viewModel.event?.image)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(it)
        }
        // Sets the events host profile image with glide
        binding?.civEventHost.let {
            Glide
                .with(this)
                .load(viewModel.host.value?.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(it!!)

        }
        // populates the views with events informations
        binding?.tvEventInfoTime?.text = viewModel.event?.eventTime
        binding?.tvEventInfoDate?.text = viewModel.event?.let { viewModel.getCurrentDate(it.eventDate) }
        binding?.tvHostName?.text = viewModel.host.value?.name
        binding?.tvEventInfoName?.text = viewModel.event?.name
        binding?.tvEventLocation?.text = viewModel.event?.eventLocation
        binding?.tvEventDescription?.text = viewModel.event?.eventDescription

        //Only displays the message button to recipients of the event and not the host
        if (viewModel.host.value?.id  == getCurrentUserID()) {
            binding?.ibHostMessage?.visibility = View.GONE
        }
    }

    private fun setupActionBar(title: String) {
        setSupportActionBar(binding?.toolbarEventInfoActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = title
        }
        binding?.toolbarEventInfoActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }


}