package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.AgencyAdapter;
import ca.uottawa.testnovigrad.fwk.ApplicationUtils;
import ca.uottawa.testnovigrad.fwk.OnEntityModelButtonClickListener;
import ca.uottawa.testnovigrad.fwk.UserAdapter;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class AgencyManagementActivity extends AppCompatActivity implements OnEntityModelButtonClickListener<Agency> {

    private static String TAG = AgencyManagementActivity.class.getName();

    private User currentUser;

    private AgencyAdapter agencyAdapter;

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private RecyclerView recyclerView;

    private ImageButton mainNavigationButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton floatingActionButtonAddAgency;
    private View.OnClickListener addAgencyListener = new View.OnClickListener(

    ) {
        @Override
        public void onClick(View v) {
            showEditAgencyDialog(new Agency());
        }
    };

    private View.OnClickListener navigateToMainListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agency_management);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshAgencyLayout);

        Toolbar toolbar = findViewById(R.id.toolbar_agency_management);
        setSupportActionBar(toolbar);

        floatingActionButtonAddAgency = findViewById(R.id.fabAddAgency);
        mainNavigationButton = findViewById(R.id.btn_agency_management_navto_main);

        redirectToTargetByGivenUserRole(getApplicationContext());

        recyclerView = findViewById(R.id.recyclerAgenciesView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainNavigationButton.setOnClickListener(navigateToMainListener);
        floatingActionButtonAddAgency.setOnClickListener(addAgencyListener);

        loadAgencies(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAgencies(recyclerView);
            }
        });

    }

    private void redirectToTargetByGivenUserRole(Context context){
        firebaseRepository = new FirebaseRepository();
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        currentUser = sharedPreferencesRepository.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else{
            if( !currentUser.getUserAuthority().equals(FirebaseRepository.USER_ROLE_ADMINISTRATOR) ){
                Toast.makeText(getApplicationContext(), "Vous n'avez pas les droits nécéssaires pour accéder à cette fonctionnalité!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }
    }

    private void loadAgencies(RecyclerView recyclerView){
        firebaseRepository.getAllAgencies()
                .thenAccept(agencyList -> {
                    if (agencyList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Aucune agence dans la base de données", Toast.LENGTH_LONG).show();
                    }else{
                        agencyAdapter = new AgencyAdapter(agencyList);
                        agencyAdapter.setOnEditButtonClickListener(this);
                        agencyAdapter.setOnDeleteButtonClickListener(this);
                        recyclerView.setAdapter(agencyAdapter);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .exceptionally(throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Toast.makeText(getApplicationContext(), "Unable to fecth agencies list.", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return null;
                });
    }

    @Override
    public void onEditButtonClick(Agency agency) {
        showEditAgencyDialog(agency);
    }

    @Override
    public void onDeleteButtonClick(Agency agency) {
        showDeleteDialog(agency);
    }

    private void showDeleteDialog(Agency agency) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(String.format(getString(R.string.delete_question_text), agency.getName()));
        dialogBuilder.setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteAgency(agency.getId());
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
        if(agency.getOpenedAt() != null)
            editTextAgencyOpenedAt.setText(ApplicationUtils.convertToDateTimeString(agency.getOpenedAt()));

        if(agency.getClosedAt() != null)
            editTextAgencyClosedAt.setText(ApplicationUtils.convertToDateTimeString(agency.getClosedAt()));

        dialogBuilder.setPositiveButton(getString(R.string.add_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( isFormValid(editTextAgencyName, editTextAgencyAddress, editTextAgencyOpenedAt, editTextAgencyClosedAt)){

                    agency.setName(editTextAgencyName.getText().toString().trim());
                    agency.setAddress(editTextAgencyAddress.getText().toString().trim());
                    agency.setOpenedAt(ApplicationUtils.convertTimeStringToDate(editTextAgencyOpenedAt.getText().toString().trim()));
                    agency.setClosedAt(ApplicationUtils.convertTimeStringToDate(editTextAgencyClosedAt.getText().toString().trim()));

                    if(agency.getId() == null){
                        addAgency(agency);
                    }else{
                        editAgency(agency);
                    }
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

    private void addAgency(Agency agency){
        firebaseRepository.createAgency(agency).thenAccept( agencyId -> {
            if(agencyId != null){
                Toast.makeText(getApplicationContext(), "Agence a été créée avec succes!", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "L'agence pu être créée!!!", Toast.LENGTH_SHORT).show();

            }

        }).exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Une erreur est suvenue lors de la creation!!!", Toast.LENGTH_SHORT).show();
            return null;
        });
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

    private void deleteAgency(String agencyId){
        firebaseRepository.deleteAgency(agencyId)
                .thenAccept(Void -> {
            Toast.makeText(getApplicationContext(), "Informations supprimees avec succes", Toast.LENGTH_SHORT).show();

        }).exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Unable to Edit user infos", Toast.LENGTH_SHORT).show();
            return null;
        });
    }
}