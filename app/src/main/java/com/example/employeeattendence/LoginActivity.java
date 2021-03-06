package com.example.employeeattendence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.employeeattendence.model.Student;
import com.example.employeeattendence.util.Constants;
import com.example.employeeattendence.util.MainActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import util.MainActivity;

public class LoginActivity extends AppCompatActivity {
    EditText userEmail,userPass;
    Button loginButton,signupButton,forgotPasswordButton,phoneVerifyButton;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    public CallbackManager mCallbackManager;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference(Constants.STUDENT);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        userEmail = findViewById(R.id.log_username);
        userPass = findViewById(R.id.log_password);
        loginButton = findViewById(R.id.loginuser);
        signupButton = findViewById(R.id.signupuser);
        forgotPasswordButton = findViewById(R.id.forgotpassword);

        phoneVerifyButton = findViewById(R.id.log_phoneverify);
        mAuth = FirebaseAuth.getInstance();

        pref = getSharedPreferences("prefs",MODE_PRIVATE);
        editor = pref.edit();




        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });



        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ForgotActivity.class);
                startActivity(intent);
            }
        });



        phoneVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,PhoneVerifyActivity.class);
                startActivity(intent);
            }
        });



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String email,pass;
            email = userEmail.getText().toString();
            pass = userPass.getText().toString();
                if(TextUtils.isEmpty(email)){
                    userEmail.setError("Please enter Email");
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    userPass.setError("Please enter Password");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Welcome : "+user.getEmail(), Toast.LENGTH_SHORT).show();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                                    updateUI(null);

                                }

                                // ...
                            }
                        });
            }
        });
    }

    //public void login(View view){
    //
    //        String email,pass;
    //        email = e1.getText().toString();
    //        pass = e2.getText().toString();
    //        if(TextUtils.isEmpty(email)){
    //            e1.setError("Please enter email");
    //            return;
    //        }
    //        if(TextUtils.isEmpty(pass)){
    //            e2.setError("Please enter pass");
    //            return;
    //        }
    //        fireLogin(email,pass);
    //    }


    public  void onfblogin(View view) {
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
// ...

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void getUserData(FirebaseUser user){
        myRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Student s = dataSnapshot.getValue(Student.class);
                editor.putString(Constants.NAME,s.getName());
                editor.putString(Constants.EMAIL,s.getEmail());
                editor.putString(Constants.COURSE,s.getCourse());
                editor.commit();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Student details error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUI(FirebaseUser user){
        if(user==null){
            getUserData(user);

        }
        //else{
          //  Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            //startActivity(intent);
        //}
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
//public void singUp(View view){
    //        Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
    //        startActivity(intent);
    //    }
}
