package com.example.studentpal.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentpal.R
import com.example.studentpal.adapter.FriendsAssignedAdapter
import com.example.studentpal.models.User

abstract class MembersListDialog(
        context: Context,
        private var list: ArrayList<User>,
        private val title: String = ""
    ) : Dialog(context) {

        private var adapter: FriendsAssignedAdapter? = null

    //inflates the dialog's view
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_list, null)

            setContentView(view)
            //context.setTheme(R.style.MyDialogTheme)
            setCanceledOnTouchOutside(true)
            setUpRecyclerView(view)
            view.setBackgroundColor(context.resources.getColor(R.color.white))


        }

        private fun setUpRecyclerView(view: View) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.rvList)

            view.findViewById<TextView>(R.id.tvTitle).text = title

            //if there are users assigned to the event
            if (list.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            adapter = FriendsAssignedAdapter(context, list)
            recyclerView.adapter = adapter

            adapter!!.setOnClickListener( object :
                FriendsAssignedAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }
    //implements what happens once a color item is selected
    protected abstract fun onItemSelected(user: User, action: String)
}