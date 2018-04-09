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
import java.util.Iterator;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static edu.tamu.ecen.capstone.patientmd.util.Const.RECORD_VIEW_SCALE;

/**
 * Created by reesul on 4/3/2018.
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<File> recordsList;
    private RecordAdapterListener mListener;

    private final String TAG = "RecordAdapter";

    public RecordAdapter(Context c, int columnWidth) {
        mContext = c;


        recordsList = new ArrayList<>(Arrays.asList(getFilesInDir()));
        //recordsList = removeDuplicateRecords(dir.listFiles(filter));
        Log.d(TAG, "RecordsAdapter Constructor:: " + recordsList.size() + " images for gridview");
    }

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

    public long getItemId(int position) {
        return 0;
    }

    private void updateRecordsList() {
        addNewRecords();
        removeOldRecords();
        onRecordDataChanged();

        Log.d(TAG, "updateRecordsList:: " + recordsList.size() + " images for gridview");
    }

    private void addNewRecords() {
        File dir = new File(Util.getImgFilepath());

        File[] records = getFilesInDir();
        for(File file : records) {
            if (!recordsList.contains(file)) {
                recordsList.add(file);
            }
        }
    }

    private void removeOldRecords() {
        File[] dir = getFilesInDir();

        //need to find files that are still in the record list, but are no longer present in file system
        for (Iterator<File> iterator = recordsList.iterator(); iterator.hasNext(); ) {
            File recordFile = iterator.next();
            boolean remove = true;
            for (File realFile: dir) {
                if (realFile.getAbsolutePath().equals(recordFile.getAbsolutePath())) {
                    remove = false;
                }
            }
            if (remove) iterator.remove();
        }
    }


    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        //RecordView recordView = new RecordView(mContext);
        //RelativeLayout grid_item =  recordView.initRecordView((File) getItem(position), convertView, parent);

        ViewHolder viewHolder;
        View view = convertView;
        File curFile = (File) getItem(position);

        if(view == null) {
            Log.d(TAG, "getView:: inflating "+curFile.getName());
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            view = (RelativeLayout) inflater.inflate(R.layout.record_image_layout, parent, false);
            viewHolder = new ViewHolder(view);

            int width = Util.getDeviceWidth() / 2;
            int height = (int) (Util.getDeviceHeight() / RECORD_VIEW_SCALE);
            GridView.LayoutParams params = new GridView.LayoutParams(width, height);
            //need to make sure height is the same across grid items so scrolling works
            view.setLayoutParams(params);

            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }


        //update viewHolder's actual data

        Bitmap bitmap = Util.recordImageTable.get(curFile.getName());
        if(bitmap!=null) {

            viewHolder.image.setImageBitmap(bitmap);
            viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);


            viewHolder.options.setTag(curFile);
            viewHolder.options.setOnClickListener(buttonClick);
        }
        viewHolder.text.setText(curFile.getName());


/*
        recordView.setEventListener(new RecordView.RecordViewListener() {
            @Override
            public void onEvent() {
                //call to update may not be necessary..
                updateRecordsList(mContext);
                onRecordDataChanged();
            }
        });
*/

        return view;

    }


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
                        //get file to view
                        Uri recordUri = FileProvider.getUriForFile(mContext,
                                "edu.tamu.ecen.capstone.input.fileprovider",
                                record);

                        //create an intent so this opens in another app
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
                    AlertDialog.Builder builderRename = new AlertDialog.Builder(mContext);
                    builderRename.setTitle("New File Name");

                    // Set up the input
                    final EditText input = new EditText(mContext);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builderRename.setView(input);

                    // Set up the buttons
                    builderRename.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //in this case, we need to rename the existing file
                            String fileExtension = record.getName();
                            fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."));
                            String newName = input.getText().toString();
                            newName = Util.getImgFilepath()+"/"+newName+fileExtension;

                            File renamedFile = new File(newName);
                            if (record.renameTo(renamedFile)) {
                                Log.d(TAG, "File renamed correctly, and old file deleted: "+!record.exists());

                                Util.replaceInTable(record.getName(), renamedFile.getName());
                                record = renamedFile;

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

                case R.id.record_share:
                    ShareActionProvider shareActionProvider = (ShareActionProvider) item.getActionProvider();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("image/*");

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
        public void onEvent();
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

    private class ViewHolder {
        public TextView text;
        public ImageView image;
        public ImageButton options;

        public RelativeLayout view;

        public ViewHolder(View convertView) {
            view = (RelativeLayout) convertView;
            text = (TextView) view.findViewById(R.id.record_text_filename);
            image = (ImageView) view.findViewById(R.id.imageview_record_image);
            options = (ImageButton) view.findViewById(R.id.record_info_button);
        }
    }




}
