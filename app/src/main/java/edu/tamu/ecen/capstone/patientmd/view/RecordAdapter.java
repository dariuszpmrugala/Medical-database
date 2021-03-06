package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Const;
import edu.tamu.ecen.capstone.patientmd.util.FileUtil;
import edu.tamu.ecen.capstone.patientmd.util.NetworkUtil;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static edu.tamu.ecen.capstone.patientmd.util.Const.RECORD_VIEW_SCALE;

/**
 * Created by reesul on 4/3/2018.
 *
 *
 * TODO add sorting options
 * https://stackoverflow.com/questions/7723872/apply-sorting-in-adapter-in-grid-layout
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<File> recordsList;
    private RecordAdapterListener mListener;

    private final String TAG = "RecordAdapter";

    public RecordAdapter(Context c, int columnWidth) {
        mContext = c;


        recordsList = new ArrayList<>(Arrays.asList(getFilesInDir()));
        Collections.sort(recordsList, compareByModifiedTime);
        Log.d(TAG, "RecordsAdapter Constructor:: " + recordsList.size() + " images for gridview");
    }

    /*
    Gets all files within the directory we expect images to be in.
    Applies a filter to keep only certain images types
     */
    private File[] getFilesInDir() {
        File dir = new File(Util.getImgFilepath());
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                boolean accept = path.contains(".jpg") || path.contains(".jpeg") || path.contains(".png");
                accept = accept && pathname.length()!=0;

                return accept;
            }
        };

        return dir.listFiles(filter);
    }

    public int getCount() {
        return recordsList.size();
    }

    public Object getItem(int position) {
        return recordsList.get(position);
    }

    //No ID's implemented
    public long getItemId(int position) {
        return 0;
    }

    /*
    Updates the records list.
    Adds new records, removes records whose files are deleted, and notifies the view to update itself
     */
    private void updateRecordsList() {
        addNewRecords();
        removeOldRecords();
        onRecordDataChanged();

        checkArraylist();
        Collections.sort(recordsList, compareByModifiedTime);
        checkArraylist();

        Log.d(TAG, "updateRecordsList:: " + recordsList.size() + " images for gridview");
    }

    /*
    Adds new records to the record list
    If any files exist in the actual files system but are not in the records list yet, add them
     */
    private void addNewRecords() {
        File dir = new File(Util.getImgFilepath());

        File[] records = getFilesInDir();
        for(File file : records) {
            if (!recordsList.contains(file)) {
                recordsList.add(file);
            }
        }
    }

    /*
    Removes any records from the array/list whose files no longer exist in their stored form
        e.g., filename changed, file deleted
     */
    private void removeOldRecords() {
        File[] dir = getFilesInDir();

        //need to find files that are still in the record list, but are no longer present in file system
        for (Iterator<File> iterator = recordsList.iterator(); iterator.hasNext(); ) {
            File recordFile = iterator.next();
            /*
            boolean remove = true;
            for (File realFile: dir) {
                if (realFile.getAbsolutePath().equals(recordFile.getAbsolutePath())) {
                    remove = false;
                }
            }
            if (remove) iterator.remove();
            */
            if (!recordFile.exists()) iterator.remove();
        }
    }

    /*
    Callback to get the views needed for each item in the grid
        Contains an ImageView, TextView, and ImageButton, stored in a ViewHolder to avoid
        repeated inflater and findViewById calls

        References a hashtable for bitmaps using filenames as keys, scaled down to reduce computation time
     */
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder viewHolder;
        View view = convertView;
        File curFile = (File) getItem(position);

        if(view == null) {
            Log.d(TAG, "getView:: inflating "+curFile.getName());
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            view = (RelativeLayout) inflater.inflate(R.layout.record_image_layout, parent, false);
            viewHolder = new ViewHolder(view);

            //Need all items in the grid to be the same height, so set that explicitly based on device characteristics
            int width = Util.getDeviceWidth() / 2;
            int height = (int) (Util.getDeviceHeight() / RECORD_VIEW_SCALE);
            GridView.LayoutParams params = new GridView.LayoutParams(width, height);
            //need to make sure height is the same across grid items so scrolling works
            view.setLayoutParams(params);

            //Tag lets the view hold onto this viewHolder
            view.setTag(viewHolder);
        }
        else {
            //if the view is recycled, get the viewHolder from the tag
            viewHolder = (ViewHolder) view.getTag();
        }

        /*update viewHolder's actual data   */

        //bitmap is a hash table whos keys are file names
        Bitmap bitmap = Util.recordImageTable.get(curFile.getName());
        if (bitmap==null) {
            Util.updateRecordTable();
        }
        bitmap = Util.recordImageTable.get(curFile.getName());
        if(bitmap!=null) {
            //setup the image for the given record
            viewHolder.image.setImageBitmap(bitmap);
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            //setup the button for managing the record
            viewHolder.options.setTag(curFile);
            viewHolder.options.setOnClickListener(buttonClick);

            //Display the record's filename
            viewHolder.text.setText(curFile.getName());
        }

        return view;
    }


    /*
    Listened for the options button
        creates a popup menu and its corresponding listener
     */
    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File img = (File) v.getTag();

            Context context = v.getContext();
            PopupMenu recordMenu = new PopupMenu(context, v);
            MenuInflater menuInflater = recordMenu.getMenuInflater();
            menuInflater.inflate(R.menu.record_image_menu, recordMenu.getMenu());

            optionsClickListener listener = new optionsClickListener(img);

            recordMenu.setOnMenuItemClickListener(listener);
            recordMenu.show();

        }
    };

    /*
    new private class for the file management button
        needs to be private so a File can be associated with it

        Current management options: view, share, rename, delete
     */
    private class optionsClickListener implements PopupMenu.OnMenuItemClickListener {

        File record;

        public optionsClickListener(File file) {
            record = file;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.d(TAG, "RecordMenu:: onClick");


            switch (item.getItemId()) {
                case R.id.record_view:
                    try {
                        Log.d(TAG, "View record: "+record.getAbsolutePath());
                        //get URI for file to view
                        Uri recordUri = FileProvider.getUriForFile(mContext,
                                "edu.tamu.ecen.capstone.input.fileprovider",
                                record);

                        //create an intent so this opens in another app for viewing
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(recordUri, "image/*");
                        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(viewIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "Attempt to view record: ",e);
                    }
                    break;

                case R.id.record_rename:
                    //todo pull up the keyboard for the user
                    //todo show old filename (selected so it can be overwritten easily)
                    final String name = record.getName();

                    //AlertDialog will show; user inputs text here
                    AlertDialog.Builder builderRename = new AlertDialog.Builder(mContext);
                    builderRename.setTitle("New File Name");

                    // Set up the input
                    final EditText input = new EditText(mContext);
                    // Specify the type of input expected
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builderRename.setView(input);

                    // Set up the buttons
                    builderRename.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //in this case, we need to rename the existing file
                            //User does not need to provide file extension.. use the old one
                            String fileExtension = name.substring(name.lastIndexOf("."));
                            String newName = input.getText().toString();
                            newName = Util.getImgFilepath()+"/"+newName+fileExtension;
                            Log.d(TAG, "RecordRename:: renaming file to "+newName);

                            //check if new filename is already present as another file's name
                            //TODO make it more obvious to the user that the file name is invalid
                            if(Util.recordImageTable.get(Util.getLastPathComponent(newName)) != null) {
                                Log.d(TAG, "RecordRename:: file " + newName + " already exists! Try another name");
                                Toast.makeText(mContext, "File by that name already exists! Try another name", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            File renamedFile = new File(newName);

                            if (record.renameTo(renamedFile)) {
                                //replace the key for this file's bitmap with the new key (new file name)
                                Util.replaceInTable(name, renamedFile.getName());
                                record = renamedFile;

                                //update the records table in another thread
                                AsyncTask.execute(Util.runnableUpdateTable);
                                updateRecordsList();

                            }

                        }
                    });
                    builderRename.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builderRename.show();

                    break;

                case R.id.record_upload:
                    NetworkUtil.POST(Const.ADDRESS, Const.PORT, record, mContext);

                    break;

                case R.id.record_download:
                    NetworkUtil.GET(Const.ADDRESS, Const.PORT, record, mContext);

                    break;

                case R.id.record_share:
                    //ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();

                    //create intent for sending the file
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("image/*");

                    //use file provider to get a URI for the file
                    Uri fileUri = FileProvider.getUriForFile(mContext,
                            "edu.tamu.ecen.capstone.input.fileprovider", record);

                    sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    mContext.startActivity(Intent.createChooser(sendIntent, "Share via"));
                    break;

                case R.id.record_delete:
                    //give alertdialog for confirmation
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.record_delete_confirmation);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msg = record.getName();
                            if(record.delete()) {

                                Toast.makeText(mContext, msg+" deleted", Toast.LENGTH_LONG)
                                        .show();
                                updateRecordsList();
                            }
                            else
                                Log.d(TAG, "Failed to delete " + msg);
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                    break;

                default:
                    break;


            }
            return false;
        }
    }


    public interface RecordAdapterListener {
        void onEvent();
    }

    public void setEventListener(RecordAdapterListener listener) {
        this.mListener = listener;
    }

    private void onRecordDataChanged() {
        Log.d(TAG, "onRecordDataChanged");
        if(mListener!=null) {
            mListener.onEvent();
        }
    }

    private Comparator<File> compareByModifiedTime = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            long timestamp1 = o1.lastModified();
            long timestamp2 = o2.lastModified();
            int retVal = timestamp1 > timestamp2 ? -1 : 1;
            //Log.d(TAG, "compareByModifiedTime:: " + o1.getName() + " vs. " + o2.getName());

            //Log.d(TAG, "compareByModifiedTime::  "+ timestamp1 +" vs. " + timestamp2 + " : " + retVal);
            if (timestamp1 == timestamp2)
                return 0;
            return retVal;

        }
    };

    private void checkArraylist(){
        for (int i = 0; i < recordsList.size(); i++) {
            Log.d(TAG, recordsList.get(i).getName());
        }
    }

    /*
    ViewHolder maintains the objects needed for the views
        Reduces need to call findViewById  every time view is recycled
     */
    private class ViewHolder {
        public TextView text;
        public ImageView image;
        public ImageButton options;

        public RelativeLayout view;

        //us findViewById only when we create this viewHolder
        public ViewHolder(View convertView) {
            view = (RelativeLayout) convertView;
            text = (TextView) view.findViewById(R.id.record_text_filename);
            image = (ImageView) view.findViewById(R.id.imageview_record_image);
            options = (ImageButton) view.findViewById(R.id.record_info_button);
        }
    }




}
