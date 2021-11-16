package com.piford.amessenger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText etPhoneNumber, etOTP;
    Button btnSendOtp, btnVerifyOTP;
    LinearLayout llRequest, llVerify;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);

        auth = FirebaseAuth.getInstance ( );
        etPhoneNumber = findViewById (R.id.etPhoneNumber);
        etOTP = findViewById (R.id.etOtp);
        btnSendOtp = findViewById (R.id.btnSendOTP);
        btnVerifyOTP = findViewById (R.id.btnVerifyOtp);
        llRequest = findViewById (R.id.llRequest);
        llVerify = findViewById (R.id.llVerify);
        btnSendOtp.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                sendOTP ( );
            }
        });
        btnVerifyOTP.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick (View view) {
                verify ( );
            }
        });


    }

    private void sendOTP () {
        String phone = etPhoneNumber.getText ( ).toString ( ).trim ( );
        if (phone.isEmpty ( ) || phone.length ( ) != 10) {
            Toast.makeText (this, "Invalid phone number", Toast.LENGTH_SHORT).show ( );
            return;
        }
        phone = "+91" + phone;
        PhoneAuthProvider.getInstance ( ).verifyPhoneNumber (
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks ( ) {
                    @Override
                    public void onVerificationCompleted (PhoneAuthCredential credential) {
                        //Login using above credentials
                        signInWithPhoneAuthCredential (credential);
                    }

                    @Override
                    public void onCodeSent (String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent (verificationId, forceResendingToken);
                        LoginActivity.this.verificationId = verificationId;
                        LoginActivity.this.forceResendingToken = forceResendingToken;
                        llRequest.setVisibility (View.GONE);
                        llVerify.setVisibility (View.VISIBLE);
                        Toast.makeText (LoginActivity.this, "Otp Sent", Toast.LENGTH_SHORT).show ( );

                    }

                    @Override
                    public void onVerificationFailed (FirebaseException e) {
                        Toast.makeText (LoginActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show ( );
                    }
                });        // OnVerificationStateChangedCallbacks


    }

    private void verify () {
        String otp = etOTP.getText ( ).toString ( ).trim ( );
        if (otp.isEmpty ( )) {
            Toast.makeText (LoginActivity.this, "Enter otp", Toast.LENGTH_SHORT).show ( );
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential (verificationId, otp);
        //Login above credentials

        signInWithPhoneAuthCredential (credential);

    }

    private void signInWithPhoneAuthCredential (PhoneAuthCredential credential) {
        FirebaseAuth.getInstance ( ).signInWithCredential (credential)
                .addOnCompleteListener (this, new OnCompleteListener<AuthResult> ( ) {
                    @Override
                    public void onComplete (@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful ( )) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult ( ).getUser ( );

                            checkExistingLogin (user);
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException ( ) instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            Toast.makeText (LoginActivity.this, "Error: " + task.getException ( ), Toast.LENGTH_SHORT).show ( );
                        }
                    }
                });
    }


    @Override
    protected void onStart () {
        super.onStart ( );

        FirebaseUser user = FirebaseAuth.getInstance ( ).getCurrentUser ( );
        checkExistingLogin (user);
    }

    private void checkExistingLogin (FirebaseUser user) {
        if (user != null) {
            Intent i = new Intent (LoginActivity.this, UserDetailActivity.class);
            startActivity (i);
            Toast.makeText (LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show ( );
            finish ( );
        }

    }
}
