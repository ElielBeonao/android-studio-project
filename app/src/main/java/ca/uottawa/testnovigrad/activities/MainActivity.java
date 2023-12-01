package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private TextView textView;

    private Button userManagementNavigationButton, agencyManagementNavigationButton, serviceDeliveryManagementNavigationButton, logoutButton;

    private User currentUser;

    private View.OnClickListener logoutCurrentUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            firebaseRepository.logout();
            sharedPreferencesRepository.logoutUser(MainActivity.this);

        }
    };

    private View.OnClickListener manageUserNavigationListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), UserManagementActivity.class));
            finish();

        }
    };

    private View.OnClickListener manageAgencyNavigationListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), AgencyManagementActivity.class));
            finish();

        }
    };

    private View.OnClickListener manageServiceDeliveryNavigationListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), ServiceDeliveryActivity.class));
            finish();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redirectToTargetByGivenUserRole(getApplicationContext());
        setUnattachedEmployee(currentUser);

        textView = findViewById(R.id.hello_word);
        textView.setText(String.format(getString(R.string.welcome_text), currentUser.getFirstName(), currentUser.getUserAuthority()));

        logoutButton = findViewById(R.id.btn_logout);
        userManagementNavigationButton = findViewById(R.id.btn_user_management);
        agencyManagementNavigationButton = findViewById(R.id.btn_agency_management);
        serviceDeliveryManagementNavigationButton = findViewById(R.id.btn_customer_services_management);

        logoutButton.setOnClickListener(logoutCurrentUserListener);
        userManagementNavigationButton.setOnClickListener(manageUserNavigationListener);
        agencyManagementNavigationButton.setOnClickListener(manageAgencyNavigationListener);
        serviceDeliveryManagementNavigationButton.setOnClickListener(manageServiceDeliveryNavigationListener);
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

    private void setUnattachedEmployee(User currentUser){
        if( currentUser.getUserAuthority().equals(FirebaseRepository.USER_ROLE_EMPLOYEE) && currentUser.getUserCompany() == null){
            loadAllAgencies(currentUser);
        }
    }

    private void loadAllAgencies(User currentUser){
        this.firebaseRepository.getAllAgencies()
                .thenAccept( agencyList ->{
                    showUserAgencyDialog(agencyList, currentUser);
                })
                .exceptionally(throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Toast.makeText(getApplicationContext(), "Unable to fecth agencies list.", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void showUserAgencyDialog(List<Agency> agencies, User currentUser){
        final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        agencies.forEach((agency -> {
            adapter.add(agency.getName());
        }));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionnez une succursale").setCancelable(false);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Agency selectedAgency = agencies.get(which);
                Toast.makeText(getApplicationContext(), "Selected Agency:"+ selectedAgency.toString(), Toast.LENGTH_SHORT).show();
                firebaseRepository.editAccount(currentUser.getUid(), currentUser.getEmail(), currentUser.getFirstName(), currentUser.getLastName(), currentUser.getUserAuthority(), selectedAgency.getId())
                        .thenAccept( userId -> {

                            Toast.makeText(getApplicationContext(), "Informations modifiees avec succes", Toast.LENGTH_SHORT).show();
                        })
                        .exceptionally(throwable -> {
                            Log.d(TAG, throwable.getMessage());
                            Toast.makeText(getApplicationContext(), "Unable to Edit user infos", Toast.LENGTH_SHORT).show();
                            return null;
                        });
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}