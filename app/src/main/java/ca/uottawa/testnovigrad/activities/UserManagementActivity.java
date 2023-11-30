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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import ca.uottawa.testnovigrad.R;
import ca.uottawa.testnovigrad.fwk.OnEntityModelButtonClickListener;
import ca.uottawa.testnovigrad.fwk.UserAdapter;
import ca.uottawa.testnovigrad.models.User;
import ca.uottawa.testnovigrad.repository.FirebaseRepository;
import ca.uottawa.testnovigrad.repository.SharedPreferencesRepository;

public class UserManagementActivity extends AppCompatActivity implements OnEntityModelButtonClickListener<User> {

    private static String TAG = UserManagementActivity.class.getName();

    private User currentUser;

    private UserAdapter userAdapter;
    private List<User> users = new ArrayList<>();

    private FirebaseRepository firebaseRepository;

    private SharedPreferencesRepository sharedPreferencesRepository;

    private RecyclerView recyclerView;

    private ImageButton mainNavigationButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton floatingActionButtonAddUser;
    private View.OnClickListener addUserListener = new View.OnClickListener(

    ) {
        @Override
        public void onClick(View v) {
            showNewUserDialog();
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
        setContentView(R.layout.activity_user_management);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        Toolbar toolbar = findViewById(R.id.toolbar_user_management);
        setSupportActionBar(toolbar);

        floatingActionButtonAddUser = findViewById(R.id.fabAddUser);
        mainNavigationButton = findViewById(R.id.btn_user_management_navto_main);

        redirectToTargetByGivenUserRole(getApplicationContext());

        recyclerView = findViewById(R.id.recyclerUsersView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainNavigationButton.setOnClickListener(navigateToMainListener);
        floatingActionButtonAddUser.setOnClickListener(addUserListener);

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

    private void showNewUserDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);
        dialogBuilder.setView(dialogView);

        TextInputEditText editTextEmailAddress = dialogView.findViewById(R.id.user_edit_email_address_input);
        TextInputLayout boxPassword = dialogView.findViewById(R.id.tilUserManagementPassword);
        TextInputEditText editTextPassword = dialogView.findViewById(R.id.user_edit_password_input);
        TextInputLayout boxConfirmPassword = dialogView.findViewById(R.id.tilUserManagementConfirmPassword);
        TextInputEditText editTextConfirmPassword = dialogView.findViewById(R.id.user_edit_confirm_password_input);
        TextInputEditText editTextFirstName = dialogView.findViewById(R.id.user_edit_firstname_input);
        TextInputEditText editTextLastName = dialogView.findViewById(R.id.user_edit_lastname_input);
        RadioButton radioButtonUserRoleClient = dialogView.findViewById(R.id.user_edit_radio_user_role_client);
        RadioButton radioButtonUserRoleEmployee = dialogView.findViewById(R.id.user_edit_radio_user_role_employee);
        RadioButton radioButtonUserRoleAdmin = dialogView.findViewById(R.id.user_edit_radio_user_role_admin);

        User user = new User();
        editTextEmailAddress.setText(user.getEmail());
        editTextFirstName.setText(user.getFirstName());
        editTextLastName.setText(user.getLastName());
        user.setUserAuthority(FirebaseRepository.USER_ROLE_EMPLOYEE);

        boxPassword.setVisibility(View.VISIBLE);
        boxConfirmPassword.setVisibility(View.VISIBLE);

        radioButtonUserRoleClient.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleEmployee.setChecked(false);
                radioButtonUserRoleAdmin.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_CLIENT);
            }
        });

        radioButtonUserRoleEmployee.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleClient.setChecked(false);
                radioButtonUserRoleAdmin.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_EMPLOYEE);
            }
        });

        radioButtonUserRoleAdmin.setOnCheckedChangeListener( (buttonView, isChecked) -> {

            if(isChecked){
                radioButtonUserRoleClient.setChecked(false);
                radioButtonUserRoleEmployee.setChecked(false);
                user.setUserAuthority(FirebaseRepository.USER_ROLE_ADMINISTRATOR);
            }
        });

        dialogBuilder.setPositiveButton(getString(R.string.add_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Récupérez les nouvelles informations de l'utilisateur depuis les champs de la boîte de dialogue

                String userRole = null;
                if( radioButtonUserRoleAdmin.isChecked() ){
                    userRole = FirebaseRepository.USER_ROLE_ADMINISTRATOR;

                } else if( radioButtonUserRoleEmployee.isChecked() ){
                    userRole = FirebaseRepository.USER_ROLE_EMPLOYEE;

                } else if( radioButtonUserRoleClient.isChecked() ){
                    userRole = FirebaseRepository.USER_ROLE_CLIENT;

                }

                if(isFormValid( editTextEmailAddress, editTextPassword, editTextConfirmPassword, editTextFirstName, editTextLastName)){
                    createUser( editTextEmailAddress.getText().toString().trim(),
                            editTextPassword.getText().toString().trim(),
                            editTextFirstName.getText().toString().trim(),
                            editTextLastName.getText().toString().trim(),
                            userRole);

                }else{
                    Toast.makeText(UserManagementActivity.this, "Veuillez remplir tous les champs de ce formulaire!", Toast.LENGTH_SHORT).show();

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
    // Méthode pour afficher la boîte de dialogue de modification
    private void showEditUserDialog(User user) {
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

        TextInputLayout boxPassword = dialogView.findViewById(R.id.tilUserManagementPassword);
        TextInputLayout boxConfirmPassword = dialogView.findViewById(R.id.tilUserManagementConfirmPassword);

        boxPassword.setVisibility(View.GONE);
        boxConfirmPassword.setVisibility(View.GONE);

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

    private void createUser(String emailAddress, String password, String firstName, String lastName, String userRole){

        firebaseRepository.createAccount(
                emailAddress.trim(),
                password.trim(),
                firstName.trim(),
                lastName.trim(),
                userRole
        ).thenAccept(userId -> {

            loadUsers(recyclerView);

        }).exceptionally(throwable -> {
            Log.e(TAG,"Error creating user account: " + throwable.getMessage());
            Toast.makeText(UserManagementActivity.this, "Une Erreur est survenue lors de la creation du compte. Veuillez contacter l'administrateur ", Toast.LENGTH_SHORT).show();
            return null;
        });
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
        showEditUserDialog(user);
    }

    @Override
    public void onDeleteButtonClick(User user) {
        showDeleteDialog(user);
    }

    private Boolean isFormValid(TextInputEditText emailAddressEditText, TextInputEditText passwordEditText, TextInputEditText confirmPasswordEditText, TextInputEditText firstNameEditText, TextInputEditText lastNameEditText){

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
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
}