package com.myeamin.loginwithgoogledemoproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private CircleImageView profileImage;
    private TextView tvName, tvEmail, tvUid;
    private Button btnLogout, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        profileImage = findViewById(R.id.profile_image);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvUid = findViewById(R.id.tv_uid);
        btnLogout = findViewById(R.id.btn_logout);
        btnDelete = findViewById(R.id.btn_delete_account);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        });

        btnDelete.setOnClickListener(v -> deleteAccount());
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        } else {
            updateUI(user);
        }
    }

    private void updateUI(FirebaseUser user) {
        tvName.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());
        tvUid.setText("UID: " + user.getUid());

        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }
    }

    // Delete Firebase account
    private void deleteAccount() {
        if (user == null) return;

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User confirmed → delete account
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, Login.class));
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                Toast.makeText(MainActivity.this,
                                        "Please log in again to delete your account",
                                        Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                startActivity(new Intent(MainActivity.this, Login.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Delete failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User canceled → do nothing
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
}
