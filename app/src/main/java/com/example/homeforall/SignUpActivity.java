package com.example.homeforall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static com.example.homeforall.AppConstants.OWNER;
import static com.example.homeforall.AppConstants.OWNER_PHONE;

//  Sign Up activity has 2 uses
//  First is to create a new user
//  Second to edit user data from a profile activity(edit mode)
public class SignUpActivity extends AppCompatActivity {
    //Firebase variables
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersDatabaseReference;
    //User data fields
    private EditText emailEditText;
    private EditText passwordEditText, confirmPasswordEditText;
    private EditText phoneNum, phoneNum2;
    private EditText nameEditText;
    private LinearLayout newPassword,existPassword;
    private Button signUpButton;
    private User user;
    private CheckBox changePassword;
    private boolean editMode;
    private boolean authSuccess;
    private boolean phoneChanged;
    private boolean nameChanged;
    private ProgressBar progressBar;
    private String name,phone1,phone2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("יצירת חשבון");
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersDatabaseReference = database.getReference().child("users");
        emailEditText = findViewById(R.id.emailEditTxt);
        passwordEditText = findViewById(R.id.passwordEditTxt);
        confirmPasswordEditText = findViewById(R.id.passwordConfirmEditTxt);
        nameEditText = findViewById(R.id.nameEditTxt);
        phoneNum = findViewById(R.id.phoneEditTxt);
        phoneNum2 = findViewById(R.id.phone2EditTxt);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar=findViewById(R.id.progressBarSignUp);
        Intent intent=getIntent();
        user=(User)intent.getSerializableExtra("user");
        //Edit mode
        if(user!=null){
           editMode=true;
           initFields();
            setTitle("עדכון פרטי בעל החשבון");
           signUpButton.setText(R.string.edit_data);
           newPassword=findViewById(R.id.newPasswordLayout);
           newPassword.setVisibility(View.GONE);
           existPassword=findViewById(R.id.existPasswordLayout);
           existPassword.setVisibility(View.GONE);
           changePassword=findViewById(R.id.passwordChangeCheckBox);
           changePassword.setVisibility(View.VISIBLE);
           changePassword.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(changePassword.isChecked()) {
                       confirmPasswordEditText.setHint("סיסימא חדשה");
                       newPassword.setVisibility(View.VISIBLE);
                       passwordEditText.setHint("סיסמא נוכחית");
                       existPassword.setVisibility(View.VISIBLE);
                   }
                   else {
                       newPassword.setVisibility(View.GONE);
                       existPassword.setVisibility(View.GONE);
                   }


               }
           });
        }

        //This button has 2 uses Sign up  OR Edit user data
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               signUp();
            }
        });
    }

    //
    private boolean inputValidation() {
        String email,password,passwordToConfirm;
        name=nameEditText.getText().toString().trim();
        phone1 = phoneNum.getText().toString().trim();
        phone2 =phoneNum2.getText().toString().trim();

        //Sign up mode
        if(!editMode) {
            //Name Validation
            if(name.isEmpty()){
                Toast.makeText(SignUpActivity.this, "Name field can't be empty", Toast.LENGTH_LONG).show();
                return false;
            }
            //Email Validation
            email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Email field is empty", Toast.LENGTH_LONG).show();
                return false;
              }
            //Password Validation
            password = passwordEditText.getText().toString().trim();
            passwordToConfirm = confirmPasswordEditText.getText().toString().trim();
            if(!passwordValidation(password,passwordToConfirm))
                return false;

            //Phone number validation
            return phoneNumberValidation(phone1,phone2);
          }//If it sign up mode this block will return answer

        //Edit mode user can't erase name and leave empty field
        if(name.isEmpty()){
            Toast.makeText(SignUpActivity.this, "Name field can't be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        //Edit mode User want to change a password
        boolean changePasswordRequest=changePassword.isChecked();
        if( changePasswordRequest) {
             password = passwordEditText.getText().toString().trim();
             passwordToConfirm = confirmPasswordEditText.getText().toString().trim();
             if(!passwordValidation(password,passwordToConfirm))
                 return false;

        }
        //Edit mode user changed a name
        if(!user.getName().equals(name))
            nameChanged=true;

        //Edit mode user changed a phone number
        if(!phone1.equals(user.getPhone1()) || !phone2.equals(user.getPhone2()) )
            return phoneNumberValidation(phone1,phone2);
        return true;


    }

    private boolean phoneNumberValidation(String phonePrimary, String phone2) {
        //Primary phone number can't be empty
        if (phonePrimary.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Phone field is empty", Toast.LENGTH_LONG).show();
            return false;
        }
        //Number must start from zero or plus sign
        Character primeNumFirstChar = phonePrimary.charAt(0);
        if (!(primeNumFirstChar == '0' || primeNumFirstChar == '+')) {
            Toast.makeText(SignUpActivity.this, "Illegal phone number", Toast.LENGTH_LONG).show();
            return false;
        }
        //Number can contain only digits
        else if (!TextUtils.isDigitsOnly(phonePrimary)) {
            Toast.makeText(SignUpActivity.this, "Phone number must be numeric", Toast.LENGTH_LONG).show();
            return false;
        }
        // Second phone number can be empty
        // But if it isn't empty number should pass same validation
        else if (phone2 != null) {
            if (!(primeNumFirstChar == '0' || primeNumFirstChar == '+'))
            {
                Toast.makeText(SignUpActivity.this, "Illegal phone 2 number", Toast.LENGTH_LONG).show();
                return false;
            }
            else if (!TextUtils.isDigitsOnly(phonePrimary))
            {
                Toast.makeText(SignUpActivity.this, "Phone 2 number must be numeric", Toast.LENGTH_LONG).show();
                return false;
            }

        }
        phoneChanged=true;
        return true;
    }

    private void signUp(){
        //Illegal user data
        if(!inputValidation())
            return;
        //Entering to edit mode
        if(editMode){
            FirebaseUser user = auth.getCurrentUser();
            //User want to change password
            if(authSuccess) {
                String newPassword = confirmPasswordEditText.getText().toString().trim();
                disableUI();
                user.updatePassword(newPassword)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Successfully change user password ", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(SignUpActivity.this, "Failed update password ", Toast.LENGTH_LONG).show();
                                    enableUI();
                                    return;
                                }
                            }
                        });
            }
            //Phone or Name fields has been change
            if(phoneChanged || nameChanged){
                DatabaseReference currentUser=usersDatabaseReference.child(user.getUid());
                if(nameChanged)
                    currentUser.child("name").setValue(name);
                if(phoneChanged){
                    currentUser.child("phone1").setValue(phone1);
                    currentUser.child("phone2").setValue(phone2);
                }
                updateAdData();
            }
        }

        //Sign up mode creating new user
        else {
        String email=emailEditText.getText().toString().trim();
        String password=passwordEditText.getText().toString().trim();
        auth.createUserWithEmailAndPassword(email,password ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                     disableUI();
                     createUser(user);
                     Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                     startActivity(intent);
                }
                //Failed to create user
                else{
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                    switch (errorCode) {

                        case "ERROR_INVALID_EMAIL":
                            Toast.makeText(SignUpActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                            break;


                        case "ERROR_EMAIL_ALREADY_IN_USE":
                            Toast.makeText(SignUpActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                            break;

                        default:
                            Toast.makeText(SignUpActivity.this, "Failed to sign up in.", Toast.LENGTH_LONG).show();


                    }
                }}});}
    }

    //Sign up mode insert a new user to database
    private void createUser(FirebaseUser firebaseUser) {
        User user=new User();
        user.setEmail(firebaseUser.getEmail());
        user.setName(nameEditText.getText().toString().trim());
        user.setPhone1(phoneNum.getText().toString().trim());
        user.setPhone2(phoneNum2.getText().toString().trim());
        usersDatabaseReference.child(firebaseUser.getUid()).setValue(user);
    }

    //On edit mode method initialize user data fields
    private void initFields(){
        emailEditText.setText(user.getEmail());
        emailEditText.setEnabled(false);
        nameEditText.setText(user.getName()) ;
        phoneNum.setText(user.getPhone1());
       if (user.getPhone2()!=null)
           phoneNum2.setText(user.getPhone2());
    }

    private boolean passwordValidation(final String newExistPassword, String confirmNewPassword) {

           //Sign up mode input validation
           if(!editMode && !basicPasswordValidation(newExistPassword))
               return false;
           // Sign up mode compare passwords
           else if (!editMode && !newExistPassword.equals(confirmNewPassword)) {
                Toast.makeText(SignUpActivity.this, "different passwords", Toast.LENGTH_LONG).show();
                return false;
            }
            //End on sign up validation password are ok
            if(!editMode  )
                return true;
            //Edit mode user want to change password
            if(!authSuccess) {
                String email=user.getEmail();
                FirebaseUser user = auth.getCurrentUser();
                //Account protection
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, newExistPassword);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    authSuccess=true;
                                }
                                else
                                    Toast.makeText(SignUpActivity.this, "Authentication failed ", Toast.LENGTH_LONG).show();
                            }
                        });
            }
            //Reauthenticate passed successfully validate a new password
            if(authSuccess)
                return basicPasswordValidation(confirmNewPassword);
            return false;


    }

    private boolean basicPasswordValidation(String password) {
        if (password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Password field is empty", Toast.LENGTH_LONG).show();
            return false;
        } else if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this, "Password length has to be minimum 6 characters", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

   // If User changed his data it is necessary update a ad connection data
    private void updateAdData(){
        DatabaseReference userAdRef=usersDatabaseReference.child(auth.getUid()).child("user_ads");
        userAdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String adId,animalKind;
                DatabaseReference adRef=database.getReference().child("ads");
                //Change data in all users ads
                for(DataSnapshot child : dataSnapshot.getChildren() ){
                    adId=child.getKey();
                    animalKind=(String) child.getValue();
                    if(nameChanged)
                        adRef.child(animalKind).child(adId).child(OWNER).setValue(name);
                    if(phoneChanged)
                        adRef.child(animalKind).child(adId).child(OWNER_PHONE).setValue(phone1+"\n"+phone2);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }

        });

    }

    //Disable user interface to protect user change or click data during update a DB
    private void disableUI(){
        nameEditText.setEnabled(false);
        emailEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        confirmPasswordEditText.setEnabled(false);
        phoneNum.setEnabled(false);
        phoneNum2.setEnabled(false);
        signUpButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void enableUI(){
        nameEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        if(!editMode)
          emailEditText.setEnabled(true);
        confirmPasswordEditText.setEnabled(true);
        phoneNum.setEnabled(true);
        phoneNum2.setEnabled(true);
        signUpButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

}
