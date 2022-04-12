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
import com.example.studentpal.adapter.CardColorItemsAdapter

//abstract: so it cannot be instantiated. Only inheritable.
abstract class EventCardColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: CardColorItemsAdapter? = null

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
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CardColorItemsAdapter(context, list, mSelectedColor)
        recyclerView.adapter = adapter

        adapter!!.onItemClickListener =
            object : CardColorItemsAdapter.OnItemClickListener {
            override fun OnClick(position: Int, color: String) {
                dismiss() // dismisses this dialog
                //when a color is clicked, a color will be passes as an argument which can be used here
                onItemSelected(color)
            }

        }
   }

    //implements what happens once a color item is selected
    protected abstract fun onItemSelected(color: String)
}