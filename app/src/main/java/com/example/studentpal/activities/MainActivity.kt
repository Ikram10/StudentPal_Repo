package com.example.studentpal.activities

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.adapter.BoardItemsAdapter
import com.example.studentpal.databinding.ActivityMainBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.messages.LatestMessagesActivity
import com.example.studentpal.models.Board
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener{

    private var binding: ActivityMainBinding? = null
    private var drawer: DrawerLayout? = null
    private lateinit var builder: AlertDialog.Builder
    private var db : FirebaseFirestore? = null
    private var mainRecyclerView : RecyclerView? = null
    private var eventTextView : TextView? = null
    private lateinit var refreshLayout : SwipeRefreshLayout

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        drawer = binding?.drawerLayout

        setupActionBar()

        mainRecyclerView = binding?.appBarMain?.root?.findViewById(R.id.rv_boards_list)
        eventTextView = binding?.appBarMain?.root?.findViewById(R.id.tv_events)

        binding?.navView?.setNavigationItemSelectedListener(this)

        /* loads the currently logged in user's data into this activity, by retrieving their document from firebase
         * The events list for the current user is also loaded into this activity
         */
        FirestoreClass().loadUserData(this, true)

        //create board action button can be clicked
        binding?.appBarMain?.fabCreateBoard?.setOnClickListener {
            //creates an intent that sends user to the Create board activity
            val intent = Intent(this, CreateBoardActivity::class.java)

            /* sends the users name with an intent via a HashMap format
               which can be retrieved using the name key
             */
            intent.putExtra(Constants.NAME, mUserName)
            //handles updates to the main activity events when a new event is created
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
        

        refreshLayout = binding?.appBarMain?.root?.findViewById(R.id.refresh_view_main)!!
        refreshLayout.setOnRefreshListener { updateMainUI() }


    }

    //method handles the displaying of event items in the main activity UI
    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        /* if the boards list size is greater than 0 we can set the visibility of the recycler view to visible
         * and set the no events textview to gone
         */
        if (boardsList.size > 0){
            eventTextView?.text = "All Events"
            mainRecyclerView?.layoutManager = LinearLayoutManager(this)
            mainRecyclerView?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList )
            mainRecyclerView?.adapter = adapter


            //handles the functionality when an event card is selected
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener {
                // when an event card is selected this method will be triggered
                override fun onClick(position: Int, model: Board) {
                    //sends the user to the Event Information screen
                    val intent = Intent(this@MainActivity, EventInfoActivity::class.java)
                    //passes the selected event card's document id to the Event info activity
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentID)
                    startActivity(intent)
                }
            })
        } else
            //if no events are listed "No Event" will be displayed
            eventTextView?.text = "No Events"

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        }
        //Updates the main activity when a new event is created
        else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this)

        }
        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun setupActionBar() {
        val toolBar = binding?.appBarMain?.toolbarMainActivity
        setSupportActionBar(toolBar)
        toolBar?.setNavigationIcon(R.drawable.ic_round_menu_24)

        toolBar?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

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

    // Function handles the selecting of menu items in the drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_active_users -> {

            }
            R.id.nav_friends -> {

            }
            R.id.nav_messages -> {
                startActivity(Intent(this, LatestMessagesActivity::class.java))
            }
            R.id.nav_sign_out -> {
               signOutUser()
            }
            R.id.nav_delete_account-> {
                deleteAccount()
            }
        }
        drawer?.closeDrawer(GravityCompat.START)
        return true
    }

    //my code
    private fun deleteAccount(){
        builder = AlertDialog.Builder(this)

        builder.setTitle("Alert")
            .setMessage("Do you want to delete account?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                db = FirebaseFirestore.getInstance()
                db!!.collection(Constants.USERS).document(getCurrentUserID()).delete().addOnSuccessListener {
                    Log.d("FirestoreDelete", "User account deleted from FireStore.")
                }

                val user = Firebase.auth.currentUser!!

                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("DeleteAccount", "User account deleted.")
                        }
                    }.addOnFailureListener{
                        Log.d("DeleteAccount", "User account delete failed.")
                    }

                val intent = Intent(this, IntroActivity::class.java)
                //close
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
        //sets the user's name
        mUserName = user.name
        //variable binds the Username Textview
        val tvUsername = binding?.navView?.findViewById<TextView>(R.id.tv_username)

        //Third party resource, helps with TODO
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into((binding?.navView?.findViewById<CircleImageView>(R.id.nav_user_image)!!))

        //Sets the username text found in the navigation header to the current users name
        tvUsername?.text = mUserName

        //only retrieves the events list from Firestore if the readBoardsList is true
        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)

        }
    }
    
    //Method responsible for refreshing the Main activity when an event item has been deleted/edited
    private fun updateMainUI () {
        val intent = Intent(this, MainActivity::class.java)
        startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        refreshLayout.isRefreshing = false
    }

}
