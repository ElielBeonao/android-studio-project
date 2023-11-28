package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.Map;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = LoginActivity.class.getName();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private TextInputEditText emailAddressEditText, passwordEditText;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private Button submitButton;

    private View.OnClickListener authenticateUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(isFormValid())
                firebaseRepository.loginUser(
                        emailAddressEditText.getText().toString().trim(),
                        passwordEditText.getText().toString().trim()
                ).thenAccept(userId -> {

                    if(userId != null && !userId.isEmpty()){
                        firebaseRepository.retrieveDocumentByUid(FirebaseRepository.USER_COLLECTION_NAME, userId)
                                .thenAccept(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Map<String, Object> userMap = documentSnapshot.getData();

                                        sharedPreferencesRepository.setCurrentUser(
                                                new User(userId,
                                                        userMap.get("email").toString(),
                                                        userMap.get("firstName").toString(),
                                                        userMap.get("lastName").toString(),
                                                        userMap.get("userAuthority").toString()
                                                )
                                        );
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }else{
                                        Toast.makeText(LoginActivity.this, "Les informations de l'utilisateur sont inexistantes. Impossible de poursuivre", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .exceptionally(throwable -> {
                                    Log.e(TAG,"Error during current user information fetching: " + throwable.getMessage());
                                    Toast.makeText(LoginActivity.this, "Une Erreur est survenue lors de la recuperation des informations de la session. Veuillez contacter l'administrateur ", Toast.LENGTH_SHORT).show();
                                    return null;
                                });

                    }

                }).exceptionally(throwable -> {
                    Log.e(TAG,"Error during user authentication: " + throwable.getMessage());
                    Toast.makeText(LoginActivity.this, "Une Erreur est survenue lors de l'authentification. Veuillez contacter l'administrateur ", Toast.LENGTH_SHORT).show();
                    return null;
                });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferencesRepository = new SharedPreferencesRepository(getApplicationContext());

        redirectToTargetByGivenUserRole(getApplicationContext());

        emailAddressEditText = findViewById(R.id.login_email_address_input);
        passwordEditText = findViewById(R.id.login_password_input);
        submitButton = findViewById(R.id.btn_login);

        submitButton.setOnClickListener(authenticateUserListener);
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


        return true;
    }
}