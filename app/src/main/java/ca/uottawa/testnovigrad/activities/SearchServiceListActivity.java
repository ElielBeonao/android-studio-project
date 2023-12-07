package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.AgencyAdapter;
import ca.uottawa.testnovigrad.fwk.ApplicationUtils;
import ca.uottawa.testnovigrad.fwk.Filter;
import ca.uottawa.testnovigrad.fwk.OnEntityModelButtonClickListener;
import ca.uottawa.testnovigrad.fwk.SearchAgencyAdapter;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class SearchServiceListActivity extends AppCompatActivity implements OnEntityModelButtonClickListener<Agency> {

    private static String TAG = SearchServiceListActivity.class.getName();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private SearchAgencyAdapter searchAgencyAdapter;

    private RecyclerView recyclerView;

    private ImageButton searchFilterButton, logoutButton;

    private View.OnClickListener onSearchButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private User currentUser;

    private View.OnClickListener logoutCurrentUserListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            firebaseRepository.logout();
            sharedPreferencesRepository.logoutUser(SearchServiceListActivity.this);

        }
    };

    private View.OnClickListener showFilterDialogListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SearchServiceListActivity.this);
            LayoutInflater inflater = getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.agency_filter, null);
            dialogBuilder.setView(dialogView);

            TextInputEditText editTextServiceName = dialogView.findViewById(R.id.agency_filter_servicename_field);
            TextInputEditText editTextAddress = dialogView.findViewById(R.id.agency_filter_address_field);
            TextInputEditText editTextTime = dialogView.findViewById(R.id.agency_filter_time_field);


            dialogBuilder.setPositiveButton(getString(R.string.search_text), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    List<Filter> filters = new ArrayList<>();
                    if( editTextServiceName.getText().toString() != null && !editTextServiceName.getText().toString().isEmpty())
                        filters.add( new Filter("servicesDelivery.name","contains", editTextServiceName.getText().toString().toLowerCase().trim()));

                    if( editTextAddress.getText().toString() != null && !editTextAddress.getText().toString().isEmpty())
                        filters.add( new Filter("address","contains", editTextAddress.getText().toString().toLowerCase().trim()));

                    if( editTextTime.getText().toString() != null && !editTextTime.getText().toString().isEmpty()){
                        filters.add( new Filter("openedAt",">=", new Timestamp(ApplicationUtils.convertTimeStringToDate(editTextTime.getText().toString().trim()))));
                        filters.add( new Filter("closedAt","<", new Timestamp(ApplicationUtils.convertTimeStringToDate(editTextTime.getText().toString().trim()))));
                    }


                    applySearchBasedOnFilter(filters, searchAgencyAdapter);
                }
            });

            dialogBuilder.setNegativeButton(getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_service_list);

        Toolbar toolbar = findViewById(R.id.toolbar_search_agency_filter);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);
        }

        recyclerView = findViewById(R.id.recyclerAgencySearchResultsView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click here
                onBackPressed();
            }
        });

        searchFilterButton = findViewById(R.id.btn_filter);
        logoutButton = findViewById(R.id.btn_logout_searchfilter);

        redirectToTargetByGivenUserRole(SearchServiceListActivity.this);
        initializeSearchItems(SearchServiceListActivity.this);

        searchFilterButton.setOnClickListener(showFilterDialogListener);
        logoutButton.setOnClickListener(logoutCurrentUserListener);
    }

    private void redirectToTargetByGivenUserRole(Context context){
        firebaseRepository = new FirebaseRepository();
        sharedPreferencesRepository = new SharedPreferencesRepository(context);
        currentUser = sharedPreferencesRepository.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(context, LoginActivity.class));
            finish();
        }
    }

    private void applySearchBasedOnFilter(List<Filter> filters, SearchAgencyAdapter searchAgencyAdapter) {
        List<Agency> agencies = firebaseRepository.performDynamicSearch(filters, searchAgencyAdapter);

        searchAgencyAdapter.setAgencyList(agencies);
    }

    @Override
    public void onEditButtonClick(Agency agency) {
        Log.d(TAG,"Open Agency Detail :"+ agency.getName());
    }

    @Override
    public void onDeleteButtonClick(Agency entity) {

    }

    private void initializeSearchItems(Context context){

        firebaseRepository.getAllAgencies()
                .thenAccept(agencyList -> {
                    if (agencyList.isEmpty()) {
                        Toast.makeText(context, "Aucune agence dans la base de données", Toast.LENGTH_LONG).show();
                    }else{
                        searchAgencyAdapter = new SearchAgencyAdapter(agencyList);
                        searchAgencyAdapter.setOnEditButtonClickListener(this);
                        searchAgencyAdapter.setOnDeleteButtonClickListener(this);
                        recyclerView.setAdapter(searchAgencyAdapter);
                    }
                })
                .exceptionally(throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Toast.makeText(context, "Unable to fetch agencies list.", Toast.LENGTH_SHORT).show();
                    return null;
                });

    }
}