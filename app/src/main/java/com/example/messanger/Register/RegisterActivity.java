package com.example.messanger.Register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.messanger.MainActivity;
import com.example.messanger.Models.User;
import com.example.messanger.R;
import com.example.messanger.Repositories.DataBase;
import com.example.messanger.Validators.ValidateEverything;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;


public class RegisterActivity extends AppCompatActivity {

    // Controlls
    private EditText loginInput, passwordInput, emailInput, nameInput, surnameInput, ageInput, phoneNumberInput;
    private Button registerBtn;
    private Spinner spinner;

    private DataBase dataBase;
    private FirebaseAuth firebaseAuth;

    private ImageView profilePhoto;
    public Uri photoURI;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String photoID;
    private boolean upload;

    // Composition
    ValidateEverything validate = new ValidateEverything();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        spinner = (Spinner) findViewById(R.id.countriesSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);



        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profilePhoto = findViewById(R.id.ivProfilePhoto);
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        initInterface();

        // Read database last index
        checkLastIDfromFirebase();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = String.valueOf(loginInput.getText());
                String password = String.valueOf(passwordInput.getText());
                String email = String.valueOf(emailInput.getText());
                String name = String.valueOf(nameInput.getText());
                String surname = String.valueOf(surnameInput.getText());
                String age = String.valueOf(ageInput.getText());
                String phoneNumber = String.valueOf(phoneNumberInput.getText());

                int errors = 0;
                if (validate.login.emptyField(login) || validate.login.oneSpace(login) || !validate.login.checkConstraint(login)) {
                    loginInput.setError("Invalid login");
                } else {
                    errors++;
                }

                if (validate.password.emptyField(password) || validate.password.oneSpace(password) || !validate.password.checkConstraint(password)) {
                    passwordInput.setError("Invalid password ");
                } else {
                    errors++;
                }

                if (validate.email.emptyField(email) || validate.email.oneSpace(email) || !validate.email.checkConstraint(email)) {
                    emailInput.setError("Invalid email");
                } else {
                    errors++;
                }

                if (validate.name.emptyField(name) || validate.name.oneSpace(name) || !validate.name.checkConstraint(name)) {
                    nameInput.setError("Invalid name");
                } else {
                    errors++;
                }

                if (validate.surname.emptyField(surname) || validate.surname.oneSpace(surname) || !validate.surname.checkConstraint(surname)) {
                    surnameInput.setError("Invalid surname");
                } else {
                    errors++;
                }

                if (validate.age.emptyField(age) || validate.age.oneSpace(age) || !validate.age.checkConstraint(age)) {
                    ageInput.setError("Invalid age");
                } else {
                    errors++;
                }

                if (validate.phoneNumber.emptyField(phoneNumber) || validate.phoneNumber.oneSpace(phoneNumber) || !validate.phoneNumber.checkConstraint(phoneNumber)) {
                    phoneNumberInput.setError("Invalid phoneNumber");
                } else {
                    errors++;
                }

                if (errors == 7 && upload) {
                    // Create new user in database
                    // Create new user to authority if process will be complete open login site else give notification
                    firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                dataBase.save(new User(spinner.getSelectedItem().toString(), login, password, email, name, surname, Integer.parseInt(age), Integer.parseInt(phoneNumber), photoID));
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                Toast toast = Toast.makeText(getApplicationContext(), "Register Succesful", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "User with this email already exist", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                } else if (!upload) {
                    Toast.makeText(getApplicationContext(), "Photo is required", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Create new Intent object, set type of intent in this case is "image/*"
     * and start new activity with request code set to 1
     */
    private void chooseImage() {
        Intent openGallery = new Intent();
        openGallery.setType("image/*");
        openGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(openGallery, 1);
    }

    /**
     * Code from method chooseImage() set up to 1, in this method check this code because java have to know which activity control now @param requestCode
     * Check if everything is ok @param resultCode
     * Image choose in intent @param data
     * After check, get data from image, set this image in layout of register form and run uploadPhoto() method
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photoURI = data.getData();
            profilePhoto.setImageURI(photoURI);
            System.out.println("PHOTO URI: " + photoURI);
            uploadPhoto();
        }
    }


    /**
     * Generate random key to set as title of uploaded image (unique)
     * Get branch of storage which name images/ <- here put all uploaded images
     */
    private void uploadPhoto() {
        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        riversRef.putFile(photoURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        // popup with information
                        upload = true;
                        photoID = randomKey;
                        Snackbar.make(findViewById(android.R.id.content), "Photo uploaded.", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage((int) progress + " %");
                    }
                });
    }

    /**
     * Take Users table and set actual count of users as new id
     */
    private void checkLastIDfromFirebase() {
        dataBase = new DataBase();
        DatabaseReference rootRef = dataBase.firebase.getReference();
        DatabaseReference usersRef = rootRef.child("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                dataBase.setID(count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        usersRef.addValueEventListener(valueEventListener);
    }

    /**
     * Initialize all controls on interface
     */
    private void initInterface() {
        registerBtn = findViewById(R.id.btnSignup);
        loginInput = findViewById(R.id.etLogin);
        passwordInput = findViewById(R.id.etPassword);
        emailInput = findViewById(R.id.etEmail);
        nameInput = findViewById(R.id.etName);
        surnameInput = findViewById(R.id.etSurname);
        ageInput = findViewById(R.id.etAge);
        phoneNumberInput = findViewById(R.id.etPhoneNumber);
    }




}