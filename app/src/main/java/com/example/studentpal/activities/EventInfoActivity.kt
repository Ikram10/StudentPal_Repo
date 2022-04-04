package com.example.studentpal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityEventInfoBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants

class EventInfoActivity : BaseActivity() {
    private var binding : ActivityEventInfoBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var boardDocumentId = ""
        //catches the event document id sent from main activity
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
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