package com.example.firebaseapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var passwordEdit:EditText
    private lateinit var emailEdit:EditText
    private lateinit var mAuth: FirebaseAuth
    private var selectedPhotoUri: Uri? = null
    private lateinit var mGoogleSignInClient:GoogleSignInClient
    val RC_SIGN_IN = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val email = emailText.text.toString()
        val password  = passwordText.text.toString()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        loginButton.setOnClickListener {
            signUpProgress()
        }
        selectPhoto_button.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        googleButton.setOnClickListener {
            signInGoogle()
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

            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            selected_imageView.setImageBitmap(bitmap)
            selectPhoto_button.alpha = 0f
//            var bitmapDrawable = BitmapDrawable(bitmap)
//            selectPhoto_button.setBackgroundDrawable(bitmapDrawable)
        }else if (requestCode == RC_SIGN_IN){
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                var account = task.result
                if(account !=  null) fireBaseAuthWithGoogle(account)
            }catch (e:ApiException){
                Log.d("APIException", e.toString())
            }
        }

    }
    private fun fireBaseAuthWithGoogle(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//        mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("currentUser", mAuth?.currentUser.toString())
            }
            else{

            }
        }
    }

    private fun signInGoogle(){
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(googleSignInIntent,RC_SIGN_IN)
    }

    private fun signUpProgress(){
        mAuth = FirebaseAuth.getInstance()
        val email = emailText.text.toString()
        val password  = passwordText.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
                return
        }
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

    private fun saveUserToFireBaseDatabase(imageUri: String){
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, imageUri)
        ref.setValue(user).addOnSuccessListener {
            Log.d("MainActivity", "saved")
        }
    }
}
