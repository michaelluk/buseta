package com.alvinhkh.buseta.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alvinhkh.buseta.Constants;
import com.alvinhkh.buseta.R;
import com.alvinhkh.buseta.provider.SuggestionProvider;
import com.alvinhkh.buseta.provider.SuggestionTable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class UpdateSuggestionService extends IntentService {

    private static final String TAG = "UpdateSuggestion";

    SharedPreferences mPrefs;

    public UpdateSuggestionService() {
        super("UpdateSuggestionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Log.d(TAG, "onHandleIntent");
        // Check internet connection
        final ConnectivityManager conMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            sendUpdate(R.string.message_no_internet_connection);
            return;
        }
        // start fetch available bus route
        sendUpdate(R.string.message_database_updating);
        Ion.with(getApplicationContext())
                .load(Constants.URL.ROUTE_AVAILABLE)
                .setHeader("X-Requested-With", "XMLHttpRequest")
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        // do stuff with the result or error
                        if (null != e)
                            Log.e(TAG, e.toString());
                        if (null != result && result.size() > 0) {
                            JsonObject object = result.get(0).getAsJsonObject();
                            if (null != object && object.has("r_no")) {
                                // clear existing suggested routes
                                getContentResolver().delete(SuggestionProvider.CONTENT_URI,
                                        SuggestionTable.COLUMN_TYPE + "=?",
                                        new String[]{SuggestionTable.TYPE_DEFAULT});
                                String routes = object.get("r_no").getAsString();
                                String[] routeArray = routes.split(",");
                                ContentValues[] contentValues = new ContentValues[routeArray.length];
                                for (int i = 0; i < routeArray.length; i++) {
                                    ContentValues values = new ContentValues();
                                    values.put(SuggestionTable.COLUMN_TEXT, routeArray[i]);
                                    values.put(SuggestionTable.COLUMN_TYPE, SuggestionTable.TYPE_DEFAULT);
                                    values.put(SuggestionTable.COLUMN_DATE, "0");
                                    contentValues[i] = values;
                                }
                                int insertedRows = getContentResolver().bulkInsert(
                                        SuggestionProvider.CONTENT_URI, contentValues);
                                if (null != mPrefs) {
                                    // update record version number
                                    // this number is to trigger update automatically
                                    SharedPreferences.Editor editor = mPrefs.edit();
                                    editor.putInt(Constants.PREF.VERSION_RECORD, Constants.ROUTES.VERSION);
                                    editor.apply();
                                }
                                if (insertedRows > 0) {
                                    Log.d(TAG, "updated available routes suggestion: " + insertedRows);
                                } else {
                                    Log.d(TAG, "error when inserting available routes to database");
                                }
                                sendUpdate(R.string.message_database_updated);
                            }
                        }
                    }
                });
    }

    private void sendUpdate(int resourceId) {
        Intent intent = new Intent(Constants.ROUTES.SUGGESTION_UPDATE);
        intent.putExtra(Constants.ROUTES.SUGGESTION_UPDATE, true);
        intent.putExtra(Constants.ROUTES.MESSAGE_ID, resourceId);
        sendBroadcast(intent);
    }

}