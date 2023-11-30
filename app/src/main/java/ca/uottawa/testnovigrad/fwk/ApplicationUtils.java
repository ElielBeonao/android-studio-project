package ca.uottawa.testnovigrad.fwk;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ca.uottawa.testnovigrad.activities.AgencyManagementActivity;

public class ApplicationUtils {

    private static String TAG = ApplicationUtils.class.getName();


    public static Date convertTimeStringToDate(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            return sdf.parse(timeString);
        } catch (ParseException e) {
            Log.e(TAG, "Unable to convert in Time:"+ e.getMessage());
            return null;
        }
    }

    public static String convertToDateTimeString(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            // Formater la date en tant que chaîne de texte
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Unable to convert in String:"+ e.getMessage());
            return null;
        }
    }
}
