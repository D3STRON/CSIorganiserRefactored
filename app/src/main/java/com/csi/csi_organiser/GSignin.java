package com.csi.csi_organiser;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class GSignin extends AppCompatActivity {
    private SignInButton mGoogleBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,firebaserole;
    private static final int RC_SIGN_IN = 2;
    private GoogleApiClient mGoogleApiClient;
    FirebaseAuth.AuthStateListener mAuthListener;
    public static final String  TAG = "Main Activity";
    public static String personEmail2;
SQLiteHelper db= new SQLiteHelper(this);
    HashMap<String,String> users;
    ArrayList<Model2> memList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        users= db.getAllValues();
        if(getIntent().getBooleanExtra("EXIT",false))
        {
            finish();
        }
         else if(!users.isEmpty())
        {
            Toast.makeText(GSignin.this,"There is a current User!",Toast.LENGTH_LONG).show();
            if(users.get("priority").matches("1"))
            {
                //Intent intent= new Intent(HomeActivity.this,CoreActivity.class);
                //startActivity(intent);

            }
            else if(users.get("priority").matches("2"))
            {
                Intent intent= new Intent(GSignin.this,JcActivity.class);
                startActivity(intent);
            }
            else
            {

                Intent intent =new Intent(GSignin.this,Members.class);
                startActivity(intent);
            }

        }
        else {

            mGoogleBtn = (SignInButton) findViewById(R.id.sign_in_button);
            mAuth = FirebaseAuth.getInstance();
            memList = new ArrayList<>();
            firebaserole = FirebaseDatabase.getInstance().getReference("Roles");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("CSI Members");

            mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        final String uuid = user.getUid();
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Model model = dataSnapshot.child(uuid).getValue(Model.class);
                                if (model == null) {
                                    Intent intent = new Intent(GSignin.this, HomeActivity.class);
                                    intent.putExtra("message", user.getEmail());
                                    startActivity(intent);
                                } else if (model != null && model.getPriority().equals("0")){
                                    startActivity(new Intent(GSignin.this, Members.class));}

                                else {
                                    db.addInfo(model.getCurrenttask(), model.getName(),model.getEmail(),
                                            model.getNumber(),model.getNeareststation(),model.getNumberoftasks(),
                                            model.getPreference1(),model.getPreference2(),model.getPreference3(),
                                            model.getPriority(),model.getRollno(),model.Id);
                                    startActivity(new Intent(GSignin.this, JcActivity.class).putExtra("model",model));}
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                  Log.e(TAG, databaseError.getMessage());
                            }
                        });
                    }
                }
            });



            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Toast.makeText(GSignin.this, "You got an error", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            mGoogleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });
        }  Toast.makeText(GSignin.this, "Authentication failed.",  Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount acct = result.getSignInAccount();
            //personEmail2 = acct.getEmail();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                personEmail2 = account.getEmail();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //if(databaseReference.child(user.getUid()) == null)

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(GSignin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
    }

    public boolean isConnected(Context context)
    {

        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo= cm.getActiveNetworkInfo();
        if(netinfo!=null && netinfo.isConnectedOrConnecting())
        {
            android.net.NetworkInfo wifi= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile!=null && mobile.isConnectedOrConnecting())|| (wifi!=null && wifi.isConnectedOrConnecting()))
            {
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

}