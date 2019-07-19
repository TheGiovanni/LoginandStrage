package com.example.firebaseapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity :AppCompatActivity(){
    private lateinit var mAuth:FirebaseAuth
//    private lateinit var currentUser:FirebaseUser?
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        loginButton.setOnClickListener {
            mAuth.signInWithEmailAndPassword(emailText.text.toString(), passwordText.text.toString()).addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Login ok", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "No Login", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
//        currentUser = mAuth.currentUser
    }
}