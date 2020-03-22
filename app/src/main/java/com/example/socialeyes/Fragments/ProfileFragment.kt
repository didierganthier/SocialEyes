package com.example.socialeyes.Fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialeyes.AccountSettingsActivity
import com.example.socialeyes.Model.User
import com.example.socialeyes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null)
        {
            this.profileId = pref.getString("profileId", "none")!!
        }

        if(profileId == firebaseUser.uid)
        {
            view.edit_account_settings_btn.text = "Edit Profile"
        }
        else if(profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButtonStatus()
        }

        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()

            when
            {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }
            }
        }

        getFollowers()
        getFollowings()
        userInfo()

        return view
    }

    private fun checkFollowAndFollowingButtonStatus()
    {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")

        }

        if(followingRef != null)
        {
            followingRef.addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(context, "${p0.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot)
                {
                    if(p0.child(profileId).exists())
                    {
                        view?.edit_account_settings_btn?.text = "Following"
                    }
                    else
                    {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

            })
        }
    }

    private fun getFollowers()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context,"${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    view?.total_followers?.text = p0.childrenCount.toString()
                }
            }

        })
    }

    private fun getFollowings()
    {
        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingRef.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context,"${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    view?.total_following?.text = p0.childrenCount.toString()
                }
            }

        })
    }

    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(context, "${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text = user!!.getUsername()
                    view?.full_name_profile_frag?.text = user!!.getFullname()
                    view?.bio_profile_frag?.text = user!!.getBio()
                }
            }

        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

}
