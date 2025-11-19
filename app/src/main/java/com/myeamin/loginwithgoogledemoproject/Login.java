package com.myeamin.loginwithgoogledemoproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.UUID;

public class Login extends AppCompatActivity {

    private Button loginButton;

    private CredentialManager credentialManager;
    private GetCredentialRequest getCredentialRequest;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        // REQUIRED OPTION #1 — one-tap fallback
        GetGoogleIdOption googleIdOption =
                new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(R.string.web_client_id))
                        .build();

        // REQUIRED OPTION #2 — Google sign-in popup
        GetSignInWithGoogleOption signInOption =
                new GetSignInWithGoogleOption.Builder(getString(R.string.web_client_id))
                        .setNonce(UUID.randomUUID().toString())
                        .build();

        // Add BOTH options
        getCredentialRequest =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .addCredentialOption(signInOption)
                        .build();

        loginButton.setOnClickListener(v -> requestLoginWithGoogle());
    }


    private void requestLoginWithGoogle() {

        credentialManager.getCredentialAsync(
                this,
                getCredentialRequest,
                null,
                Runnable::run,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {

                        try {
                            Credential credential = response.getCredential();

                            GoogleIdTokenCredential googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(
                                            credential.getData()
                                    );

                            String idToken = googleIdTokenCredential.getIdToken();

                            AuthCredential authCredential =
                                    GoogleAuthProvider.getCredential(idToken, null);


                            mAuth.signInWithCredential(authCredential)
                                    .addOnSuccessListener(authResult -> {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(Login.this, "Firebase Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );

                        } catch (Exception e) {
                            Toast.makeText(Login.this, "Error Parsing Credential: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Toast.makeText(Login.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
