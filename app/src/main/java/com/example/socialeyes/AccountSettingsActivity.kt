package com.example.socialeyes

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialeyes.Fragments.ProfileFragment
import com.example.socialeyes.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        progressDialog = ProgressDialog(this)

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        save_infos_profile_btn.setOnClickListener {
            if(checker == "clicked")
            {

            }
            else
            {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun updateUserInfoOnly()
    {
        when {
            full_name_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty full name...", Toast.LENGTH_SHORT).show()
            username_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty username...", Toast.LENGTH_SHORT).show()
            bio_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty bio...", Toast.LENGTH_SHORT).show()
            else -> {

                progressDialog.setTitle("Updating Infos")
                progressDialog.setMessage("Please wait a few seconds...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_name_profile_settings.text.toString().toLowerCase()
                userMap["username"] = username_profile_settings.text.toString().toLowerCase()
                userMap["bio"] = bio_profile_settings.text.toString()

                userRef.child(firebaseUser.uid).updateChildren(userMap)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful)
                        {
                            Toast.makeText(applicationContext, "Account infos have been updated", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                        else
                        {
                            Toast.makeText(applicationContext, "${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                        }
                        progressDialog.dismiss()
                    }
            }
        }
    }

    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(applicationContext, "${p0.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_settings)
                    username_profile_settings.setText(user.getUsername())
                    full_name_profile_settings.setText(user.getFullname())
                    bio_profile_settings.setText(user.getBio())
                }
            }

        })
    }
}
