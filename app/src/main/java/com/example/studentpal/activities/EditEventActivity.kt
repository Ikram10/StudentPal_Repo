package com.example.studentpal.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityEditEventBinding
import com.example.studentpal.dialogs.EventCardColorListDialog
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.Board
import com.example.studentpal.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class EditEventActivity : BaseActivity() {
    var binding: ActivityEditEventBinding? = null
    private lateinit var mBoardDetails: Board
    private lateinit var etEventName: AppCompatEditText
    private var mSelectedColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        etEventName = binding?.etNameEventDetails!!
        //sets the editable text to the event name
        etEventName.setText(mBoardDetails.name)
        //when clicked the focus will be set to the end of the text string
        etEventName.setSelection(etEventName.text.toString().length)


        setupActionBar()

        mSelectedColor = mBoardDetails.cardColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }


        binding?.btnUpdateEventDetails?.setOnClickListener {
            if (etEventName.text.toString().isNotEmpty()) {
                updateEventDetails()
            } else {
                Toast.makeText(this, "Please Enter an Event name", Toast.LENGTH_LONG).show()
            }

        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            eventCardColorListDialog()
        }
    }

    //method triggers the card color dialog
    private fun eventCardColorListDialog(){
        val colorList: ArrayList<String> = colorsList()

        val listDialog = object: EventCardColorListDialog(
            this,
            colorList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor){
            override fun onItemSelected(color: String) {
               mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    //sets the background color for the color label and Event card
    private fun setColor(){
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))

    }

    //returns the list of colors the user can use for their event cards
    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()

        colorsList.add("#FFD0C1") //Light orange
        colorsList.add("#D7F2FF") //Light blue
        colorsList.add("#DFFFE0") //Light green
        colorsList.add("#EDE4FF") //Light purple
        colorsList.add("#FFF9CF") //Light yellow
        colorsList.add("#FFC7C7") //Light red


        return colorsList
    }




    private fun setupActionBar() {
        val toolbar = binding?.toolbarEditEventActivity
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Edit Event: ${mBoardDetails.name}"
        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun updateEventDetails() {
        val eventHashMap = HashMap<String, Any>()

        if (etEventName.text.toString() != mBoardDetails.name) {
            eventHashMap[Constants.NAME] = binding?.etNameEventDetails?.text.toString()
        }

        if(mBoardDetails.cardColor != mSelectedColor) {
            eventHashMap[Constants.CARD_COLOR] = mSelectedColor
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateBoardDetails(this, eventHashMap, mBoardDetails.documentID)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_delete_event, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.action_delete_card -> {
               deleteEvent()
           }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteEvent() {
        //The event can only be deleted by the event creator
        if (mBoardDetails.creatorID == FirebaseAuth.getInstance().currentUser!!.uid) {
            alertDialogForDeleteEvent(mBoardDetails.name)
        }
        else {
            Toast.makeText(this,"You don't have permission to delete event ", Toast.LENGTH_LONG).show()
        }
    }

    private fun alertDialogForDeleteEvent(eventName: String) {
        val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder.setTitle("Alert")
            .setMessage("Do you want to delete Event: $eventName")
            .setCancelable(true)
            .setIcon(R.drawable.ic_round_warning_24)
            .setPositiveButton("Yes") { _, _ ->
                FirestoreClass().deleteEvent(this, mBoardDetails)
            }
            .setNegativeButton("No") { DialogInterface, _ ->
                DialogInterface.cancel()
            }
            .show()
    }
}