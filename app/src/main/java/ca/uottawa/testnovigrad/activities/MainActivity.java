package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.ApplicationUtils;
import ca.uottawa.testnovigrad.fwk.OnMultiSelectListener;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.ServiceDelivery;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private TextView textView;

    private Button currentAgencyDetailEditButton, currentAgencyServicesEditButton, userManagementNavigationButton, agencyManagementNavigationButton, serviceDeliveryManagementNavigationButton;
    private ImageButton logoutButton;

    private User currentUser;

    private Agency currentAgency;

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
        redirectToTargetByGivenUserRole(this);


        textView = findViewById(R.id.hello_word);
        textView.setText(String.format(getString(R.string.welcome_text), currentUser.getFirstName(), currentUser.getUserAuthority()));

        currentAgencyDetailEditButton =  findViewById(R.id.btn_edit_current_agency_detail_management);
        currentAgencyServicesEditButton = findViewById(R.id.btn_edit_current_agency_services_management);

        logoutButton = findViewById(R.id.btn_logout);
        userManagementNavigationButton = findViewById(R.id.btn_user_management);
        agencyManagementNavigationButton = findViewById(R.id.btn_agency_management);
        serviceDeliveryManagementNavigationButton = findViewById(R.id.btn_customer_services_management);

        logoutButton.setOnClickListener(logoutCurrentUserListener);
        userManagementNavigationButton.setOnClickListener(manageUserNavigationListener);
        agencyManagementNavigationButton.setOnClickListener(manageAgencyNavigationListener);
        serviceDeliveryManagementNavigationButton.setOnClickListener(manageServiceDeliveryNavigationListener);

        displayAdaptedControls(currentUser);

    }

    private void redirectToTargetByGivenUserRole(Context context){
        firebaseRepository = new FirebaseRepository();
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        currentUser = sharedPreferencesRepository.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(context, LoginActivity.class));
            finish();
        }else{
            setUnattachedEmployee(currentUser, context);
        }
    }

    private void setUnattachedEmployee(User currentUser, Context context){
        if( currentUser.getUserAuthority().equals(FirebaseRepository.USER_ROLE_EMPLOYEE) && currentUser.getUserCompany() == null){
            loadAllAgencies(currentUser);
        }
        if( currentUser.getUserCompany() != null ){
            firebaseRepository.retrieveAgencyByUid(currentUser.getUserCompany())
                    .thenAccept( agencyFound ->{
                        currentAgency = agencyFound;
                        currentAgencyDetailEditButton.setText(String.format(getString(R.string.change_agency_detail_management_label), currentAgency.getName()));
                        currentAgencyServicesEditButton.setText(String.format(getString(R.string.change_agency_services_management_label), currentAgency.getName()));

                        currentAgencyDetailEditButton.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                showEditAgencyDialog(currentAgency);
                            }
                        });
                        currentAgencyServicesEditButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setCurrentAgencyServicesByUser(context, currentUser, currentAgency);
                            }
                        });

                    })
                    .exceptionally(throwable -> {
                        Log.d(TAG, throwable.getMessage());
                        Toast.makeText(getApplicationContext(), "Unable to fecth current agency", Toast.LENGTH_SHORT).show();
                        return null;
                    });
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
                            sharedPreferencesRepository.setCurrentUser(new User(currentUser.getUid(), currentUser.getEmail(), currentUser.getFirstName(), currentUser.getLastName(), currentUser.getUserAuthority(), selectedAgency.getId()));
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

    private void displayAdaptedControls(User currentUser){
        if (currentUser.getUserAuthority().equals(FirebaseRepository.USER_ROLE_ADMINISTRATOR)){

            userManagementNavigationButton.setVisibility(View.VISIBLE);
            agencyManagementNavigationButton.setVisibility(View.VISIBLE);
            serviceDeliveryManagementNavigationButton.setVisibility(View.VISIBLE);

            currentAgencyServicesEditButton.setVisibility(View.GONE);
            currentAgencyDetailEditButton.setVisibility(View.GONE);

        }else if( currentUser.getUserAuthority().equals(FirebaseRepository.USER_ROLE_EMPLOYEE) ){

            currentAgencyServicesEditButton.setVisibility(View.VISIBLE);
            currentAgencyDetailEditButton.setVisibility(View.VISIBLE);

            userManagementNavigationButton.setVisibility(View.GONE);
            agencyManagementNavigationButton.setVisibility(View.GONE);
            serviceDeliveryManagementNavigationButton.setVisibility(View.GONE);
        } else{
            currentAgencyServicesEditButton.setVisibility(View.GONE);
            currentAgencyDetailEditButton.setVisibility(View.GONE);

            userManagementNavigationButton.setVisibility(View.GONE);
            agencyManagementNavigationButton.setVisibility(View.GONE);
            serviceDeliveryManagementNavigationButton.setVisibility(View.GONE);
        }
    }

    private void showEditAgencyDialog(Agency agency) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_agency, null);
        dialogBuilder.setView(dialogView);

        TextInputEditText editTextAgencyName = dialogView.findViewById(R.id.agency_edit_name_input);
        TextInputEditText editTextAgencyAddress = dialogView.findViewById(R.id.agency_edit_address_input);
        TextInputEditText editTextAgencyOpenedAt = dialogView.findViewById(R.id.agency_edit_openedat_input);
        TextInputEditText editTextAgencyClosedAt = dialogView.findViewById(R.id.agency_edit_closedat_input);

        editTextAgencyName.setText(agency.getName());
        editTextAgencyAddress.setText(agency.getAddress());
        if(agency.getOpenedAt() != null) {
            editTextAgencyOpenedAt.setText(ApplicationUtils.convertToDateTimeString(agency.getOpenedAt().toDate()));
        }

        if(agency.getClosedAt() != null){
            editTextAgencyClosedAt.setText(ApplicationUtils.convertToDateTimeString(agency.getClosedAt().toDate()));
        }


        dialogBuilder.setPositiveButton(getString(R.string.save_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( isFormValid(editTextAgencyName, editTextAgencyAddress, editTextAgencyOpenedAt, editTextAgencyClosedAt)){

                    agency.setName(editTextAgencyName.getText().toString().trim());
                    agency.setAddress(editTextAgencyAddress.getText().toString().trim());
                    agency.setOpenedAt(new Timestamp(ApplicationUtils.convertTimeStringToDate(editTextAgencyOpenedAt.getText().toString().trim())));
                    agency.setClosedAt(new Timestamp(ApplicationUtils.convertTimeStringToDate(editTextAgencyClosedAt.getText().toString().trim())));

                    editAgency(agency);
                }
            }
        });

        dialogBuilder.setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // L'utilisateur a annulé la modification
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private Boolean isFormValid(TextInputEditText nameEditText, TextInputEditText addressEditText, TextInputEditText openedAtEditText, TextInputEditText closedAtEditText){

        if( nameEditText.getText().toString().trim().length() == 0 ){
            nameEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            nameEditText.setError(null);
        }

        if( addressEditText.getText().toString().trim().length() == 0 ){
            addressEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            addressEditText.setError(null);
        }

        if( openedAtEditText.getText().toString().trim().length() == 0 ){
            openedAtEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            openedAtEditText.setError(null);
        }

        if( closedAtEditText.getText().toString().trim().length() == 0 ){
            closedAtEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            closedAtEditText.setError(null);
        }

        return true;
    }

    private void editAgency(Agency agency){
        firebaseRepository.updateAgency(agency).thenAccept( agencyId -> {
            if(agencyId != null){
                Toast.makeText(getApplicationContext(), "Agence a été modifiée avec succes!", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "L'agence n'a pu être modifiée!!!", Toast.LENGTH_SHORT).show();
            }

        }).exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Une erreur est suvenue lors de la creation!!!", Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void setCurrentAgencyServicesByUser(Context context, User currentUser, Agency currentAgency){

        firebaseRepository
                .getAllServiceDeliveries()
                .thenAccept( serviceDeliveries -> {

                    if( !serviceDeliveries.isEmpty() ){
                        showSelectorDialogUI(serviceDeliveries, currentAgency, context);
                    }

                })
                .exceptionally(throwable -> {
                    Log.d(TAG, "unable to fecth services list:" + throwable.getMessage());
//                    throwable.getStackTrace();
                    Toast.makeText(context, "Une erreur est suvenue lors du chargement des donnees!!!", Toast.LENGTH_SHORT).show();
                    return null;
                });


    }

    private void showSelectorDialogUI(List<ServiceDelivery> serviceDeliveries, Agency currentAgency, Context context){
        boolean[] checkedItems = new boolean[serviceDeliveries.size()];
        List<String> serviceDeliveryIdsInAgency = getServiceDeliveryIdsInAgency(currentAgency);
        for (int i=0; i < serviceDeliveries.size(); i++){
            checkedItems[i] = serviceDeliveryIdsInAgency.contains(serviceDeliveries.get(i).getId());
        }

        ApplicationUtils.showDialogWithMultipleSelection(
                context,
                serviceDeliveries,
                checkedItems,
                ServiceDelivery::getName,
                String.format(context.getString(R.string.select_agency_services_management_label), currentAgency.getName()),
                context.getString(R.string.cancel_text),
                context.getString(R.string.save_text),
                new OnMultiSelectListener<ServiceDelivery>() {
                    @Override
                    public void onMultiSelect(List<ServiceDelivery> selectedItems) {

                        currentAgency.setServicesDelivery(selectedItems);
                        Log.d(TAG, "Selected ServiceDelivery: " + selectedItems.size());
                    }
                },
                new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                },
                new DialogInterface.OnClickListener(){


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editAgency(currentAgency);
                    }
                },
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                        Log.d(TAG, "Checkbox clicked - Position: " + which + ", Checked: " + isChecked);

                    }
                }
        );
    }

    private List<String> getServiceDeliveryIdsInAgency(Agency agency) {
        List<String> serviceDeliveryIds = new ArrayList<>();
        List<ServiceDelivery> servicesDelivery = agency.getServicesDelivery();
        for (ServiceDelivery serviceDelivery : servicesDelivery) {
            serviceDeliveryIds.add(serviceDelivery.getId());
        }
        return serviceDeliveryIds;
    }
}