package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.util.Map;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private TextView textView;

    private Button logoutButton;

    private User currentUser;

    private View.OnClickListener logoutCurrentUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            firebaseRepository.logout();
            sharedPreferencesRepository.logoutUser(MainActivity.this);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redirectToTargetByGivenUserRole(getApplicationContext());

        textView = findViewById(R.id.hello_word);
        textView.setText(String.format(getString(R.string.welcome_text), currentUser.getFirstName(), currentUser.getUserAuthority()));

        logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(logoutCurrentUserListener);

    }

    private void redirectToTargetByGivenUserRole(Context context){
        firebaseRepository = new FirebaseRepository();
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        currentUser = sharedPreferencesRepository.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }
}