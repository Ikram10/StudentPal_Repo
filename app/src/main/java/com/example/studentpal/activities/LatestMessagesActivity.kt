package com.example.studentpal.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Messenger
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import com.example.studentpal.R
import com.example.studentpal.databinding.ActivityLatestMessagesBinding

//kotlinMessenger code
class LatestMessagesActivity : BaseActivity() {
    private var binding : ActivityLatestMessagesBinding? = null
    private var toolbar : androidx.appcompat.widget.Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

    }

    //Denis Panjuta code.
    private fun setupActionBar() {
        toolbar = binding?.toolbarLatestMessages
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = "Messenger"
        }
        toolbar?.setNavigationOnClickListener{
            onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                startActivity(Intent(this, NewMessageActivity::class.java))
            }
            R.id.menu_sign_out -> {
                signOutUser()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}