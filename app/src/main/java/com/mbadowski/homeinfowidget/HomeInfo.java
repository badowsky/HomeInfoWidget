package com.mbadowski.homeinfowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link HomeInfoConfigureActivity HomeInfoConfigureActivity}
 */
public class HomeInfo extends AppWidgetProvider implements OnTaskCompleted {
    private static final String TAG = "MOJ_TAG";
    static final String address = new String("http://dom.mbadowski.pl/homeheart/api/all");
    static Boolean enabled = false;
    static RemoteViews views;
    static int[] appWidgetIds;
    static AppWidgetManager appWidgetManager;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.views = new RemoteViews(context.getPackageName(), R.layout.home_info);
        this.appWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        if(enabled == false){
            onEnabled(context);
            Log.d(TAG, "Enabled manualy...");
        }
        Log.d("MOJ_TAG", "onUpdate");
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadMeasurementsTask(this).execute(address);
        } else {
            Log.d(TAG, "Internet connection disabled");
        }
        updateAppWidget();






    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            HomeInfoConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        enabled = true;
        // Enter relevant functionality for when the first widget is created
        Log.d("MOJ_TAG", "onEnable");
        // Register an onClickListener
        Intent intent = new Intent(context, HomeInfo.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button_update, pendingIntent);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget() {
        // Instruct the widget manager to update the widget
        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }

    @Override
    public void onTaskComplete(JSONObject JSONobj) {
        try {
            Log.d(TAG, "Taks complete");
            views.setTextViewText(R.id.temp_in, JSONobj.getJSONObject("measurements").getString("temperature_inside") + "\u00B0C");
            views.setTextViewText(R.id.temp_out, JSONobj.getJSONObject("measurements").getString("temperature_outside") + "\u00B0C");
            views.setTextViewText(R.id.humidity, JSONobj.getJSONObject("measurements").getString("humidity") + "%");
            updateAppWidget();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}


