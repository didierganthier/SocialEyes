package com.example.socialeyes.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.socialeyes.CommentsActivity
import com.example.socialeyes.MainActivity
import com.example.socialeyes.Model.Post
import com.example.socialeyes.Model.User
import com.example.socialeyes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context,
                  private val mPost: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        holder.description.text = post.getDescription()

        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.getPublisher())
        isLikes(post.getPostid(), holder.likeButton)
        numberOfLikes(holder.likes, post.getPostid())
        getTotalComments(holder.comments, post.getPostid())

        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag == "Like")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.commentButton.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostid())
            intent.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.getPostid())
            intent.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intent)
        }
    }

    private fun numberOfLikes(likes: TextView, postid: String)
    {
        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(mContext,p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    if(p0.childrenCount.toString() == "1" || p0.childrenCount.toString() == "0")
                    {
                        likes.text = p0.childrenCount.toString() + " like"
                    }
                    else
                    {
                        likes.text = p0.childrenCount.toString() + " likes"
                    }
                }
            }
        })
    }

    private fun getTotalComments(comments: TextView, postid: String)
    {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(mContext,p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    if(p0.childrenCount.toString() == "1")
                    {
                        comments.text = "View " + p0.childrenCount.toString() + " comment"
                    }
                    else
                    {
                        comments.text = "View all " + p0.childrenCount.toString() + " comments"
                    }
                }
            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView)
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(mContext,p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else
                {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }
        })
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var username: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_home)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_image_save_btn)
            username = itemView.findViewById(R.id.user_name_home)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
        }
    }

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherID: String)
    {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        usersRef.addValueEventListener(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
                Toast.makeText(mContext, p0.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if(p0.exists())
                {
                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    username.text = user.getUsername()
                    publisher.text = user.getFullname()
                }
            }

        })
    }


}