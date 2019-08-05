package com.example.android.travelmantics_firebase;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FirebaseUtil {

    static FirebaseDatabase sFirebaseDatabase;
    static DatabaseReference sDatabaseReference;
    private static FirebaseUtil sFirebaseUtil;
    private static FirebaseAuth sFirebaseAuth;
    private static FirebaseAuth.AuthStateListener sAuthListener;
    private static FirebaseStorage sFirebaseStorage;
    static StorageReference sStorageReference;

    static List<TravelDeal> sDeals;
    private static Activity mCallerActivity;
    private static final int RC_SIGN_IN = 123;

    private FirebaseUtil() {

    }

    static void openFirebaseReference(String ref, final Activity callerActivity) {
        if (sFirebaseUtil == null) {
            sFirebaseUtil = new FirebaseUtil();
            sFirebaseDatabase = FirebaseDatabase.getInstance();
            sFirebaseAuth = FirebaseAuth.getInstance();

            mCallerActivity = callerActivity;

            sAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) // show sign in screen only if there is no current login
                        signIn();
//                    Toast.makeText(callerActivity, "Welcome back!", Toast.LENGTH_SHORT).show();
                }
            };
        }
        sDeals = new ArrayList<>();
        sDatabaseReference = sFirebaseDatabase.getReference().child(ref);

        connectStorage(); // connect firebase storage
    }

    private static void signIn() {

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("40265211786-3arqngc2pg4qj22vvbteq5hnnsd53cp1.apps.googleusercontent.com")
//                .requestServerAuthCode("40265211786-3arqngc2pg4qj22vvbteq5hnnsd53cp1.apps.googleusercontent.com")
//                .requestEmail()
//                .requestId()
//                .build();

//        // Choose authentication providers
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build(),
//                new AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(gso).build());

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        mCallerActivity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    static void attachListener() {
        sFirebaseAuth.addAuthStateListener(sAuthListener);
    }

    static void detachListener() {
        sFirebaseAuth.removeAuthStateListener(sAuthListener);
    }

    static void connectStorage() {
        sFirebaseStorage = FirebaseStorage.getInstance();
        sStorageReference = sFirebaseStorage.getReference().child("deals_pictures"); // deals_pictures is a storage folder in firebase

    }

}
