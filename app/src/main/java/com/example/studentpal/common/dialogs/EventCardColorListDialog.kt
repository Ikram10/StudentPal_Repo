package com.example.studentpal.common.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentpal.R
import com.example.studentpal.view.adapter.CardColorItemsAdapter
/**
 * Abstract class for the event card color dialog.
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (Panjuta, 2021) (see references file)
 * because it provided implementations for features that would be beneficial for the project.
 *
 * @see[com.example.studentpal.common.References]
 */

//abstract: so it cannot be instantiated. Only inheritable.
abstract class EventCardColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: CardColorItemsAdapter? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null).apply {
            setContentView(this)
        }
        //context.setTheme(R.style.MyDialogTheme)
        setCanceledOnTouchOutside(true)
        setUpRecyclerView(view)
        view.setBackgroundColor(
            ContextCompat
                .getColor(context, R.color.white)
        )


    }

    private fun setUpRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvList)
        view.findViewById<TextView>(R.id.tvTitle).text = title
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CardColorItemsAdapter(context, list, mSelectedColor)
        recyclerView.adapter = adapter

        adapter!!.onItemClickListener =
            object : CardColorItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, Color: String) {
                dismiss() // dismisses this dialog
                //when a color is clicked, a color will be passed as an argument which can be used here
                onItemSelected(Color)
            }

        }
   }

    //implements what happens once a color item is selected
    protected abstract fun onItemSelected(color: String)
}