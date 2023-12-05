package ca.uottawa.testnovigrad.fwk;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.Timestamp;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import ca.uottawa.testnovigrad.models.User;

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

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Timestamp timestamp = convertJsonElementToTimestamp(json);
            if( timestamp != null){
                return timestamp.toDate();
            }else{
                return null;
            }
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

    public static <T> void showDialogWithMultipleSelection(
            Context context,
            List<T> items,
            Function<T, String> extractor,
            String titleText,
            String cancelText,
            String okText,
            final OnMultiSelectListener<T> listener,
            DialogInterface.OnClickListener onCancelListener,
            DialogInterface.OnClickListener onOkListener,
            DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener
    ) {
        boolean[] checkedItems = new boolean[items.size()];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleText);

        builder.setMultiChoiceItems(
                convertListToArray(items, extractor),
                checkedItems,
                onMultiChoiceClickListener
        );

        builder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<T> selectedItems = new ArrayList<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedItems.add(items.get(i));
                    }
                }
                if (listener != null) {
                    listener.onMultiSelect(selectedItems);
                }
                if (onOkListener != null) {
                    onOkListener.onClick(dialog, which);
                }
            }
        });

        builder.setNegativeButton(cancelText, onCancelListener);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static <T> CharSequence[] convertListToArray(List<T> items, Function<T, String> displayTextExtractor) {
        CharSequence[] displayTexts = new CharSequence[items.size()];

        for (int i = 0; i < items.size(); i++) {
            displayTexts[i] = displayTextExtractor.apply(items.get(i));
        }

        return displayTexts;
    }
}