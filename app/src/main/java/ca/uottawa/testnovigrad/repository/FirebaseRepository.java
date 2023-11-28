package ca.uottawa.testnovigrad.repository;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirebaseRepository {

    private static String TAG  = FirebaseRepository.class.getName();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public static String USER_COLLECTION_NAME = "users";

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

    public CompletableFuture<Boolean> isAuthenticationTokenValid(String token){
        CompletableFuture<Boolean> future = new CompletableFuture<>();

//        firebaseAuth.is

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
}
