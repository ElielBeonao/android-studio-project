package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class RegisterActivity extends AppCompatActivity {

    private static String TAG = RegisterActivity.class.getName();

    private TextInputEditText emailAddressEditText, passwordEditText, confirmPasswordEditText, firstNameEditText, lastNameEditText;
    private RadioButton userRoleClientRadioButton, userRoleEmployeeRadioButton;

    private Button navigationButton, submitButton;

    private String userRole = FirebaseRepository.USER_ROLE_CLIENT;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private View.OnClickListener navigationListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    };

    private View.OnClickListener registerListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(isFormValid()){
                firebaseRepository.createAccount(
                        emailAddressEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim(),
                        firstNameEditText.getText().toString().trim(),
                        lastNameEditText.getText().toString().trim(),
                        userRole
                ).thenAccept(userId -> {

                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();

                }).exceptionally(throwable -> {
                    Log.e(TAG,"Error creating user account: " + throwable.getMessage());
                    Toast.makeText(RegisterActivity.this, "Une Erreur est survenue lors de la creation du compte. Veuillez contacter l'administrateur ", Toast.LENGTH_SHORT).show();
                    return null;
                });
            }else{
                Toast.makeText(RegisterActivity.this, "Veuillez remplir tous les champs de ce formulaire!", Toast.LENGTH_SHORT).show();

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        redirectToTargetByGivenUserRole(getApplicationContext());

        emailAddressEditText = findViewById(R.id.email_address_input);
        passwordEditText = findViewById(R.id.password_input);
        confirmPasswordEditText = findViewById(R.id.confirm_password_input);
        firstNameEditText = findViewById(R.id.firstname_input);
        lastNameEditText = findViewById(R.id.lastname_input);
        submitButton = findViewById(R.id.btn_register);
        navigationButton = findViewById(R.id.btn_login_navto_login);

        userRoleClientRadioButton = findViewById(R.id.user_role_client);
        userRoleEmployeeRadioButton = findViewById(R.id.radio_user_role_employee);

        userRoleClientRadioButton.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                userRoleEmployeeRadioButton.setChecked(false);
                userRole = FirebaseRepository.USER_ROLE_CLIENT;
            }else{
                userRoleEmployeeRadioButton.setChecked(true);
                userRole = FirebaseRepository.USER_ROLE_EMPLOYEE;
            }
        });

        userRoleEmployeeRadioButton.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                userRoleClientRadioButton.setChecked(false);
                userRole = FirebaseRepository.USER_ROLE_EMPLOYEE;
            }else{
                userRoleClientRadioButton.setChecked(true);
                userRole = FirebaseRepository.USER_ROLE_CLIENT;
            }
        });

        submitButton.setOnClickListener(registerListener);
        navigationButton.setOnClickListener(navigationListener);
    }

    private Boolean isFormValid(){
        if( emailAddressEditText.getText().toString().trim().length() == 0 ){
            emailAddressEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            emailAddressEditText.setError(null);
        }

        if( !emailAddressEditText.getText().toString().trim().matches(emailPattern)){
            emailAddressEditText.setError(getString(R.string.invalid_emailaddress));
            return false;
        }else{
            emailAddressEditText.setError(null);
        }

        if( passwordEditText.getText().toString().trim().length() == 0 ){
            passwordEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            passwordEditText.setError(null);
        }

        if( confirmPasswordEditText.getText().toString().trim().length() == 0 ){
            confirmPasswordEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            confirmPasswordEditText.setError(null);
        }

        if( !passwordEditText.getText().toString().trim().equals(confirmPasswordEditText.getText().toString().trim()) ){
            confirmPasswordEditText.setError(getString(R.string.password_confirmation_error));
            return false;
        }else{
            confirmPasswordEditText.setError(null);
        }

        return true;
    }

    private void redirectToTargetByGivenUserRole(Context context){
        firebaseRepository = new FirebaseRepository();
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        if(sharedPreferencesRepository.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

//        firebaseRepository.isUserAuthenticated()
//                .thenAccept(isAuthenticated -> {
//                    if (isAuthenticated) {
//                        Toast.makeText(getApplicationContext(), "User authenticated", Toast.LENGTH_LONG).show();
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        finish();
//                    }
//                })
//                .exceptionally(throwable -> {
//                    Log.d(TAG, throwable.getMessage());
//                    Toast.makeText(getApplicationContext(), "Unable to check if user is authenticated", Toast.LENGTH_SHORT).show();
//                    return null;
//                });
    }
}