package edu.tamu.ecen.capstone.patientmd.util;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import edu.tamu.ecen.capstone.patientmd.database.DatabaseHelper;


public class NetworkUtil {

    public static ConnectivityManager connectivityManager;
    public static volatile boolean isUploading = false;
    public static String lastResponse = null;

    private static String TAG = "NetworkUtil";

    private static String url;
    private static String port;

    public static String getUrl() {
        if (url==null)
            //todo poll user, throw exception; handle from call
            url="something";
        return url;
    }

    public static String getPort() {
        return port;
    }

    public static void setUrl(String url, String port) {
        NetworkUtil.url = url;
        NetworkUtil.port = port;
    }

    /*
    Check if there has been a connection established
        call getActivity if calling from a fragment, or provide 'this' if calling from activity

        //TODO: figure out if needed - may not need to be fully connected if using local connection
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

    public static String GET(String address, String port, File file, Context context) {
        setUrl(address, port);
        HttpGETAsyncTask httpTask = new HttpGETAsyncTask(context);

        httpTask.execute(file);

        return null;

    }


    /*
    POST a file to the server for OCR - should be an image file
        address: url to connect to including the port number- should be http
            e.g. http://192.168.0.1:80     */
    public static String POST(String address, String port, File file, Context context) {

        setUrl(address, port);
        HttpAsyncTask httpTask = new HttpAsyncTask(context);
        httpTask.execute(file);

        String response=null;

        return response;
    }

    public static String sendFile(String address, String port, File file) throws NetworkErrorException {
        Log.d(TAG, "sendFile:: init");

        if (isUploading) {
            Log.e(TAG, "sendFile:: A file is already being sent to server");
            throw new NetworkErrorException(address+" is already in use by this app!");
        }
        isUploading = true;

        //static stuff
        String attachmentName = "image";
        String attachmentFileName = file.getName();
        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";



        try {
            //setup request
            Log.d(TAG, "sendFile:: make HTTP connection");
            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(address+":"+port);
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

            Log.d(TAG, "SendFile:: Content-Disposition: form-data; name=\"" +
                    attachmentName + "\";filename=\"" +
                    attachmentFileName + "\"" + crlf);

            //get file and write its contents
            int size = (int) file.length();
            byte[] byteMe = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(byteMe, 0, byteMe.length);
            Log.d(TAG, "Sendfile:: send data length is " + byteMe.length+" B, "+byteMe.length/1024+" KB");
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

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = responseStreamReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            responseStreamReader.close();

            String response = stringBuilder.toString();
            int rCode = httpUrlConnection.getResponseCode();
            Log.d(TAG, "sendFile:: response code: " + rCode);


            File csv = null;
            if (rCode == 200) {

                while (csv==null) {
                    httpUrlConnection.disconnect();

                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    httpUrlConnection.setRequestMethod("GET");
                    httpUrlConnection.setConnectTimeout(10000);
                    httpUrlConnection.setReadTimeout(10000);

                    httpUrlConnection.connect();

                    int responseCode = httpUrlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        line=null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        Log.d(TAG, "sendFile:: received from GET: " + sb.toString());
                        break;  //TODO create the actual file
                    }

                }

            }


            responseStream.close();
            httpUrlConnection.disconnect();

            isUploading = false;
            return response;


        } catch (Exception e) {
            Log.e(TAG, "multipart post error " + e + "(" + address + ")");
        } finally {
            isUploading = false;
        }

