package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static android.content.ContentValues.TAG;

/**
 * Created by reesul on 4/3/2018.
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private File[] recordsList;
    private RecordGridListener mListener;

    public RecordAdapter(Context c, int columnWidth) {
        mContext = c;
        File dir = new File(Util.getImgFilepath());
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getPath();
                boolean accept = path.contains(".jpg") || path.contains(".jpeg") || path.contains(".png");
                accept = accept && pathname.length()!=0;

                return accept;
            }
        };
        recordsList = dir.listFiles(filter);
        //recordsList = removeDuplicateRecords(dir.listFiles(filter));
        Log.d(TAG, "RecordsAdapter Constructor:: " + recordsList.length + " images for gridview");
    }

    public int getCount() {
        return recordsList.length;
    }

    public Object getItem(int position) {
        return recordsList[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    //TODO use this when transitions between fragments is smoother and has memory
    private void updateRecordsList(Context context) {
        File dir = new File(Util.getImgFilepath());
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getPath();
                boolean accept = path.contains(".jpg") || path.contains(".jpeg") || path.contains(".png");
                return accept;
            }
        };
        recordsList = dir.listFiles(filter);
        //recordsList = removeDuplicateRecords(dir.listFiles(filter));
        Log.d(TAG, "updateRecordsList:: " + recordsList.length + " images for gridview");
    }

    /*
    make sure we don't have images showing up multiple times; MAY NOT NEED THIS FUNCTION

     */
    private File[] removeDuplicateRecords(File[] filesArray) {
        ArrayList<File> filesList= new ArrayList<File>();
        for (File file: filesArray) {
            if(!filesList.contains(file))
                filesList.add(file);
        }
        return (File []) filesList.toArray();
    }

    /*
    todo sort records list alphabetically
     */
    private void sortRecordsList() {

    }

    // create a new ImageView for each item referenced by the Adapter
    //todo make this more efficient by using a custom view and view holder (??), and maybe bitmaps on an another thread
    public View getView(int position, View convertView, ViewGroup parent) {

        RecordView recordView = new RecordView(mContext);
        RelativeLayout grid_item =  recordView.initRecordView((File) getItem(position), convertView, parent);



        recordView.setEventListener(new RecordView.RecordViewListener() {
            @Override
            public void onEvent() {
                //call to update may not be necessary..
                updateRecordsList(mContext);
                onRecordViewChanged();
            }
        });


        return grid_item;

    }


    //todo implement onClick, uses a dropdown menu, containing options: rename, delete, share/export
    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //todo make dropdown menu, manipulate file from there: delete, rename, share, view
            //File img = (File) v.getTag();


            Context context = v.getContext();
            PopupMenu recordMenu = new PopupMenu(context, v);
            MenuInflater menuInflater = recordMenu.getMenuInflater();
            menuInflater.inflate(R.menu.record_image_menu, recordMenu.getMenu());
            recordMenu.setOnMenuItemClickListener(recordMenuListener);

            recordMenu.show();

        }
    };


    private PopupMenu.OnMenuItemClickListener recordMenuListener = new PopupMenu.OnMenuItemClickListener() {
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        Log.d(TAG, "RecordMenu:: onClick");

        //need to get the file
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) {
            Log.d(TAG, "onClick:: null menu info");
            return false;
        }
        final File selectedImage = (File) getItem(info.position);


        Log.d(TAG, "onMenuItemClick:: file is " + selectedImage.getAbsolutePath());



        switch (item.getItemId()) {
            case R.id.record_view:
                //todo show the image; use an intent to show in gallery(?) so they can get a better look i.e. zooming in
                break;
            case R.id.record_rename:
                //todo show current file name in line, text here will replace name of file
                break;
            case R.id.record_share:
                //todo create a share intent?
                break;

            case R.id.record_delete:
                // todo delete the file, update the adapter
                //give alertdialog for confirmation
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.record_delete_confirmation);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String msg = Util.getLastPathComponent(selectedImage);
                                if(selectedImage.delete()) {

                                    Toast.makeText(mContext, msg+" deleted", Toast.LENGTH_LONG)
                                            .show();
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

    };

    public interface RecordGridListener {
        public void onEvent();
    }

    public void setEventListener(RecordGridListener listener) {
        this.mListener = listener;
    }

    private void onRecordViewChanged() {
        Log.d(TAG, "onRecordViewChanged");
        if(mListener!=null) {
            mListener.onEvent();
        }
    }

    private class ViewHolder {
        public TextView text;
        public ImageView image;
        public ImageButton options;

        public ViewHolder(View convertView) {
            text = convertView.findViewById(R.id.record_text_filename);
            image = convertView.findViewById(R.id.record_image);
            options = convertView.findViewById(R.id.record_info_button);
        }
    }


}
