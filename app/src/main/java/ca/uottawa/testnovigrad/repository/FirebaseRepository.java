package ca.uottawa.testnovigrad.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import ca.uottawa.testnovigrad.models.Agency;
import ca.uottawa.testnovigrad.models.ServiceDelivery;
import ca.uottawa.testnovigrad.models.User;

public class FirebaseRepository {

    private static String TAG  = FirebaseRepository.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public static String USER_COLLECTION_NAME = "users";

    public static String COMPANY_COLLECTION_NAME = "companies";

    public static String SERVICE_COLLECTION_NAME = "services";

    public static String USER_ROLE_CLIENT = "USER_CLIENT";

    public static String USER_ROLE_EMPLOYEE = "USER_EMPLOYEE";

    public static String USER_ROLE_ADMINISTRATOR = "USER_ADMIN";

    public FirebaseRepository(){


        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseFirestore = FirebaseFirestore.getInstance();

        if( this.firebaseFirestore != null)
            Log.d("FirebaseConnector", "Firestore is initialized");
    }

    /**
     * Create a new user account in Firestore
     *
     * @param emailAddress    User's email
     * @param password User's password
     * @return CompletableFuture with the UID of the newly created user
     */
    public CompletableFuture<String> createAccount(String emailAddress, String password, String firstName, String lastName, String userAuthority) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Create a new user with email and password
        firebaseAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();

                        // Once the user is created, you can store additional information in Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", emailAddress);
                        userMap.put("firstName", firstName);
                        userMap.put("lastName", lastName);
//                        userMap.put("username", username);
                        userMap.put("userAuthority", userAuthority);

                        // Add the user information to Firestore
                        firebaseFirestore.collection(USER_COLLECTION_NAME).document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> future.complete(uid))
                                .addOnFailureListener(e -> future.completeExceptionally(e));
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    public CompletableFuture<String> editAccount(String uid, String emailAddress, String firstName, String lastName, String userAuthority) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Create a new user with email and password
//        firebaseAuth. (new com.google.firebase.firestore.auth.User(uid))
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        String uid = task.getResult().getUser().getUid();

                        // Once the user is created, you can store additional information in Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("email", emailAddress);
                        userMap.put("firstName", firstName);
                        userMap.put("lastName", lastName);
//                        userMap.put("username", username);
                        userMap.put("userAuthority", userAuthority);
//                        if(userAuthority.equals(USER_ROLE_EMPLOYEE)){
//                            userMap.put("company", null);
//                        }

                        // Add the user information to Firestore
                        firebaseFirestore.collection(USER_COLLECTION_NAME).document(uid)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> future.complete(uid))
                                .addOnFailureListener(e -> future.completeExceptionally(e));
