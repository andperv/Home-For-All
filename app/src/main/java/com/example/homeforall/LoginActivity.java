package com.example.homeforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//From this activity it is possible to login on exist account or click on newUser
// that will transfer to signUp activity to create new account
public class LoginActivity extends AppCompatActivity {
    //UI var
    private EditText emailEditTxt;
    private EditText passwordEditTxt;
    private Button loginBut;
    private TextView newUser;
    private ProgressBar progressBar;
    //Firebase var
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userDbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("כניסה למערכת");
        //Firebase var
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        userDbRef=database.getReference().child("users");
        //UI views
        emailEditTxt=findViewById(R.id.emailEditTxtLoginActivity);
        passwordEditTxt=findViewById(R.id.passwordEditTxtLoginActivity);
        loginBut=findViewById(R.id.loginBut);
        newUser=findViewById(R.id.toggleToSignUp);
        progressBar=findViewById(R.id.progressBarLogin);
        //if user didn't sign out go direct to his profile activity
        if(auth.getCurrentUser() !=null) {
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));}

        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        //New User need to create account
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    private void login() {
        if(!inputValidation())
            return;
        // Login to exist account
        String email=emailEditTxt.getText().toString().trim();
        String password=passwordEditTxt.getText().toString().trim();
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete( Task<AuthResult> task) {
                            //Successfully login to account
                            if(task.isSuccessful()){
                                //Disable all fields and buttons to avoid app crashing
                                disableUI();
                                Toast.makeText(LoginActivity.this,"Login  completed!",Toast.LENGTH_LONG).show();
                                 Intent intent=new Intent(LoginActivity.this, ProfileActivity.class);
                                 startActivity(intent);
                            }
                           //Failed to login in, creating a error message
                            else{
                               try{
                                   String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();


                                switch (errorCode) {

                                    case "ERROR_INVALID_EMAIL":
                                        Toast.makeText(LoginActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "ERROR_WRONG_PASSWORD":
                                        Toast.makeText(LoginActivity.this, "The password is invalid or the user is incorrect.", Toast.LENGTH_LONG).show();
                                        break;


                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        Toast.makeText(LoginActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                        break;

                                    default:
                                        Toast.makeText(LoginActivity.this, "Failed to login in. "+errorCode, Toast.LENGTH_LONG).show();


                                }}//end of try
                                catch (Exception e){
                                    Toast.makeText(LoginActivity.this, "Failed to login "+e.getLocalizedMessage() , Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

           }





    private boolean inputValidation() {
        String email = emailEditTxt.getText().toString().trim();
        String password = passwordEditTxt.getText().toString().trim();

        //Email validation
        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email field is empty", Toast.LENGTH_LONG).show();
            return false;
        }

        // Basic Password validation
        else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Password field is empty", Toast.LENGTH_LONG).show();
            return false;
        } else if (password.length() < 6) {
            Toast.makeText(LoginActivity.this, "Password length has to be minimum 6 characters", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    //Disable user interface to protect user change or click data during access a DB
    private void disableUI(){
        emailEditTxt.setEnabled(false);
        passwordEditTxt.setEnabled(false);
        newUser.setEnabled(false);
        loginBut.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }


}
