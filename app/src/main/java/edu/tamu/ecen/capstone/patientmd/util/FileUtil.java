package edu.tamu.ecen.capstone.patientmd.util;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileUtil {

    //data needed to access dropbox API
    private static final String DROPBOX_KEY = "yrrbgz06t4netbc";
    private static final String DROPBOX_SECRET = "yrrbgz06t4netbc";
    private static final String DROPBOX_TOKEN = "BBLbh4uhW0AAAAAAAAAEd3XCUydNA8yreZ4CxMa1VZfbyuBDAVzMJ3sTnCZHA4NZ";

    private static DbxRequestConfig config;
    private static DbxClientV2 client;

    private static String TAG = "FileUtil";


    public static void initDropbox() {



        config = DbxRequestConfig.newBuilder("patientMD_V1.0").build();
        client = new DbxClientV2(config, DROPBOX_TOKEN);

        try {
            FullAccount account = client.users().getCurrentAccount();
            System.out.println(account.getName().getDisplayName());

            ListFolderResult result = client.files().listFolder("");
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    //System.out.println(metadata.getPathLower());
                    Log.d(TAG, "File in dropbox: " + metadata.getPathDisplay());
                }

                if (!result.getHasMore()) {
                    break;
                }

                result = client.files().listFolderContinue(result.getCursor());
            }

        } catch (DbxException dbe) {
            Log.e(TAG, "InitDropbox:: error: ",dbe);
        }

    //todo figure out how to send and receive from dropbox


    }


    /*
    upload all existing records to dropbox
     */
    public static boolean dropboxUploadAllRecords(File files[]) {
// Upload "test.txt" to Dropbox
        Log.d(TAG, "dropboxUploadAllRecords: " + files.length);
        try {

            for (File record : files) {
                Log.d(TAG, "upload file "+record.getName() + " to dropbox");
                InputStream in = new FileInputStream(record);
                String path = "/records/"+record.getName();
                FileMetadata metadata = client.files().uploadBuilder(path).uploadAndFinish(in);

                Log.d(TAG, metadata.getName());
            }
        }
        catch (FileNotFoundException fne)
        {
            fne.printStackTrace();
            return false;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        catch (DbxException dbxe)
        {
            dbxe.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean dropboxUploadRecord(File file) {
        Log.d(TAG, "dropboxUploadRecord: " );
        try {

                InputStream in = new FileInputStream(file);
                String path = "/records/"+file.getName();
                FileMetadata metadata = client.files().uploadBuilder(path).uploadAndFinish(in);

                Log.d(TAG, metadata.getName());
        }
        catch (FileNotFoundException fne)
        {
            fne.printStackTrace();
            return false;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return false;
        }
        catch (DbxException dbxe)
        {
            dbxe.printStackTrace();
            return false;
        }

        return true;
    }

    /*
    Downloads all files of the specified type to the directory at the pathname
    //todo generalize this more
    @param dir: Directory in dropbox to download from
    @param type: file extension (ie file type) that we want to download
     */
    public static boolean dropboxDownload(String dir, String type) {


        File dest = new File(Util.getDataFilepath());
        dest.mkdirs();
        Log.d(TAG, "dropboxDownload: download to path: "+dest.getAbsolutePath());

        try {
            ListFolderResult result = client.files().listFolder(dir);
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    //System.out.println(metadata.getPathLower());
                    String filename = metadata.getName();
                    if (filename.contains(type)) {
                        Log.d(TAG, "dropboxDownload: download to: "+dest+"/"+filename);
                        File file = new File(dest, filename);

                        OutputStream os = new FileOutputStream(file);

                        client.files().downloadBuilder(dir+filename).download(os);
                        if(file.length()>0) {
                            //if this is true, likely that the download was successful
                            Log.d(TAG, "Download of "+filename+" successful with bytes: "+filename.length());
                        }
                    }
                    else continue;

                }

                if (!result.getHasMore()) {
                    break;
                }

                result = client.files().listFolderContinue(result.getCursor());
            }

        } catch (DbxException dbe) {
            Log.e(TAG, "DropboxDownload: error: ",dbe);
            return false;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "DropboxDownload: FileNotFound: ", e);
            return false;
        }
        catch (IOException ioe) {
            Log.e(TAG, "DropboxDown: IOexception: ", ioe);
            return false;
        }


        return true;
    }

    /*
    Download all CSV files in a given directory with in the dropbox file path
    Saves the files to an app-specific folder whose path is at Util.getDataFilepath()

    @param dir: directory in dropbox to download from
        /records/ contains images of records
        /data/ contains csv files of processed data
     */
    public static boolean downloadCSV(String dir) {
        File dest = new File(Util.getDataFilepath());
        dest.mkdirs();
        Log.d(TAG, "dropboxDownload from path: "+dest.getAbsolutePath());

        try {
            ListFolderResult result = client.files().listFolder(dir);
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    String filename = metadata.getName();

                    Log.d(TAG, "dropboxDownload: download to: "+dest+"/"+filename);
                    File file = new File(dest, filename);

                    OutputStream os = new FileOutputStream(file);

                    client.files().downloadBuilder(dir+filename).download(os);
                    if(file.length()>0) {
                        //if this is true, likely that the download was successful
                        Log.d(TAG, "Download of "+filename+" successful!");
                    }

                    else continue;

                }

                if (!result.getHasMore()) {
                    break;
                }

                result = client.files().listFolderContinue(result.getCursor());
            }

        } catch (DbxException dbe) {
            Log.e(TAG, "DropboxDownload: error: ",dbe);
            return false;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "DropboxDownload: FileNotFound: ", e);
            return false;
        }
        catch (IOException ioe) {
            Log.e(TAG, "DropboxDown: IOexception: ", ioe);
            return false;
        }


        return true;
    }

}
