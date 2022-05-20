@file:Suppress("DEPRECATION")

package com.example.studentpal.view.events

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.databinding.ActivityMainBinding
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.entities.User
import com.example.studentpal.model.remote.EventDatabase.deleteUserEventsData
import com.example.studentpal.model.remote.EventDatabase.getEventsList
import com.example.studentpal.model.remote.UsersDatabase.deleteUserData
import com.example.studentpal.model.remote.UsersDatabase.loadUserData
import com.example.studentpal.model.remote.UsersDatabase.updateUserProfileData
import com.example.studentpal.view.BaseActivity
import com.example.studentpal.view.adapter.EventItemsAdapter
import com.example.studentpal.view.friends.FindFriends
import com.example.studentpal.view.friends.FriendsActivity
import com.example.studentpal.view.friends.RequestsActivity
import com.example.studentpal.view.messages.LatestMessagesActivity
import com.example.studentpal.view.profile.MyProfileActivity
import com.example.studentpal.view.profile.PostsActivity
import com.example.studentpal.view.registration.IntroActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

/**
 * This activity is the main component of the application. Users will be able to navigate
 * around the app via the navigation menu. The users scheduled events will be displayed here.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 * However, the author significantly evolved the code produced by Panjuta to accommodate the project requirements.
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * Reused code that has been adapted by the author is labeled [Adapted ].
 *
 * @see[com.example.studentpal.common.References]
 */
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    //Global variables
    private var binding: ActivityMainBinding? = null
    private var drawer: DrawerLayout? = null // Navigation drawer layout
    private lateinit var builder: AlertDialog.Builder
    private var mainRecyclerView: RecyclerView? = null
    private var eventTextView: TextView? = null
    private lateinit var refreshLayout: SwipeRefreshLayout // Allows the screen to be swipe refreshed
    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    //Constant values
    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_EVENT_REQUEST_CODE: Int = 12
    }

    /**
     *  This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        //initialise the drawer layout
        drawer = binding?.drawerLayout

        setupActionBar()

        mainRecyclerView = binding?.appBarMain?.root?.findViewById(R.id.rv_boards_list)
        eventTextView = binding?.appBarMain?.root?.findViewById(R.id.tv_events)

        binding?.navView?.setNavigationItemSelectedListener(this)

        // Shared preferences only available inside within application
        mSharedPreferences =
            this.getSharedPreferences(Constants.STUDENTPAL_PREFERENCES, Context.MODE_PRIVATE)

        // Variable is used get the value either token is updated in the database or not.
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        // Here if the token is already updated than we don't need to update it every time.
        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            // Get the current logged in user details.
            loadUserData(this, true)
        } else {
            FirebaseMessaging
                .getInstance()
                .token
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("FCM token update", "token: ${it.result}")
                        // Get new FCM registration token and update it in users firestore
                        updateFCMToken(it.result)
                    } else
                        Log.d("FCM token update failed", "token: ${it.result}")

                }
        }
        /* loads the currently logged in user's data into this activity, by retrieving their document from firebase
         * The events list for the current user is also loaded into this activity
         */
        loadUserData(this, true)

        //create event action button can be clicked
        binding?.appBarMain?.fabCreateEvent?.setOnClickListener {
            //creates an intent that sends user to the Create Event activity
            val intent = Intent(this, CreateEventActivity::class.java)
            /* sends the users name with an intent via a HashMap format
               which can be retrieved using the name key
             */
            intent.putExtra(Constants.NAME, mUserName)
            //handles updates to the main activity events when a new event is created
            startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE)
        }
        // initialise and set the refresh layout
        refreshLayout = binding?.appBarMain?.root?.findViewById(R.id.refresh_view_main)!!
        refreshLayout.setOnRefreshListener { updateMainUI() }
    }

    /**
     * [Adapted ]: method handles the displaying of event items in the main activity UI.
     * Updates the event text and enables the event items to be clickable.
     *
     * @param eventsList the list of events to be loaded into the recyclerview
     */
    fun populateEventsListToUI(eventsList: ArrayList<Event>) {
        hideProgressDialog()
        /* [My Code]: if the events list size is greater than 0 we can set the visibility of the recycler view to visible
         * and set the events textview
         */
        if (eventsList.size > 0) {
            eventTextView?.setText(R.string.all_events)
            mainRecyclerView?.layoutManager = LinearLayoutManager(this)
            mainRecyclerView?.setHasFixedSize(true)
            //initialise adapter for recyclerview
            val adapter = EventItemsAdapter(this, eventsList)
            mainRecyclerView?.adapter = adapter

            //handles the functionality when an event card is selected
            adapter.setOnClickListener(object : EventItemsAdapter.OnClickListener {
                // when an event card is selected this method will be triggered
                override fun onClick(position: Int, model: Event) {
                    //sends the user to the Event Information screen
                    val intent = Intent(this@MainActivity, EventInfoActivity::class.java)
                    //passes the selected event card's document id to the Edit Event activity
                    intent.putExtra(Constants.EVENT_DETAIL, model)
                    startActivity(intent)
                }
            })
        } else
        // if no events are listed "No Event" will be displayed
            eventTextView?.setText(R.string.no_events)
    }

    /**
     * method starts an activity and receives a result back
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            //Loads the users updated data
            loadUserData(this)
        }
        // Updates the main activity when a new event is created
        else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_EVENT_REQUEST_CODE) {
            getEventsList(this)

        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    /**
     * A method to setup action bar
     */
    private fun setupActionBar() {
        val toolBar = binding?.appBarMain?.toolbarMainActivity
        setSupportActionBar(toolBar)
        toolBar?.setNavigationIcon(R.drawable.ic_round_menu_24)

        toolBar?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    /**
     * A method for opening and closing the Navigation Drawer
     */
    private fun toggleDrawer() {
        if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer?.closeDrawer(GravityCompat.START)
        } else {
            drawer?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer?.isDrawerOpen(GravityCompat.START) == true) {
            drawer?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    /**
     * Method handles the selecting of menu items in the drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            //My Code: navigates users to the active users activity
            R.id.nav_active_users -> {
                startActivity(Intent(this, FindFriends::class.java))
            }
            //My Code: navigates users to the posts activity
            R.id.nav_posts -> {
                startActivity(Intent(this, PostsActivity::class.java))
            }
            //My Code: navigates users to the friends activity
            R.id.nav_friends -> {
                startActivity(Intent(this, FriendsActivity::class.java))
            }
            //My Code: navigates users to the friends requests activity
            R.id.nav_requests -> {
                startActivity(Intent(this, RequestsActivity::class.java))
            }
            //My Code: navigates users to the messages activity
            R.id.nav_messages -> {
                startActivity(Intent(this, LatestMessagesActivity::class.java))
            }
            R.id.nav_sign_out -> {
                signOutUser()
                // removes all values from the preferences
                mSharedPreferences.edit().clear().apply()
            }
            //My Code: deletes account and exits the application
            R.id.nav_delete_account -> {
                deleteAccount()
            }
        }
        drawer?.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * [My code ]: Method deletes the users firestore documents and authentication details. Exits the user from the application
     */
    private fun deleteAccount() {
        // Configures the alert dialog
        builder = AlertDialog.Builder(this, R.style.MyDialogTheme)
        builder.setTitle("Alert")
            .setMessage("Do you want to delete account?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                // when user selects "Yes", delete user data in firestore
                deleteUserData(getCurrentUserID())
                deleteUserEventsData(getCurrentUserID())
                //navigates user back to intro screen
                val intent = Intent(this, IntroActivity::class.java)
                //clears the stack of activities opened
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { DialogInterface, _ ->
                DialogInterface.cancel()
            }
            .show()
    }

    /**
     * Function updates the users Profile image in the navigation view header
     * adds the users name to the navigation view header
     */
    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        //sets the user's name
        mUserName = user.name
        //variable binds the Username Textview
        val tvUsername = binding?.navView?.findViewById<TextView>(R.id.tv_username)

        //Third party resource, helps with image loading
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into((binding?.navView?.findViewById<CircleImageView>(R.id.nav_user_image)!!))

        //Sets the username text found in the navigation header to the current users name
        tvUsername?.text = mUserName

        //only retrieves the events list from Firestore if the readBoardsList is true
        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            getEventsList(this)
        }
    }

    /**
     * Method responsible for refreshing the Main activity when an event item has been deleted/edited
     */
    private fun updateMainUI() {
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE)
        refreshLayout.isRefreshing = false
    }

    /**
     * A function to notify the token is updated successfully in the users database.
     */
    fun tokenUpdateSuccess() {
        hideProgressDialog()
        // Here we have added a another value in shared preference that the token is updated in the database successfully.
        // So we don't need to update it every time.
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        loadUserData(this, true)
    }

    /**
     * A function to update the user's FCM token in the database.
     */
    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        // Update the data in the database.
        updateUserProfileData(this, userHashMap)

    }
}
