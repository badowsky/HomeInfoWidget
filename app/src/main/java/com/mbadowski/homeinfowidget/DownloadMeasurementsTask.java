package com.mbadowski.homeinfowidget;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Bador on 2015-01-29.
 */

class DownloadMeasurementsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "MOJ_TAG";

    ConnectivityManager connMgr = null;
    NetworkInfo networkInfo = null;

    OnTaskCompleted listener;

    public DownloadMeasurementsTask(OnTaskCompleted listener){
        this.listener = listener;
    }
        @Override
        protected String doInBackground(String[] urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject JSONobj = new JSONObject(result);
                Log.d(TAG, "Result: " + result);
                Log.d(TAG, "JSON: " + JSONobj.toString());
                Log.d(TAG, "Temperaure in: " + JSONobj.getJSONObject("measurements").getString("temperature_inside"));
                Log.d(TAG, "Temperaure out: " + JSONobj.getJSONObject("measurements").getString("temperature_outside"));
                Log.d(TAG, "Humidity: " + JSONobj.getJSONObject("measurements").getString("humidity"));
                listener.onTaskComplete(JSONobj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = getStringFromInputStream(is);
            Log.d(TAG, contentAsString);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