        return "Request failed";
    }


    private static class HttpAsyncTask extends AsyncTask<File, String, Boolean> {

        Context context;

        protected HttpAsyncTask(Context mContext) {
            context = mContext;
        }

        @Override
        protected Boolean doInBackground(File... file) {
            Boolean retVal = false;
            if (isUploading)
                return false;

            if (file.length > 1)
                Log.d(TAG, "Attempting to send multiple files at once is not supported");

            try {
                String address = getUrl();
                String port = getPort();
                File f = file[0];
                Log.d(TAG, "sendFile:: init");

                if (isUploading) {
                    Log.e(TAG, "sendFile:: A file is already being sent to server");
                }
                isUploading = true;

                //static stuff
                String attachmentName = "image";
                String attachmentFileName = file[0].getName();
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary =  "*****";



                //setup request
                Log.d(TAG, "sendFile:: make HTTP connection");
                HttpURLConnection httpUrlConnection = null;
                URL url = new URL(address+":"+port);
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

                Log.d(TAG, "SendFile:: Content-Disposition: form-data; name=\"" +
                        attachmentName + "\";filename=\"" +
                        attachmentFileName + "\"" + crlf);

                //get file and write its contents
                int size = (int) f.length();
                byte[] byteMe = new byte[size];
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
                buf.read(byteMe, 0, byteMe.length);
                Log.d(TAG, "Sendfile:: send data length is " + byteMe.length+" B, "+byteMe.length/1024+" KB");
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

                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                String response = stringBuilder.toString();
                int rCode = httpUrlConnection.getResponseCode();
                Log.d(TAG, "SendFile:: response code: " + rCode);


                File csv = null;
                int reqCount = 0;
                if (rCode == 200) {
                    Thread.sleep(15000);

                    Log.d(TAG, "POST worked, now sending GETs");

                    publishProgress(response);

                    String rawName = f.getName().split("\\.")[0];

                    String newAddress = address + ":" + port + "/" + rawName + ".csv";
                    Log.d(TAG, "GET from " + newAddress);
                    URL requestURL = new URL(newAddress);

                    //TODO provide better timeout stuff
                    while (csv==null && reqCount < 20) {
                        httpUrlConnection.disconnect();

                        httpUrlConnection = (HttpURLConnection) requestURL.openConnection();

                        int responseCode = httpUrlConnection.getResponseCode();
                        Log.d(TAG, "sendFile:: Get request "+reqCount + " received " +responseCode);
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            InputStream input = httpUrlConnection.getInputStream();

                            String csvPath = Util.getDataFilepath() + "/" + rawName + ".csv"; //todo change this to csv
                            csv = new File(csvPath);
                            Log.d(TAG, csvPath);
                            FileOutputStream output = new FileOutputStream(csv, false);

                            byte data[] = new byte[4096];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                // allow canceling with back button
                                if (isCancelled()) {
                                    input.close();
                                    return false;
                                }
                                output.write(data, 0, count);
                            }

                            Log.d(TAG, "sendFile:: received from GET: " + csv.exists());

                            break;
                        }

                        reqCount++;

                        Thread.sleep(3000);
                    }
                    Log.d(TAG, "Received CSV after " + reqCount + " attempts");
                    if (csv!=null) {
                        Log.d(TAG, csv.getName());
                        DatabaseHelper db = new DatabaseHelper(context);
                        retVal = db.ReadRecordCSV(csv);
                    }


                }


                responseStream.close();
                httpUrlConnection.disconnect();

                Log.d(TAG, response);

                isUploading = false;
                return retVal;


            } catch (MalformedURLException e) {
                Log.e(TAG, "multipart post error " + e + "(" + url + ")");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isUploading = false;
            }

            return retVal;


        }

        @Override
        protected void onPostExecute(Boolean result) {
            //TODO: make this a file for database to process
            Log.d(TAG, "HTTP task finished");
            if (!result) {
                Toast.makeText(context, "Last record sent was not interpreted by server, please take a new image", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... response) {
            Log.d(TAG, "ProgressUpdate:: Received from server:\n" + response[0]);
            //give user update on how their upload went
            String ok = response[0].contains("upload success!") ? "success" : "failure";

            Toast.makeText(context, "Upload result: " + ok, Toast.LENGTH_LONG).show();
        }

    }



    private static class HttpGETAsyncTask extends AsyncTask<File, String, Boolean> {

        private Context context;

        protected HttpGETAsyncTask(Context mContext) {
            context = mContext;
        }

        @Override
        protected Boolean doInBackground(File... files) {
            boolean retVal = false;

            if (isUploading)
                return false;

            File f = files[0];
            File csv = null;
            int reqCount = 0;
            HttpURLConnection httpUrlConnection;

            try {
                String address = getUrl();
                String port = getPort();

                Log.d(TAG, "Sending GETs");


                String rawName = f.getName().split("\\.")[0];

                String newAddress = address + ":" + port + "/" + rawName + ".csv";
                Log.d(TAG, "GET from " + newAddress);
                URL requestURL = new URL(newAddress);

                //TODO provide better timeout stuff
                while (csv == null && reqCount < 20) {


                    httpUrlConnection = (HttpURLConnection) requestURL.openConnection();

                    int responseCode = httpUrlConnection.getResponseCode();
                    Log.d(TAG, "sendFile:: Get request " + reqCount + " received " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream input = httpUrlConnection.getInputStream();

                        String csvPath = Util.getDataFilepath() + "/" + rawName + ".csv"; //todo change this to csv
                        csv = new File(csvPath);
                        Log.d(TAG, csvPath);
                        FileOutputStream output = new FileOutputStream(csv, false);

                        byte data[] = new byte[4096];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            // allow canceling with back button
                            if (isCancelled()) {
                                Log.d(TAG, "cancelled request");
                                input.close();
                                return false;
                            }
                            output.write(data, 0, count);
                        }

                        Log.d(TAG, "sendFile:: received from GET: " + csv.exists());

                        break;
                    }

                    reqCount++;

                    Thread.sleep(3000);
                    httpUrlConnection.disconnect();
                }

                Log.d(TAG, "Received CSV after " + reqCount + " attempts");

                if (csv != null) {
                    Log.d(TAG, csv.getName());
                    DatabaseHelper db = new DatabaseHelper(context);
                    retVal = db.ReadRecordCSV(csv);
                }


                } catch(MalformedURLException e){
                    Log.e(TAG, "multipart post error " + e + "(" + url + ")");
                } catch(Exception e){
                    e.printStackTrace();
                } finally{
                    isUploading = false;
                }


                isUploading = false;
                return retVal;


            }

        @Override
        protected void onPostExecute(Boolean result) {
            //TODO: make this a file for database to process
            Log.d(TAG, "HTTP task finished");
            if (!result) {
                Toast.makeText(context, "Last record sent was not interpreted by server, please take a new image", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... response) {
            Log.d(TAG, "ProgressUpdate:: Received from server:\n" + response[0]);
            //give user update on how their upload went
            String ok = response[0].contains("upload success!") ? "success" : "failure";

            Toast.makeText(context, "Upload result: " + ok, Toast.LENGTH_LONG).show();
        }

    }



}
