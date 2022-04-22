package com.example.studentpal.activities.events

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.activities.messages.ChatLogActivity
import com.example.studentpal.databinding.ActivityEventInfoBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class EventInfoActivity : BaseActivity() {
    private var binding : ActivityEventInfoBinding? = null
    private lateinit var board : Board
    private lateinit var host: User


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        //catches the event document id sent from main activity
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            board = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setupActionBar(board.name)

        FirestoreClass().getEventHost(this, board.creatorID)

        binding?.ibEventLocation?.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(Constants.BOARD_DETAIL, board)
            startActivity(intent)
        }

        binding?.ibHostMessage?.setOnClickListener {
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(Constants.USER_KEY, host)
            startActivity(intent)

        }
    }

    private fun populateEventInformation() {
        binding?.ivEventInfo?.let {
            // Sets the event image
            Glide
                .with(this)
                .load(board.image)
                .centerCrop()
                .placeholder(R.drawable.add_screen_image_placeholder)
                .into(it)
        }
        // Sets the events host profile image
        binding?.civEventHost.let {
            Glide
                    .with(this)
                    .load(host.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(it!!)

        }
        binding?.tvEventInfoTime?.text  = board.eventTime
        binding?.tvEventInfoDate?.text = getCurrentDate(board.eventDate)
        binding?.tvHostName?.text = host.name
        binding?.tvEventInfoName?.text = board.name
        binding?.tvEventLocation?.text = board.eventLocation
        binding?.tvEventDescription?.text = board.eventDescription

        //hide message button if host is the same as current user
        if (host.id == getCurrentUserID()) {
            binding?.ibHostMessage?.visibility = View.GONE
        }
    }

    fun setHost(user: User){
        host = user

        populateEventInformation()
    }

    private fun getCurrentDate(long : Long): String {
        val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

        return formatter.format(long)
    }

    fun boardDetails(board : Board) {
        hideProgressDialog()
        //called here so we can access the board information and set the Event name as the title
        setupActionBar(board.name)

    }

    private fun setupActionBar(title: String) {
        setSupportActionBar(binding?.toolbarEventInfoActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = title
        }
        binding?.toolbarEventInfoActivity?.setNavigationOnClickListener{
            onBackPressed()
        }

    }
  }