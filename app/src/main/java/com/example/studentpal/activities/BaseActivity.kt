package com.example.studentpal.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.renderscript.ScriptGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityBaseBinding
import com.example.studentpal.databinding.MainContentBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {

    private lateinit var builder: AlertDialog.Builder

    private var alertDialog : AlertDialog? = null

    private var doubleBackToExitPressOnce = false

    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

    }


    fun showProgressDialog(text : String) {

        mProgressDialog = Dialog(this)
        //sets the screen content from a layout resource.
        mProgressDialog!!.setContentView(R.layout.dialog_progress)


        var tvProgress : TextView = mProgressDialog!!.findViewById(R.id.tv_progress_text)
        tvProgress.text = text

        mProgressDialog!!.show()
    }

    fun hideProgressDialog(){
        mProgressDialog?.dismiss()
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressOnce){
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressOnce = true
        Toast.makeText(this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT).show()

        Handler().postDelayed({doubleBackToExitPressOnce = false}, 2000)
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))

        snackBar.show()
    }


    //My code
    protected fun signOutUser() {
        builder = AlertDialog.Builder(this)

        builder.setTitle("Alert")
            .setMessage("Do you want to sign out?")
            .setCancelable(true)
            .setPositiveButton("Yes") { DialogInterface, it ->

                FirebaseAuth.getInstance().signOut()

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
}