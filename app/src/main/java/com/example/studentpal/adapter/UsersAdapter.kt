package com.example.studentpal.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.activities.friends.ViewFriendProfile
import com.example.studentpal.databinding.ItemAssignFriendBinding
import com.example.studentpal.models.User
import com.example.studentpal.utils.Constants

//UserAdapter will display a list of Users in the Recycler view
class UsersAdapter(var context: Context, var list: ArrayList<User>)
    : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(), Filterable{

    // contains all the Users in the database before filtering
    var listAll = ArrayList(list)

    inner class  UsersViewHolder(private val itemBinding: ItemAssignFriendBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
            fun bindItem(user: User){
                itemBinding.tvMemberName.text = user.name
                itemBinding.tvMemberEmail.text = user.email
                Glide
                    .with(context)
                    .load(user.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(itemBinding.ivFriendImage)


            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            ItemAssignFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val model = list[position]

        holder.bindItem(model)
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ViewFriendProfile::class.java)
            intent.putExtra(Constants.USER_KEY, model)
            it.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }












    override fun getFilter(): Filter {
        return filters
    }

    private val filters : Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            var filteredList : ArrayList<User> = ArrayList()
            // Show all users if no search is occurring
            if(charSequence.toString().isEmpty()){
                filteredList.addAll(listAll)
            } else {
                for (user: User in listAll ) {
                    if (user.email.lowercase().contains(charSequence.toString().lowercase())){
                        filteredList.add(user)
                    }
                }
            }
            val filteredResults: FilterResults = FilterResults()
            filteredResults.values = filteredList

            return filteredResults
        }

        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            list.clear()
            if (filterResults != null) {
                list.addAll(filterResults.values as Collection<User>)
                notifyDataSetChanged()
            }
        }

    }




}


