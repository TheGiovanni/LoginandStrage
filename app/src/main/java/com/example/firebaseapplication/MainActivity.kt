package com.example.firebaseapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var passwordEdit:EditText
    private lateinit var emailEdit:EditText
    private lateinit var mAuth: FirebaseAuth
    private var selectedPhotoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        loginButton.setOnClickListener {
            mAuth.createUserWithEmailAndPassword(emailText.text.toString(),passwordText.text.toString()).addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "ok",Toast.LENGTH_LONG).show()
                    uploadImageToFireBaseStorage()
//                    var intent = Intent(this@MainActivity, LoginActivity::class.java)
//                    startActivity(intent)
//                    user?.sendEmailVerification()?.addOnCompleteListener { confirm ->
//                        if (confirm.isSuccessful){
//                            Toast.makeText(this, "Verifyok",Toast.LENGTH_LONG).show()
//                        }
//                    }

                }else{
                    Toast.makeText(this, "No",Toast.LENGTH_LONG).show()
                }
            }
        }
        selectPhoto_button.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }




        
//        var mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
//        val fireLogBundle = Bundle()
//        fireLogBundle.putString("TEST", "FireSample app MainActivity.onCreate() is called.")
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, fireLogBundle)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data

            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            var bitmapDrawable = BitmapDrawable(bitmap)
            selectPhoto_button.setBackgroundDrawable(bitmapDrawable)
        }
    }
    private fun uploadImageToFireBaseStorage(){
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("MaicdnActivity", it.metadata?.path)

            ref.downloadUrl.addOnSuccessListener {
                saveUserToFireBaseDatabase(it.toString())
            }
        }
    }

    private fun saveUserToFireBaseDatabase(uri: String){

    }
}
