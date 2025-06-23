package com.example.dsagame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dsagame.MainActivity;
import com.example.dsagame.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        progressBar = findViewById(R.id.progressBar);

        // Check if user is already logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startMainActivity();
            return;
        }

        findViewById(R.id.emailLoginButton).setOnClickListener(v -> startLogin());
        findViewById(R.id.googleLoginButton).setOnClickListener(v -> startLogin());
    }

    private void startLogin() {
        try {
            progressBar.setVisibility(View.VISIBLE);

            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            );

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false, true)
                            .setTheme(R.style.Theme_DSAGame)
                            .build(),
                    RC_SIGN_IN
            );
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error starting login: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("LoginActivity", "Login initialization error", e);
        }
    }

//    private void startEmailLogin() {
//        // Replace FirebaseUI with direct Firebase Auth
//        String email = "test@example.com";
//        String password = "password123";
//
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        startMainActivity();
//                    } else {
//                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressBar.setVisibility(View.GONE);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    startMainActivity();
                }
            } else {
                // Sign in failed
                if (response != null) {
                    Toast.makeText(this, "Login failed: " + response.getError(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}