package com.example.attendify;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // UI Components
    private TextView userNameTextView;
    private TextView nameField;
    private TextView departmentField;
    private TextView emailField;
    private TextView contactField;
    private ImageButton themeToggle;
    private Button editProfileButton;
    private CircleImageView profileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://attendify-34e8f-default-rtdb.firebaseio.com/")
                .getReference();

        // Initialize views
        initializeViews();

        // Setup features
        setupThemeToggle();
        setupEditProfileButton();
        loadProfilePhoto();
        loadFacultyData();
    }

    private void initializeViews() {
        userNameTextView = findViewById(R.id.user_name);
        nameField = findViewById(R.id.name_field);
        departmentField = findViewById(R.id.department_field);
        emailField = findViewById(R.id.email_field);
        contactField = findViewById(R.id.contact_field);
        themeToggle = findViewById(R.id.theme_toggle);
        editProfileButton = findViewById(R.id.edit_profile_button);
        profileImage = findViewById(R.id.avatar);
    }

    private void loadProfilePhoto() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (currentUser != null) {
            // First try to get photo from Google Sign In
            if (user.getPhotoUrl() != null) {
                Picasso.get().load(user.getPhotoUrl()).into(profileImage);
            }
            // If Google photo is not available, try Firebase user photo
            else if (currentUser.getPhotoUrl() != null) {
                loadImageWithPicasso(currentUser.getPhotoUrl().toString());
            }
            // If no photo is available, load default image
            else {
                profileImage.setImageResource(R.drawable.ic_person);
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_person);
        }
    }

    private void loadImageWithPicasso(String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .error(R.drawable.ic_person)
                .placeholder(R.drawable.ic_person)
                .into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Profile image loaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error loading profile image", e);
                        profileImage.setImageResource(R.drawable.ic_person);
                    }
                });
    }

    private void setupThemeToggle() {
        themeToggle.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    private void setupEditProfileButton() {
        editProfileButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFacultyData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = currentUser.getEmail();
        if (userEmail == null) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Query facultyQuery = databaseRef.child("faculty").orderByChild("Email").equalTo(userEmail);
        facultyQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot facultyData = dataSnapshot.getChildren().iterator().next();

                    String name = getStringValue(facultyData.child("Name"));
                    String course = getStringValue(facultyData.child("Course"));
                    String email = getStringValue(facultyData.child("Email"));
                    String contact = getStringValue(facultyData.child("Contact"));
                    String username = getStringValue(facultyData.child("Username"));

                    updateUI(name, course, email, contact, username);
                } else {
                    Log.d(TAG, "No faculty data found for email: " + userEmail);
                    Toast.makeText(ProfileActivity.this,
                            "Faculty data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(ProfileActivity.this,
                        "Error loading faculty data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(String name, String course, String email, String contact, String username) {
        try {
            userNameTextView.setText(name);
            nameField.setText(name);
            departmentField.setText(course);  // Using Course instead of Department
            emailField.setText(email);
            contactField.setText(contact);
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
            Toast.makeText(this, "Error updating profile display", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringValue(DataSnapshot snapshot) {
        if (!snapshot.exists()) {
            return "Not available";
        }

        Object value = snapshot.getValue();
        return value != null ? value.toString() : "Not available";
    }
}