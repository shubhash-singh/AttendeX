package com.ragnar.attendex;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailOrRollEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private ProgressBar loginProgressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set click listeners
        setClickListeners();

        // Check if user is already logged in
        checkCurrentUser();
    }

    private void initializeViews() {
        emailOrRollEditText = findViewById(R.id.emailOrRollEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loginProgressBar = findViewById(R.id.loginProgressBar);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPasswordTextView.setOnClickListener(v -> handleForgotPassword());
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            navigateToMainActivity();
        }
    }

    private void attemptLogin() {
        String emailOrRoll = emailOrRollEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (!validateInput(emailOrRoll, password)) {
            return;
        }

        showProgressBar(true);

        // Check if input is email or roll number
        if (isValidEmail(emailOrRoll)) {
            // Direct email login
            loginWithEmail(emailOrRoll, password);
        } else {
            // Roll number - need to get email from Firestore
            loginWithRollNumber(emailOrRoll, password);
        }
    }

    private boolean validateInput(String emailOrRoll, String password) {
        if (TextUtils.isEmpty(emailOrRoll)) {
            emailOrRollEditText.setError("Email or Roll Number is required");
            emailOrRollEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String input) {
        return Patterns.EMAIL_ADDRESS.matcher(input).matches();
    }

    private void loginWithEmail(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgressBar(false);
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Please verify your email first", Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut();
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginWithRollNumber(String rollNumber, String password) {
        // Query Firestore to get email associated with roll number
        firestore.collection("students")
                .whereEqualTo("roll", rollNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String email = document.getString("email");
                        if (email != null) {
                            loginWithEmail(email, password);
                        } else {
                            showProgressBar(false);
                            Toast.makeText(this, "Email not found for this roll number", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showProgressBar(false);
                        Toast.makeText(this, "Roll number not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleForgotPassword() {
        String emailOrRoll = emailOrRollEditText.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrRoll)) {
            Toast.makeText(this, "Please enter your email or roll number first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isValidEmail(emailOrRoll)) {
            sendPasswordResetEmail(emailOrRoll);
        } else {
            // Get email from roll number first
            firestore.collection("students")
                    .whereEqualTo("roll", emailOrRoll)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String email = document.getString("email");
                            if (email != null) {
                                sendPasswordResetEmail(email);
                            } else {
                                Toast.makeText(this, "Email not found for this roll number", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Roll number not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showProgressBar(boolean show) {
        if (show) {
            loginProgressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
        } else {
            loginProgressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, LoadFragmentActivity.class);
        startActivity(intent);
        finish();
    }
}