package com.example.training.myapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.prefs.Preferences;


public class SigninScreen extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SigninScreen";
    private FirebaseAuth mAuth;

    public String UID;
    String EventId;

    private FirebaseDatabase mDatabase;
    private DatabaseReference eRef;

    private EditText mEmailField;
    private EditText mPasswordField;
    private DatabaseReference myRef;
    private DatabaseReference qrRef;
    public static final String PREFS_NAME="usertype";
    public static final String PREFS_EVENTID="eventid";
    SharedPreferences preferences;

    // [START declare_auth]

    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_screen);

        preferences=getSharedPreferences(PREFS_NAME,MODE_PRIVATE);


        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        mEmailField.setText("ro@gmail.com");
        mPasswordField.setText("naseefro");

        qrRef=FirebaseDatabase.getInstance().getReference();
        eRef=FirebaseDatabase.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

        myRef = mDatabase.getReference("Users/"+UID+"/Eventid");

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
       // findViewById(R.id.sign_out_button).setOnClickListener(this);
       // findViewById(R.id.verify_email_button).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
    }
    // [END on_start_check_user]

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SigninScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UID=mAuth.getCurrentUser().getUid().toString();
                            myRef=mDatabase.getReference("Users/"+UID+"/type");

                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String type=dataSnapshot.getValue(String.class);

                                    SharedPreferences.Editor editor= preferences.edit();

                                    editor.putString("usertype",type);
                                    editor.commit();
                                    String Value=preferences.getString("usertype","");
                                    Toast.makeText(getApplicationContext(),Value,Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            myRef=mDatabase.getReference("Users/"+UID+"/Eventid");

                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    EventId=dataSnapshot.getValue(String.class);
                                    checkuser();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            String type = preferences.getString("usertype", "");



                            if(type.equals("Audiance")||type.equals("Speaker"))
                            {
                                Toast.makeText(getApplicationContext(),"audiance or speaker",Toast.LENGTH_LONG).show();


                                        //startActivityForResult(new Intent(SigninScreen.this,QRCodeReader.class),10007);

                                if(EventId==null)
                                {
                                    //startActivityForResult(new Intent(SigninScreen.this,QRCodeReader.class),10007);
                                }
                                else startActivity(new Intent(getApplicationContext(),MainActivity.class));

                            }



                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SigninScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10007)
        {
            String EventId=data.getStringExtra("result");
            qrRef.child("Users").child(UID).child("Eventid").setValue(EventId);
            qrRef.child("events").child(EventId).child("Audiance").push().setValue(UID);
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }

    //    private void sendEmailVerification() {
//        // Disable button
//        findViewById(R.id.verify_email_button).setEnabled(false);
//
//        // Send verification email
//        // [START send_email_verification]
//        final FirebaseUser user = mAuth.getCurrentUser();
//        user.sendEmailVerification()
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // [START_EXCLUDE]
//                        // Re-enable button
//                        findViewById(R.id.verify_email_button).setEnabled(true);
//
//                        if (task.isSuccessful()) {
//                            Toast.makeText(SigninScreen.this,
//                                    "Verification email sent to " + user.getEmail(),
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            Log.e(TAG, "sendEmailVerification", task.getException());
//                            Toast.makeText(SigninScreen.this,
//                                    "Failed to send verification email.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        // [END_EXCLUDE]
//                    }
//                });
//        // [END send_email_verification]
//    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            //mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
              //      user.getEmail(), user.isEmailVerified()));
            //mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            //findViewById(R.id.signed_in_buttons).setVisibility(View.VISIBLE);
          //  if(user.Eventid==null&& userType!==host){show qr code surafce}

//            if user.evert.id== or user== host show mainactivity

            // Read from the database

//
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    EventId = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "Value is: " + EventId);
                    if(EventId==null){
                       // startActivity(new Intent(getApplicationContext(),QRCodeReader.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });


//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);

           // findViewById(R.id.verify_email_button).setEnabled(!user.isEmailVerified());
        } else {
//            mStatusTextView.setText(R.string.signed_out);
  //          mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
           // findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
           /* createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());*/

           Intent intent = new Intent(SigninScreen.this, User_Registration.class);
           startActivity(intent);

        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());



        //} else if (i == R.id.sign_out_button) {
//            signOut();
//        } else if (i == R.id.verify_email_button) {
////            sendEmailVerification();
//        }
        }
    }

    public void checkuser()
    {
        String type = preferences.getString("usertype", "");

        if(type.equals("Audiance")||type.equals("Speaker"))
        {
            Toast.makeText(getApplicationContext(),"audiance or speaker",Toast.LENGTH_LONG).show();
            if(EventId==null)
            {
                startActivityForResult(new Intent(SigninScreen.this,QRCodeReader.class),10007);
            }
            else startActivity(new Intent(getApplicationContext(),MainActivity.class));

        }else
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }


}