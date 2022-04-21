package com.example.studentpal.activities.events

import android.content.Intent
import android.os.Bundle
import com.example.studentpal.R
import com.example.studentpal.activities.BaseActivity
import com.example.studentpal.databinding.ActivityEventInfoBinding
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants

class EventInfoActivity : BaseActivity() {
    private var binding : ActivityEventInfoBinding? = null
    private lateinit var board : Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //catches the event document id sent from main activity
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            board = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setupActionBar(board.name)

        binding?.tvEventLocation?.text = board.eventLocation

        binding?.btnMap?.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(Constants.BOARD_DETAIL, board)
            startActivity(intent)
        }
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