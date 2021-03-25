package com.example.messanger.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.messanger.MainSite.MainSite;
import com.example.messanger.R;
import com.example.messanger.Register.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private EditText loginInput, passwordInput;
    private Button loginBtn;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.progresLogin);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor("#C83030")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final TextView register = findViewById(R.id.tvRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            }
        });

        loginInput = findViewById(R.id.etLoginInput);
        passwordInput = findViewById(R.id.etPasswordInput);
        loginBtn = findViewById(R.id.log);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                if (loginInput.getText().toString().equals("") && passwordInput.getText().toString().equals("")) {
                    loginInput.setError("Field cannot be empty");
                    passwordInput.setError("Field cannot be empty");
                } else if (loginInput.getText().toString().equals("")) {
                    loginInput.setError("Field cannot be empty");
                } else if (passwordInput.getText().toString().equals("")) {
                    passwordInput.setError("Field cannot be empty");
                } else {
                    login(loginInput.getText().toString(), passwordInput.getText().toString());
                }
            }
        });
    }

    /**
     * Log in with firebase authenticator if everything is ok run loadMainSite(FirebaseUser firebaseUser) and put in this method actual logged user
     * in the other case show Toast message
     * Input from login field @param loginField
     * Input from password field @param passwordField
     */
    private void login(String loginField, String passwordField) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(loginField, passwordField)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                            progressBar.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    loadMainSite(firebaseUser);
                                }
                            }, 3000);
                        } else {
                            progressBar.setVisibility(View.VISIBLE);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Wrong login or password", Toast.LENGTH_SHORT).show();
                                }
                            }, 2000);
                        }
                    }
                });
    }




    /**
     * Get Email from user
     * Create new intent to switch site
     * Create bundle and put in email from user, bundle add to intent
     * Run new activity
     * Actual logged user @param firebaseUser
     */
    private void loadMainSite(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();

        Intent switchToMainSite = new Intent(LoginActivity.this, MainSite.class);

        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        switchToMainSite.putExtras(bundle);

        startActivity(switchToMainSite);
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }




}