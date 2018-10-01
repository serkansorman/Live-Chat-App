package com.example.serkansorman.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class GoogleAuthentication extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth auth;
    private final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d("GoogleAuthentication", "User logged in: " + user.getEmail());
                    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                    findViewById(R.id.logout).setVisibility(View.VISIBLE);
                    findViewById(R.id.passToRoom).setVisibility(View.VISIBLE);

                }else{
                    Log.d("GoogleAuthentication", "User Logged out");
                    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.logout).setVisibility(View.GONE);
                    findViewById(R.id.passToRoom).setVisibility(View.GONE);

                }
            }
        };

        //Google Sign in Options Yapılandırması
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.WEB_ID))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(GoogleAuthentication.this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(GoogleAuthentication.this);
                progressDialog.setTitle("Signing in...");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                signIn();
            }
        });



        findViewById(R.id.passToRoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = getSharedPreferences("MyPref", MODE_PRIVATE).edit();
                editor.putString("userName",auth.getCurrentUser().getEmail().split("@")[0]);
                editor.apply();

                finish();
                startActivity(new Intent(getApplicationContext(),RoomList.class));

            }
        });


    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }


    public void logOut(View view) {
        auth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In basarili oldugunda Firebase ile yetkilendir
                Log.i("GoogleAuthentication", "Google Sign In Success.");
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In hatası.
                Log.e("GoogleAuthentication", "Google Sign In Fail.");
            }
        }
    }




    //GoogleSignInAccount nesnesinden ID token'ı alıp, bu Firebase güvenlik referansını kullanarak
    // Firebase ile yetkilendirme işlemini gerçekleştiriyoruz
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        // Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful())
                        Log.w("GoogleAuthentication", "signInWithCredential", task.getException());
                    else {
                        Log.d("GoogleAuthentication", "signInWithCredential:onComplete:" + task.isSuccessful());
                        Toast.makeText(getApplicationContext(), "Signed in Successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
    }

    @Override
    public void onStart() {
        super.onStart();
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null)
            auth.removeAuthStateListener(mAuthListener);

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("GoogleAuthentication", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }



}
