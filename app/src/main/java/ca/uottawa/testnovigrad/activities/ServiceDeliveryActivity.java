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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.AgencyAdapter;
import ca.uottawa.testnovigrad.fwk.ApplicationUtils;
import ca.uottawa.testnovigrad.fwk.OnEntityModelButtonClickListener;
import ca.uottawa.testnovigrad.fwk.ServiceDeliveryAdapter;
import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.ServiceDelivery;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class ServiceDeliveryActivity extends AppCompatActivity implements OnEntityModelButtonClickListener<ServiceDelivery> {

    private static String TAG = ServiceDeliveryActivity.class.getName();

    private User currentUser;

    private ServiceDeliveryAdapter serviceDeliveryAdapter;

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private RecyclerView recyclerView;

    private ImageButton mainNavigationButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton floatingActionButtonAddServiceDelivery;

    private View.OnClickListener addServiceDeliveryListener = new View.OnClickListener(

    ) {
        @Override
        public void onClick(View v) {
            showEditServiceDeliveryDialog(new ServiceDelivery());
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
        setContentView(R.layout.activity_service_delivery);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshServiceDeliveryLayout);

        Toolbar toolbar = findViewById(R.id.toolbar_service_delivery_management);
        setSupportActionBar(toolbar);

        floatingActionButtonAddServiceDelivery = findViewById(R.id.fabAddServiceDelivery);
        mainNavigationButton = findViewById(R.id.btn_service_delivery_management_navto_main);

        redirectToTargetByGivenUserRole(getApplicationContext());

        recyclerView = findViewById(R.id.recyclerServiceDeliveriesView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainNavigationButton.setOnClickListener(navigateToMainListener);
        floatingActionButtonAddServiceDelivery.setOnClickListener(addServiceDeliveryListener);

        loadServiceDeliveries(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadServiceDeliveries(recyclerView);
            }
        });
    }


    private void addServiceDelivery(ServiceDelivery serviceDelivery){
        firebaseRepository.createServiceDelivery(serviceDelivery).thenAccept( serviceDeliveryId -> {
            if(serviceDeliveryId != null){
                Toast.makeText(getApplicationContext(), "Une prestation de service a été créée avec succes!", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "L'agence pu être créée!!!", Toast.LENGTH_SHORT).show();

            }

        }).exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Une erreur est suvenue lors de la creation!!!", Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void editServiceDelivery(ServiceDelivery serviceDelivery){
        firebaseRepository.updateServiceDelivery(serviceDelivery).thenAccept( serviceDeliveryId -> {
            if(serviceDeliveryId != null){
                Toast.makeText(getApplicationContext(), "Prestation de service a été modifiée avec succes!", Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "Prestation de service n'a pu être modifiée!!!", Toast.LENGTH_SHORT).show();
            }

        }).exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Une erreur est suvenue lors de la creation!!!", Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void deleteServiceDelivery(String serviceDeliveryId){
        firebaseRepository.deleteServiceDelivery(serviceDeliveryId)
                .thenAccept(Void -> {
                    Toast.makeText(getApplicationContext(), "Informations supprimees avec succes", Toast.LENGTH_SHORT).show();

                }).exceptionally(throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Toast.makeText(getApplicationContext(), "Unable to Delete Service Delivery", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    @Override
    public void onEditButtonClick(ServiceDelivery serviceDelivery) {
        showEditServiceDeliveryDialog(serviceDelivery);
    }

    @Override
    public void onDeleteButtonClick(ServiceDelivery serviceDelivery) {
        showDeleteDialog(serviceDelivery);
    }

    private Boolean isFormValid(TextInputEditText nameEditText){
        if( nameEditText.getText().toString().trim().length() == 0 ){
            nameEditText.setError(getString(R.string.field_required_error));
            return false;
        }else{
            nameEditText.setError(null);
        }

        return true;
    }

    private void loadServiceDeliveries(RecyclerView recyclerView){
        firebaseRepository.getAllServiceDeliveries()
                .thenAccept(serviceDeliveryList -> {
                    if (serviceDeliveryList.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Aucune prestation de service dans la base de données", Toast.LENGTH_LONG).show();
                    }else{
                        serviceDeliveryAdapter = new ServiceDeliveryAdapter(serviceDeliveryList);
                        serviceDeliveryAdapter.setOnEditButtonClickListener(this);
                        serviceDeliveryAdapter.setOnDeleteButtonClickListener(this);
                        recyclerView.setAdapter(serviceDeliveryAdapter);
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


    private void showEditServiceDeliveryDialog(ServiceDelivery serviceDelivery) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_service_delivery, null);
        dialogBuilder.setView(dialogView);

        TextInputEditText editTextServiceDeliveryName = dialogView.findViewById(R.id.service_delivery_edit_name_input);
        TextInputEditText editTextServiceDeliveryDescription = dialogView.findViewById(R.id.service_delivery_edit_description_input);

        editTextServiceDeliveryName.setText(serviceDelivery.getName());
        editTextServiceDeliveryDescription.setText(serviceDelivery.getDescription());

        dialogBuilder.setPositiveButton(getString(R.string.add_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( isFormValid(editTextServiceDeliveryName)){

                    serviceDelivery.setName(editTextServiceDeliveryName.getText().toString().trim());
                    serviceDelivery.setDescription(editTextServiceDeliveryDescription.getText().toString().trim());

                    if(serviceDelivery.getId() == null){
                        addServiceDelivery(serviceDelivery);
                    }else{
                        editServiceDelivery(serviceDelivery);
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

    private void showDeleteDialog(ServiceDelivery serviceDelivery) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(String.format(getString(R.string.delete_question_text), serviceDelivery.getName()));
        dialogBuilder.setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteServiceDelivery(serviceDelivery.getId());
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

}