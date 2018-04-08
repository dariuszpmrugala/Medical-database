package edu.tamu.ecen.capstone.patientmd.util;

import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;


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
                    System.out.println(metadata.getPathLower());
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


}
