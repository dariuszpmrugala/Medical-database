package edu.tamu.ecen.capstone.patientmd.util;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
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

    public static String POST(String address, File file) {

        setUrl(address);
        new HttpAsyncTask().execute(file);
        //address = "http://255.255.255.255:80";
        /*
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


        try {
            Socket socket = new Socket(host, port);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        */

        return "because the internet";
    }

    public static String sendFile(String address, File file){
        Log.d(TAG, "sendFile:: init");

        //static stuff
        String attachmentName = "image";
        String attachmentFileName = file.getName();
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        isUploading = true;

        try {
            //setup request
            Log.d(TAG, "sendFile:: make HTTP connection");
            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(address);
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpUrlConnection.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + boundary);

            //start content wrapper
            DataOutputStream request = new DataOutputStream(
                    httpUrlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    attachmentName + "\";filename=\"" +
                    attachmentFileName + "\"" + crlf);
            request.writeBytes(crlf);

            //get file and write its contents
            int size = (int) file.length();
            byte[] byteMe = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(byteMe, 0, byteMe.length);
            buf.close();

            request.write(byteMe);

            //end content wrapper
            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary +
                    twoHyphens + crlf);

            //flush output buffer
            request.flush();
            request.close();

            //get response
            InputStream responseStream = new
                    BufferedInputStream(httpUrlConnection.getInputStream());

            BufferedReader responseStreamReader =
                    new BufferedReader(new InputStreamReader(responseStream));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();



            responseStream.close();
            httpUrlConnection.disconnect();

            isUploading = false;
            return response;


        } catch (Exception e) {
            Log.e(TAG, "multipart post error " + e + "(" + address + ")");
        }

        return null;
    }


    //TODO: this; need to provide a file to send
    private static class HttpAsyncTask extends AsyncTask<File, Void, String> {
        @Override
        protected String doInBackground(File... file) {
            if (isUploading)
                return "upload in progress, try again later";
            

            String response = sendFile(getUrl(), file[0]);
            Log.d(TAG, response);

            return response;
        }
    }


}
