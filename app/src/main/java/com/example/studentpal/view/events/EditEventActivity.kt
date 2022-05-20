@file:Suppress("DEPRECATION")

package com.example.studentpal.view.events

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.GridLayoutManager
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.common.dialogs.EventCardColorListDialog
import com.example.studentpal.databinding.ActivityEditEventBinding
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.EventDatabase
import com.example.studentpal.model.remote.EventDatabase.updateEventDetails
import com.example.studentpal.model.remote.UsersDatabase.getAssignedFriendsListDetails
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.CardAttendeeItemAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
/**
 * This activity is responsible for editing the event information.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * Reused code that has been adapted by the author is labeled [Adapted ].
 *
 * @see[com.example.studentpal.common.References]
 */
class EditEventActivity : BaseActivity() {
    //Global Variables
    private var binding: ActivityEditEventBinding? = null
    private lateinit var mEventDetails: Event
    private lateinit var mEventDocumentId: String
    private lateinit var etEventName: AppCompatEditText
    private var mSelectedColor = "" // Selected event card colour
    private lateinit var mAssignedMemberDetailList: ArrayList<User> //User list of assigned members
    private var mSelectedDueDateMilliSeconds: Long = 0 // Event date


    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Retrieves the intent data
        if (intent.hasExtra(Constants.EVENT_DETAIL)) {
            //initialises the event details
            mEventDetails = intent.getParcelableExtra(Constants.EVENT_DETAIL)!!
            //initialises the event document id
            mEventDocumentId = mEventDetails.documentID
            showProgressDialog(resources.getString(R.string.please_wait))
            //retrieves the friends list for the event
            getAssignedFriendsListDetails(this, mEventDetails.assignedTo)
        }

        etEventName = binding?.etNameEventDetails!!
        //sets the editable text to the event name
        etEventName.setText(mEventDetails.name)
        //when clicked the focus will be set to the end of the text string
        etEventName.setSelection(etEventName.text.toString().length)

        setupActionBar()

        //sets the label colour to the selected colour
        mSelectedColor = mEventDetails.cardColor
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

        binding?.tvSelectEventDate?.setOnClickListener {
            showDatePicker()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            getAssignedFriendsListDetails(this, mEventDetails.assignedTo)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * method triggers the card color dialog
     */
    private fun eventCardColorListDialog(){
        // initialises the colour list
        val colourList: ArrayList<String> = coloursList()

        val listDialog = object: EventCardColorListDialog(
            this,
            colourList,
            resources.getString(R.string.str_select_card_color),
            mSelectedColor){
            // When user clicks a colour strip the label colour will be set
            override fun onItemSelected(color: String) {
               mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    /**
     * Method sets the background color for the color label and Event card
     */
    private fun setColor(){
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))

    }

    /**
     * method responsible for returning the list of event card colours
     *
     * @return the list of colors the user can use for their event cards
     */
    private fun coloursList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        //[My Code]: a list of colours the event card can be. Author chose the colours
        colorsList.add("#FFD0C1") //Light orange
        colorsList.add("#D7F2FF") //Light blue
        colorsList.add("#DFFFE0") //Light green
        colorsList.add("#EDE4FF") //Light purple
        colorsList.add("#FFF9CF") //Light yellow
        colorsList.add("#FFC7C7") //Light red
        colorsList.add("#FFFFFF") //White

        return colorsList
    }

    /**
     * Method sets up the action bar
     */
    private fun setupActionBar() {
        val toolbar = binding?.toolbarEditEventActivity
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Edit Event: ${mEventDetails.name}"
        }
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Method updates the event details only if changes have been made
     */
    private fun updateEventDetails() {
        // HashMap to store the information changes
        val eventHashMap = HashMap<String, Any>()
        // Event Name changes
        if (etEventName.text.toString() != mEventDetails.name) {
            eventHashMap[Constants.NAME] = binding?.etNameEventDetails?.text.toString()
        }
        // Card Colour changes
        if(mEventDetails.cardColor != mSelectedColor) {
            eventHashMap[Constants.CARD_COLOR] = mSelectedColor
        }
        // Event date changes
        if(mEventDetails.eventDate != mSelectedDueDateMilliSeconds) {
            eventHashMap[Constants.EVENT_DATE] = mSelectedDueDateMilliSeconds
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        // Update the event information in the database
        updateEventDetails(this, eventHashMap, mEventDetails.documentID)
    }

    /**
     * Adds a delete icon button to the action bar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_delete_event, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Deletes the event if delete button selected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.action_delete_card -> {
               deleteEvent()
           }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Method responsible for deleting an event
     */
    private fun deleteEvent() {
        //The event can only be deleted by the event creator
        if (mEventDetails.creatorID == getCurrentUserID()) {
            // Delete confirmation alert dialog
            alertDialogForDeleteEvent(mEventDetails.name)
        }
        else {
            Toast.makeText(this,"You don't have permission to delete event ", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Method displays a delete confirmation alert dialog to the user
     */
    private fun alertDialogForDeleteEvent(eventName: String) {
        val builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder.setTitle("Alert")
            .setMessage("Do you want to delete Event: $eventName")
            .setCancelable(true)
            .setIcon(R.drawable.ic_round_warning_24)
            .setPositiveButton("Yes") { _, _ ->
                // Confirmation will delete the event information from Firestore
                EventDatabase.deleteEvent(this, mEventDetails)
            }
            .setNegativeButton("No") { DialogInterface, _ ->
                DialogInterface.cancel()
            }
            .show()
    }

    /**
     * Method sets up the recycler view that displays the grid list of users assigned to the event
     *
     * @param list the list of users assigned to the event
     */
    fun setUpAssignedMembersList(list: ArrayList<User>){
        mAssignedMemberDetailList = list
        hideProgressDialog()

        // displays the recycler view showing the assigned members if at least one member is assigned
        if(mAssignedMemberDetailList.size > 0){
            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE
            //initialises a grid layout for the recycler view
            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(this, 6)
            //initialises the adapter for the recyclerview
            val adapter = CardAttendeeItemAdapter(this, mAssignedMemberDetailList)
            binding?.rvSelectedMembersList?.adapter = adapter

            adapter.setOnClickListener(
                object : CardAttendeeItemAdapter.OnClickListener{
                    override fun onClick() {
                       val intent = Intent(this@EditEventActivity, AssignFriendsActivity::class.java)
                        intent.putExtra(Constants.EVENT_DETAIL, mEventDetails )
                        startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                    }
                }
            )
        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }
    /**
     * Method to display a date picker dialog to user
     */
    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val calYear = c.get(Calendar.YEAR) // Returns the value of the given calendar year
        val calMonth = c.get(Calendar.MONTH) // Returns the value of the given calendar month
        val calDay = c.get(Calendar.DAY_OF_MONTH) // Returns the value of the given calendar day

        DatePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            { _, year, month, day ->
                // Appends a 0 to the start of day if month is less than 10
                val sDayOfMonth = if (day < 10) "0$day" else "$day"
                // Appends a 0 to the start of month  if less than 10
                val sMonthOfYear = if ((month + 1) < 10) "0${month + 1}" else "${month + 1}"
                // Sets the textview to the selected date
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding?.tvSelectEventDate?.text = selectedDate

                //formats the date in a readable format
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            calYear,
            calMonth,
            calDay
        ).show()
    }

    /**
     *this method is called when the event has been modified successfully
     */
    fun eventModifiedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }


}