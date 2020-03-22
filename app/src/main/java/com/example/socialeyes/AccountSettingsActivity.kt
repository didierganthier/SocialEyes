package com.example.socialeyes

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialeyes.Fragments.ProfileFragment
import com.example.socialeyes.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private lateinit var progressDialog: ProgressDialog
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        progressDialog = ProgressDialog(this)

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_btn.setOnClickListener {

            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        save_infos_profile_btn.setOnClickListener {
            if(checker == "clicked")
            {
                uploadImageAndUpdateInfo()
            }
            else
            {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image_view_settings.setImageURI(imageUri)
        }
        else
        {
            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show()
        }
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

    private fun uploadImageAndUpdateInfo()
    {
        when
        {
            imageUri == null -> Toast.makeText(applicationContext, "Please select image first", Toast.LENGTH_SHORT).show()
            full_name_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty full name...", Toast.LENGTH_SHORT).show()
            username_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty username...", Toast.LENGTH_SHORT).show()
            bio_profile_settings.text.toString() == "" -> Toast.makeText(applicationContext, "Nobody is allowed to have an empty bio...", Toast.LENGTH_SHORT).show()

            else -> {

                progressDialog.setTitle("Updating Infos")
                progressDialog.setMessage("Please wait a few seconds...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                  if(!task.isSuccessful)
                  {
                      task.exception?.let {
                          throw it
                          progressDialog.dismiss()
                      }
                  }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if(task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = full_name_profile_settings.text.toString().toLowerCase()
                        userMap["username"] = username_profile_settings.text.toString().toLowerCase()
                        userMap["bio"] = bio_profile_settings.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                            .addOnCompleteListener { task ->
                                if(task.isSuccessful)
                                {
                                    Toast.makeText(applicationContext, "Account infos have been updated", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                    progressDialog.dismiss()
                                }
                                else
                                {
                                    Toast.makeText(applicationContext, "${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                                    progressDialog.dismiss()
                                }
                            }
                    } else {
                        Toast.makeText(applicationContext, "${task.exception.toString()}", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                })
            }
        }

    }
}
