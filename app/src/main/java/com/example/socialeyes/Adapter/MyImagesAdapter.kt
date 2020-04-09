package com.example.socialeyes.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.socialeyes.Model.Post
import com.example.socialeyes.R
import com.squareup.picasso.Picasso

class MyImagesAdapter (private val mContext: Context, private val mPost: List<Post>)
    :RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost[position]

        Picasso.get().load(post.getPostimage()).placeholder(R.drawable.profile).into(holder.postImage)
    }

    inner class ViewHolder(@NonNull itemView: View)
        : RecyclerView.ViewHolder(itemView)
    {
        var postImage: ImageView

        init {
            postImage = itemView.findViewById(R.id.post_image)
        }
    }
}