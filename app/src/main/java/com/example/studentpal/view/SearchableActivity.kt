package com.example.studentpal.view

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.google.firebase.firestore.FirebaseFirestore

class SearchableActivity : AppCompatActivity() {
    lateinit var firebaseDB: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doMySearch(query)
            }
        }
    }

    private fun doMySearch(query: String) {
        firebaseDB = FirebaseFirestore.getInstance()
        //Creates a reference to the Users collection in Firestore
        val usersRef = firebaseDB.collection(Constants.USERS)

        //rey
        usersRef.whereEqualTo(Constants.EMAIL, query).get()
            .addOnSuccessListener { documents ->
            for (document in documents) {
                Log.d("Users retrieved:", "${document.id} => ${document.data}")
            }
        }
            .addOnFailureListener { exception ->
                Log.w("User Retrieval failed:", "Error getting documents: ", exception)
            }


    }
}
