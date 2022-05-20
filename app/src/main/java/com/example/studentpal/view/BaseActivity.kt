package com.example.studentpal.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.studentpal.R
import com.example.studentpal.view.registration.IntroActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * Base class for activities that wish to use features common to all activities.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 * because it provided implementations for features that would be beneficial for the project.
 *
 * All code that was created by the author is labeled with [My Code].
 *
 * [Developers Guide](https://developer.android.com/reference/android/content/Intent)assisted
 * the author in implementing the additional code.
 *
 * @see[com.example.studentpal.common.References]
 */
@Suppress("DEPRECATION")
open class BaseActivity : AppCompatActivity() {

    private lateinit var builder: AlertDialog.Builder

    private var doubleBackToExitPressOnce = false

    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

    }

    /**
     * A function used to show the progress dialog with the title and message to user
     */
    fun showProgressDialog(text : String) {
        mProgressDialog = Dialog(this)
        //sets the screen content from a layout resource.
        mProgressDialog?.setContentView(R.layout.dialog_progress)

        val tvProgress : TextView = mProgressDialog!!.findViewById(R.id.tv_progress_text)
        tvProgress.text = text

        mProgressDialog?.show()
    }

    /**
     * A function used to dismiss the progress dialog if it is visible to user
     */
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

    /**
     * A method that displays an error message to the user
     *
     * @param message the error message to display
     */
    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message, Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))

        snackBar.show()
    }


    /**
     * My Code: Displays an alert dialog prompting for confirmation to sign out
     */
    protected fun signOutUser() {
        builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
            .setMessage("Do you want to sign out?")
            .setCancelable(true)
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                // Closes all opened activities
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { DialogInterface, _ ->
                DialogInterface.cancel()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }
}