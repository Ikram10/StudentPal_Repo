package com.example.studentpal.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.view.friends.FriendProfile
import com.example.studentpal.databinding.ItemAssignFriendBinding
import com.example.studentpal.model.entities.User
import com.example.studentpal.common.Constants

/**
 * This adapter will display a list of Users in the Recycler view.
 *
 * The methods displayed was adapted from Denis Panjuta's Trello clone (see references file).
 * However alterations were made to the code to suit the project's requirements. For instance,
 * filterable interface was implemented to add a search function, see [filters] below.
 *
 * @see[com.example.studentpal.common.References]
 */

class UsersAdapter(var context: Context, var list: ArrayList<User>)
    : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(), Filterable{

    // contains all the Users in the database before filtering
    var listAll = ArrayList(list)

    inner class UsersViewHolder(private val itemBinding: ItemAssignFriendBinding):
        RecyclerView.ViewHolder(itemBinding.root) {
            fun bindItem(user: User){
                // Loads users name into textview
                itemBinding.tvMemberName.text = user.name
                // Loads users username into textview
                itemBinding.tvMemberUsername.text = user.username
                //Loads users profile image
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
        /* User item is clickable.
         * Navigates user to the selected users profile
         */
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, FriendProfile::class.java)
            intent.putExtra(Constants.USER_KEY, model)
            it.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }


    /**
     * The following code is responsible for executing the search functionality in the find friends
     * activity.
     *
     * The entire code was adapted from a YouTube tutorial delivered by Foxandroid (Foxandroid, 2021).
     * The code was adapted to allow users to be searched by their username and filter the recyclerview
     * to display the results.
     *
     * @see com.example.studentpal.view.friends.FindFriends
     * @see com.example.studentpal.common.References
     */

    override fun getFilter(): Filter {
        return filters
    }

    private val filters : Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val filteredList : ArrayList<User> = ArrayList()
            // Show all users if no search is occurring
            if(charSequence.toString().isEmpty()){
                filteredList.addAll(listAll)
            } else {
                for (user: User in listAll ) {
                    // Add users to the filtered list matching the search query
                    if (user.username.lowercase()
                            .contains(charSequence.toString().lowercase())){
                        filteredList.add(user)
                    }
                }
            }
            val filteredResults = FilterResults()
            filteredResults.values = filteredList

            return filteredResults
        }

        // This method displays the filtered results to the recyclerview
        @SuppressLint("NotifyDataSetChanged")
        @Suppress("UNCHECKED_CAST")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            list.clear()
            if (filterResults != null) {
                list.addAll(filterResults.values as Collection<User>)
                notifyDataSetChanged()
            }
        }

    }

}