//                    } else {
//                        future.completeExceptionally(task.getException());
//                    }
//                });

        return future;
    }

    public CompletableFuture<String> loginUser(String email, String password) {
        CompletableFuture<String> future = new CompletableFuture<>();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        future.complete(task.getResult().getUser().getUid());
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }



    /**
     * Check if a user is currently authenticated.
     *
     * @return CompletableFuture with a boolean indicating whether the user is authenticated.
     */
    public CompletableFuture<Boolean> isUserAuthenticated() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Check if there is a currently authenticated user
        if (firebaseAuth.getCurrentUser() != null) {
            future.complete(true);
        } else {
            future.complete(false);
        }

        return future;
    }

    public CompletableFuture<List<User>> getAllUsers() {
        CompletableFuture<List<User>> future = new CompletableFuture<>();

        firebaseFirestore.collection(USER_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> userList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String uid = documentSnapshot.getId();
                        String email = documentSnapshot.getString("email");
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String userAuthority = documentSnapshot.getString("userAuthority");

                        User user = new User(uid, email, firstName, lastName, userAuthority);
                        userList.add(user);
                    }

                    future.complete(userList);
                })
                .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }

    public CompletableFuture<String> createAgency(Agency agency){
        CompletableFuture<String> future = new CompletableFuture<>();

        firebaseFirestore.collection(COMPANY_COLLECTION_NAME)
                .add(agency)
                .addOnSuccessListener( agencyDoc -> future.complete(agencyDoc.get().getResult().get("id").toString()))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletableFuture<String> updateAgency(Agency agency){
        CompletableFuture<String> future = new CompletableFuture<>();

        firebaseFirestore.collection(COMPANY_COLLECTION_NAME)
                .document(agency.getId())
                .set(agency)
                .addOnSuccessListener( aVoid -> future.complete(agency.getId()))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletableFuture<List<Agency>> getAllAgencies() {
        CompletableFuture<List<Agency>> future = new CompletableFuture<>();

        firebaseFirestore.collection(COMPANY_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Agency> agenceList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String id = documentSnapshot.getId();
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");
                        Date openedAt = documentSnapshot.getDate("openedAt");
                        Date closedAt = documentSnapshot.getDate("closedAt");

                        Agency agency = new Agency(id, name, address, openedAt , closedAt);
                        agenceList.add(agency);
                    }

                    future.complete(agenceList);
                })
                .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }

    public CompletableFuture<List<ServiceDelivery>> getAllServiceDeliveries() {
        CompletableFuture<List<ServiceDelivery>> future = new CompletableFuture<>();

        firebaseFirestore.collection(SERVICE_COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ServiceDelivery> serviceDeliveryList = new ArrayList<>();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String id = documentSnapshot.getId();
                        String name = documentSnapshot.getString("name");
                        String description = documentSnapshot.getString("description");

                        ServiceDelivery serviceDelivery = new ServiceDelivery(id, name, description);
                        serviceDeliveryList.add(serviceDelivery);
                    }

                    future.complete(serviceDeliveryList);
                })
                .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }

    public CompletableFuture<String> createServiceDelivery(ServiceDelivery serviceDelivery){
        CompletableFuture<String> future = new CompletableFuture<>();

        firebaseFirestore.collection(SERVICE_COLLECTION_NAME)
                .add(serviceDelivery)
                .addOnSuccessListener( serviceDeliveryJson -> future.complete(serviceDeliveryJson.get().getResult().get("id").toString()))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletableFuture<String> updateServiceDelivery(ServiceDelivery serviceDelivery){
        CompletableFuture<String> future = new CompletableFuture<>();

        firebaseFirestore.collection(SERVICE_COLLECTION_NAME)
                .document(serviceDelivery.getId())
                .set(serviceDelivery)
                .addOnSuccessListener( aVoid -> future.complete(serviceDelivery.getId()))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    /**
     * Retrouver un document par uid
     * @param collectionName
     * @param uid
     * @return
     */
    public CompletableFuture<DocumentSnapshot> retrieveDocumentByUid(String collectionName, String uid) {
        CompletableFuture<DocumentSnapshot> future = new CompletableFuture<>();

        firebaseFirestore.collection(collectionName).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> future.complete(documentSnapshot))
                .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }

    /**
     * Retrouver un Document dans Firebase
     * @param databaseReference
     * @param field
     * @param value
     * @return
     */
    public CompletableFuture<DataSnapshot> retrieveDocumentByField(DatabaseReference databaseReference, String field, String value){
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        databaseReference.orderByChild(field).equalTo(value).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                future.complete(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }

    public CompletableFuture<Void> deleteDocument(DatabaseReference databaseReference) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        databaseReference.removeValue((databaseError, databaseReference1) -> {
            if (databaseError == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    /**
     * Supprimer le compte d'un utilisateur par son UID
     *
     * @param uid Identifiant de l'utilisateur à supprimer
     * @return CompletableFuture indiquant si la suppression a réussi ou échoué
     */
    public CompletableFuture<Void> deleteAccountByUid(String uid) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Supprimez l'utilisateur de l'authentification Firebase en utilisant l'UID spécifié
//        firebaseAuth. (uid)
//                .addOnSuccessListener(aVoid -> {
                    // L'utilisateur a été supprimé de l'authentification Firebase, maintenant supprimez ses données de Firestore
                    firebaseFirestore.collection(USER_COLLECTION_NAME).document(uid)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> future.complete(null))
                            .addOnFailureListener(e -> future.completeExceptionally(e));
//                })
//                .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }


    public void logout(){
        firebaseAuth.signOut();
    }

    public FirebaseFirestore getFirebaseFirestore(){
        return this.firebaseFirestore;
    }

    public FirebaseAuth getFirebaseAuth(){
        return  this.firebaseAuth;
    }

    public CompletionStage<Void> deleteAgency(String agencyId) {

        CompletableFuture<Void> future = new CompletableFuture<>();
        firebaseFirestore.collection(COMPANY_COLLECTION_NAME).document(agencyId)
                .delete()
                .addOnSuccessListener(aVoid1 -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletionStage<Void> deleteServiceDelivery(String serviceDeliveryId) {

        CompletableFuture<Void> future = new CompletableFuture<>();
        firebaseFirestore.collection(SERVICE_COLLECTION_NAME).document(serviceDeliveryId)
                .delete()
                .addOnSuccessListener(aVoid1 -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }
}
