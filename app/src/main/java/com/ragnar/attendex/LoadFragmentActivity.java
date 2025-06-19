package com.ragnar.attendex;


import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoadFragmentActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    // Fragment instances
    private DashboardFragment dashboardFragment;
    private SubjectsFragment subjectsFragment;
    private NoticeBoardFragment noticeBoardFragment;
    private ProfileFragment profileFragment;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_fragment);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Setup bottom navigation
        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(getDashboardFragment(), "Dashboard");
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = "";

                int itemId = item.getItemId();

                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = getDashboardFragment();
                    tag = "Dashboard";
                } else if (itemId == R.id.nav_subjects) {
                    selectedFragment = getSubjectsFragment();
                    tag = "Subjects";
                } else if (itemId == R.id.nav_notice_board) {
                    selectedFragment = getNoticeBoardFragment();
                    tag = "NoticeBoard";
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = getProfileFragment();
                    tag = "Profile";
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, tag);
                    return true;
                }

                return false;
            }
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Hide all fragments first
        hideAllFragments(transaction);

        // Check if fragment is already added
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
        if (existingFragment != null) {
            transaction.show(existingFragment);
        } else {
            transaction.add(R.id.fragmentContainer, fragment, tag);
        }

        transaction.commit();

        // Update action bar title
        updateActionBarTitle(tag);
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (dashboardFragment != null && dashboardFragment.isAdded()) {
            transaction.hide(dashboardFragment);
        }
        if (subjectsFragment != null && subjectsFragment.isAdded()) {
            transaction.hide(subjectsFragment);
        }
        if (noticeBoardFragment != null && noticeBoardFragment.isAdded()) {
            transaction.hide(noticeBoardFragment);
        }
        if (profileFragment != null && profileFragment.isAdded()) {
            transaction.hide(profileFragment);
        }
    }

    private void updateActionBarTitle(String fragmentName) {
        if (getSupportActionBar() != null) {
            switch (fragmentName) {
                case "Dashboard":
                    getSupportActionBar().setTitle("Dashboard");
                    break;
                case "Subjects":
                    getSupportActionBar().setTitle("My Subjects");
                    break;
                case "NoticeBoard":
                    getSupportActionBar().setTitle("Notice Board");
                    break;
                case "Profile":
                    getSupportActionBar().setTitle("Profile");
                    break;
                default:
                    getSupportActionBar().setTitle("Attendance Manager");
            }
        }
    }

    // Singleton pattern for fragments to avoid recreation
    private DashboardFragment getDashboardFragment() {
        if (dashboardFragment == null) {
            dashboardFragment = new DashboardFragment();
        }
        return dashboardFragment;
    }

    private SubjectsFragment getSubjectsFragment() {
        if (subjectsFragment == null) {
            subjectsFragment = new SubjectsFragment();
        }
        return subjectsFragment;
    }

    private NoticeBoardFragment getNoticeBoardFragment() {
        if (noticeBoardFragment == null) {
            noticeBoardFragment = new NoticeBoardFragment();
        }
        return noticeBoardFragment;
    }

    private ProfileFragment getProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    @Override
    public void onBackPressed() {
        // If not on dashboard, navigate to dashboard
        super.onBackPressed();
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_dashboard) {
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        } else {
            // Show exit confirmation
            showExitConfirmation();
        }
    }

    private void showExitConfirmation() {
        // Simple toast for now - can be replaced with AlertDialog
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        // You can implement double back press to exit here
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is still authenticated
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up fragment references
        dashboardFragment = null;
        subjectsFragment = null;
        noticeBoardFragment = null;
        profileFragment = null;
    }

    // Public method to get current user (can be used by fragments)
    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    // Public method for fragments to update action bar title
    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    // Public method for fragments to show toast messages
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}