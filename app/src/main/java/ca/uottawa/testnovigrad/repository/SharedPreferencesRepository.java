package ca.uottawa.testnovigrad.repository;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.Map;

import ca.uottawa.testnovigrad.activities.LoginActivity;
import ca.uottawa.testnovigrad.models.User;

public class SharedPreferencesRepository {

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor sharedPreferencesEditor;

    private int PRIVATE_MODE = 0;

    private String PREF_NAME = "APP_NOVIGRAD";

    private String CURRENT_USER_ID_KEY = "USER_ID";

    private String CURRENT_USER_KEY = "CURRENT_USER";

    private Context context;

    public SharedPreferencesRepository(Context context){
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        this.sharedPreferencesEditor = sharedPreferences.edit();
    }

    public String getCurrentUserID(){
        return this.sharedPreferences.getString(CURRENT_USER_KEY, null);
    }

    public void setCurrentUserID(String currentUserId){
        this.sharedPreferencesEditor.putString(CURRENT_USER_KEY, currentUserId);
        this.sharedPreferencesEditor.commit();
    }

    public void setCurrentUser(User currentUser){
        this.sharedPreferencesEditor.putString(CURRENT_USER_KEY,new Gson().toJson(currentUser));
        this.sharedPreferencesEditor.commit();
    }

    public User getCurrentUser(){
        String userJson = this.sharedPreferences.getString(CURRENT_USER_KEY, null);
        if( userJson == null)
            return null;

        return new Gson().fromJson(userJson, User.class);
    }

    public void logoutUser(Activity activity) {

        sharedPreferencesEditor.clear();
        sharedPreferencesEditor.commit();

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        activity.finish();
    }
}
