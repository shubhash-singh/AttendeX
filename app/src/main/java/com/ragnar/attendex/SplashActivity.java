package com.ragnar.attendex;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar for splash screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views (following nameEditText convention)
        // Views would be initialized here if needed

        // Check permissions and internet connectivity
        checkPermissionsAndProceed();
    }

    private void checkPermissionsAndProceed() {
        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] newPermissions = new String[permissions.length + 1];
            System.arraycopy(permissions, 0, newPermissions, 0, permissions.length);
            newPermissions[permissions.length] = Manifest.permission.POST_NOTIFICATIONS;
            permissions = newPermissions;
        }

        if (hasAllPermissions()) {
            checkInternetAndProceed();
        } else {
            requestPermissions();
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                checkInternetAndProceed();
            } else {
                Toast.makeText(this, "Permissions are required for the app to function properly", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void checkInternetAndProceed() {
        if (isInternetAvailable()) {
            proceedToLogin();
        } else {
            Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show();
            // Still proceed to login after showing warning
            proceedToLogin();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void proceedToLogin() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}