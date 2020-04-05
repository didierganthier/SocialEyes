package com.example.socialeyes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialeyes.Model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity()
{

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postId = intent.getStringExtra("postId")
        publisherId = intent.getStringExtra("publisherId")


        firebaseUser = FirebaseAuth.getInstance().currentUser


        userInfo()

        post_comment.setOnClickListener {
            if(add_comment.text.toString() == "")
            {
                Toast.makeText(this@CommentsActivity, "Please write a comment first...", Toast.LENGTH_LONG).show()
            }
            else
            {
                addComment()
            }
        }
    }



    private fun addComment()
    {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postId)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        add_comment.text.clear()
    }

    private fun userInfo()
    {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

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

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comment)
                }
            }

        })
    }

}
