package ca.uottawa.testnovigrad.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.OnUserEditButtonClickListener;
import ca.uottawa.testnovigrad.fwk.UserAdapter;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class UserManagementActivity extends AppCompatActivity implements OnUserEditButtonClickListener {

    private static String TAG = UserManagementActivity.class.getName();

    private User currentUser;

    private UserAdapter userAdapter;
    private List<User> users = new ArrayList<>();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        Toolbar toolbar = findViewById(R.id.toolbar_user_management);
        setSupportActionBar(toolbar);

        redirectToTargetByGivenUserRole(getApplicationContext());

        RecyclerView recyclerView = findViewById(R.id.recyclerUsersView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUsers(recyclerView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUsers(recyclerView);
            }
        });


    }

    private void loadUsers(RecyclerView recyclerView){
            firebaseRepository.getAllUsers()
            .thenAccept(userList -> {
                if (userList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Aucun utilisateur dans la base de données", Toast.LENGTH_LONG).show();
                }else{
                    userAdapter = new UserAdapter(userList);
                    userAdapter.setOnEditButtonClickListener(this);
                    userAdapter.setOnDeleteButtonClickListener(this);
                    recyclerView.setAdapter(userAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }
            })
            .exceptionally(throwable -> {
                Log.d(TAG, throwable.getMessage());
                Toast.makeText(getApplicationContext(), "Unable to fecth users list.", Toast.LENGTH_SHORT).show();
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

    // Méthode pour afficher la boîte de dialogue de modification
    private void showEditDialog(User user) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);
        dialogBuilder.setView(dialogView);


        EditText editTextEmailAddress = dialogView.findViewById(R.id.user_edit_email_address_input);
        EditText editTextFirstName = dialogView.findViewById(R.id.user_edit_firstname_input);
        EditText editTextLastName = dialogView.findViewById(R.id.user_edit_lastname_input);
        RadioButton radioButtonUserRoleClient = dialogView.findViewById(R.id.user_edit_radio_user_role_client);
        RadioButton radioButtonUserRoleEmployee = dialogView.findViewById(R.id.user_edit_radio_user_role_employee);
        RadioButton radioButtonUserRoleAdmin = dialogView.findViewById(R.id.user_edit_radio_user_role_admin);

        editTextEmailAddress.setText(user.getEmail());
        editTextFirstName.setText(user.getFirstName());
        editTextLastName.setText(user.getLastName());
        if(user.getUserAuthority().equals(FirebaseRepository.USER_ROLE_CLIENT)){
            radioButtonUserRoleClient.setChecked(true);
            radioButtonUserRoleEmployee.setChecked(false);
            radioButtonUserRoleAdmin.setChecked(false);

        }else if( user.getUserAuthority().equals(FirebaseRepository.USER_ROLE_EMPLOYEE) ){
            radioButtonUserRoleClient.setChecked(false);
            radioButtonUserRoleEmployee.setChecked(true);
            radioButtonUserRoleAdmin.setChecked(false);
        } else if ( user.getUserAuthority().equals(FirebaseRepository.USER_ROLE_ADMINISTRATOR)){
            radioButtonUserRoleClient.setChecked(false);
            radioButtonUserRoleEmployee.setChecked(false);
            radioButtonUserRoleAdmin.setChecked(true);
        }

        radioButtonUserRoleClient.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleEmployee.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_CLIENT);
            }
        });

        radioButtonUserRoleEmployee.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleClient.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_EMPLOYEE);
            }
        });

        radioButtonUserRoleAdmin.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleClient.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_ADMINISTRATOR);
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.edit_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Récupérez les nouvelles informations de l'utilisateur depuis les champs de la boîte de dialogue

                String newEmailAddress = editTextEmailAddress.getText().toString().trim();
                String newFirstName = editTextFirstName.getText().toString().trim();
                String newLastName = editTextLastName.getText().toString().trim();
                String newUserRole = null;
                if( radioButtonUserRoleAdmin.isChecked() ){
                    newUserRole = FirebaseRepository.USER_ROLE_ADMINISTRATOR;

                } else if( radioButtonUserRoleEmployee.isChecked() ){
                    newUserRole = FirebaseRepository.USER_ROLE_EMPLOYEE;

                } else if( radioButtonUserRoleClient.isChecked() ){
                    newUserRole = FirebaseRepository.USER_ROLE_CLIENT;

                }

                // Mettez à jour l'utilisateur dans Firebase Firestore
                updateUser(user.getUid(), newEmailAddress, newFirstName, newLastName, newUserRole);
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

    private void updateUser(String uid, String emailAddress, String firstName, String lastName, String userRole){

        firebaseRepository.editAccount(uid, emailAddress, firstName, lastName, userRole)
                .thenAccept( userId -> {

                    Toast.makeText(getApplicationContext(), "Informations modifiees avec succes", Toast.LENGTH_SHORT).show();
        })
        .exceptionally(throwable -> {
                    Log.d(TAG, throwable.getMessage());
                    Toast.makeText(getApplicationContext(), "Unable to Edit user infos", Toast.LENGTH_SHORT).show();
                    return null;
        });
    }

    private void showDeleteDialog(User user) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        dialogBuilder.setTitle(String.format(getString(R.string.delete_question_text), user.getEmail()));
        dialogBuilder.setPositiveButton(getString(R.string.delete_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Supprimez à jour l'utilisateur dans Firebase Firestore
                deleteUser(user.getUid());
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

    private void deleteUser(String uid){
        firebaseRepository.deleteAccountByUid(uid).thenAccept(Void -> {
            Toast.makeText(getApplicationContext(), "Informations supprimees avec succes", Toast.LENGTH_SHORT).show();

        })        .exceptionally(throwable -> {
            Log.d(TAG, throwable.getMessage());
            Toast.makeText(getApplicationContext(), "Unable to Edit user infos", Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    @Override
    public void onEditButtonClick(User user) {
        showEditDialog(user);
    }

    @Override
    public void onDeleteButtonClick(User user) {
        showDeleteDialog(user);
    }
}