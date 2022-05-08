package com.example.studentpal.common

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.studentpal.R
import com.google.firebase.firestore.FirebaseFirestore

class SearchableActivity : AppCompatActivity(){

    val firebaseDB : FirebaseFirestore? = null

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
        firebaseDB?.collection(Constants.USERS)?.whereEqualTo(Constants.EMAIL, query)
            ?.get()
            ?.addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Username search query", "${document.id} => ${document.data}")
                }
            }
            ?.addOnFailureListener { exception ->
                Log.w("Username search query", "Error getting documents: ", exception)
            }
    }
}
