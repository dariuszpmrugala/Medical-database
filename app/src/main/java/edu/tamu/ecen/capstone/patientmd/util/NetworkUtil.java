package edu.tamu.ecen.capstone.patientmd.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class NetworkUtil {

    public static ConnectivityManager connectivityManager;
    public static boolean isUploading = false;

    private static String TAG = "NetworkUtil";

    private static String url;

    public static String getUrl() {
        if (url==null)
            //todo poll user, throw exception; handle from call
            url="something";
        return url;
    }

    public static void setUrl(String url) {
        NetworkUtil.url = url;
    }

    /*
    Check if there has been a connection established
        call getActivity if calling from a fragment, or provide 'this' if calling from activity
     */
    public static boolean isConnected(Activity activity) {
        boolean connected = false;
        if (connectivityManager==null) {
            connectivityManager = (ConnectivityManager) activity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            connected = true;

        return connected;
    }

    public static String POST(File file) {


        //address = "http://255.255.255.255:80";
        HttpURLConnection urlConnection=null;
        try {
            //setup basic connection
            URL url = new URL(getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");

            //create streams from
            OutputStream outHttp = new BufferedOutputStream(urlConnection.getOutputStream());
            InputStream inHttp  = new BufferedInputStream(urlConnection.getInputStream());
            FileInputStream fileInputStream = new FileInputStream(file);

            //send the file through a buffer to the output stream (HTTP)
            byte[] buffer = new byte[1024];
            int len=0;

            while ((len = fileInputStream.read(buffer)) > 0) {
                outHttp.write(buffer);
            }
            outHttp.close();
            fileInputStream.close();


        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }


//        try {
//            Socket socket = new Socket(host, port);
//
//        } catch (IOException e) {
//            Log.e(TAG, e.toString());
//        }

        return "because the internet";
    }


    //TODO: this; need to provide a file to send
    private static class HttpAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            return null;
        }
    }


}
