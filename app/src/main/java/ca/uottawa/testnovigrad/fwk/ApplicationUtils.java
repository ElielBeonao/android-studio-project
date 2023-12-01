package ca.uottawa.testnovigrad.fwk;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.protobuf.TimestampOrBuilder;

import java.lang.reflect.Type;
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

    public static class DateDeserializer implements JsonDeserializer<Date> {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Timestamp timestamp = convertJsonElementToTimestamp(json);
            if( timestamp != null){
                return timestamp.toDate();
            }else{
                return null;
            }
//            String dateString = json.getAsString();
//            if (dateString != null && !dateString.isEmpty()) {
//                try {
//                    return dateFormat.parse(dateString);
//                } catch (ParseException e) {
//                    throw new JsonParseException(e);
//                }
//            } else {
//                return null;
//            }
        }
    }

    private static Timestamp convertJsonElementToTimestamp(JsonElement jsonElement) {
        try {
            String timestampString;

            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                timestampString = jsonElement.getAsString();
            } else {
                throw new JsonParseException("Invalid JsonElement type or format");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = dateFormat.parse(timestampString);

            return new Timestamp(date);
        } catch (ParseException | IllegalStateException e) {
            throw new JsonParseException("Error parsing timestamp from JsonElement", e);
        }
    }
}