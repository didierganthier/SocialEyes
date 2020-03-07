package com.example.socialeyes.Fragments


import android.os.Bundle
import android.renderscript.Sampler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialeyes.Adapter.UserAdapter
import com.example.socialeyes.Model.User

import com.example.socialeyes.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment()
{
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true) }
        recyclerView?.adapter = userAdapter

        view.search_edit_text.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if(view.search_edit_text.text.toString() == "")
                {
                    retrieveUsers()
                }
                else
                {
                    recyclerView?.visibility = View.VISIBLE
                    search_text.visibility = View.GONE

                    retrieveUsers()
                    searchUser(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?)
            {
            }

        })

        return view
    }

    private fun searchUser(input: String)
    {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(context, "${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                mUser?.clear()

                for(snapshot in p0.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    if(user != null)
                    {
                        mUser?.add(user)
                    }
                }

                userAdapter?.notifyDataSetChanged()
            }

        })
    }

    private fun retrieveUsers()
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(context, "${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(view!!.search_edit_text.text.toString() == "")
                {
                    mUser?.clear()

                    for(snapshot in p0.children)
                    {
                        val user = snapshot.getValue(User::class.java)
                        if(user != null)
                        {
                            mUser?.add(user)
                        }
                    }

                    userAdapter?.notifyDataSetChanged()
                }
            }

        })
    }
}
