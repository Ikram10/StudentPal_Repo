package com.example.studentpal.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityMainBinding
import com.example.studentpal.firebase.FirestoreClass
import com.example.studentpal.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding : ActivityMainBinding? = null
    private var drawer : DrawerLayout? = null
    private lateinit var builder: AlertDialog.Builder

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        drawer = binding?.drawerLayout

        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)
        } else{
            Log.e("Cancelled", "Cancelled")
        }
    }
    private fun setupActionBar(){
        val toolBar = binding?.appBarMain?.toolbarMainActivity
        setSupportActionBar(toolBar)
        toolBar?.setNavigationIcon(R.drawable.ic_round_menu_24)

        toolBar?.setNavigationOnClickListener{
            toggleDrawer()
        }
    }

    private fun toggleDrawer (){

        if (drawer?.isDrawerOpen(GravityCompat.START) == true){
            drawer?.closeDrawer(GravityCompat.START)
        }else {
            drawer?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawer?.isDrawerOpen(GravityCompat.START) == true){
            drawer?.closeDrawer(GravityCompat.START)
        }else {
            doubleBackToExit()
        }
    }

    // Function handles the selecting of menu items in the drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                builder = AlertDialog.Builder(this)

                builder.setTitle("Alert")
                    .setMessage("Do you want to sign out?")
                    .setCancelable(true)
                    .setPositiveButton("Yes"){ DialogInterface, it ->

                        FirebaseAuth.getInstance().signOut()

                        val intent = Intent(this, IntroActivity::class.java )
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
        }
        drawer?.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Function updates the users Profile image in the navigation view header
     * adds the users name to the navigation view header
     */
    fun updateNavigationUserDetails(user : User) {

            val tv_username = binding?.navView?.findViewById<TextView>(R.id.tv_username)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into((binding?.navView?.findViewById<ImageView>(R.id.nav_user_image)!!))

        tv_username?.text = user.name
    }
}
