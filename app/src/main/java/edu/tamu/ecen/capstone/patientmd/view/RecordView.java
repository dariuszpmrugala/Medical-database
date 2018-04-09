package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Util;



public class RecordView extends View {

    private Context mContext;
    private File record;
    private String recordPath;
    private RelativeLayout layout;
    private RecordViewListener mListener;
    private TextView textView;
    private ImageView imageView;
    private ImageButton imageButton;

    private final String TAG = "RecordView";

    public RecordView(Context context) {
        super(context);
        mContext=context;

        //initRecordView();
    }



    public RelativeLayout initRecordView(File record, View convertView, ViewGroup parent) {
        RelativeLayout grid_item;
        this.record = record;
        recordPath = this.record.getAbsolutePath();

        if(convertView==null) {
            Log.d(TAG, "initRecordView:: " + record.getName());
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            grid_item = (RelativeLayout) inflater.inflate(R.layout.record_image_layout, parent, false);


            int width = Util.getDeviceWidth() / 2;
            int height = (int) (Util.getDeviceHeight() / 2.5);
            GridView.LayoutParams params = new GridView.LayoutParams(width, height);
            //need to make sure height is the same across grid items so scrolling works
            grid_item.setLayoutParams(params);

            imageView = (ImageView) grid_item.findViewById(R.id.imageview_record_image);
            textView = (TextView) grid_item.findViewById(R.id.record_text_filename);
            imageButton = (ImageButton) grid_item.findViewById(R.id.record_info_button);

            //set the data members for the view
            updateView();


                 /*
                imageView.setLayoutParams(new ViewGroup.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setAdjustViewBounds(true);
                */
            grid_item.setLayoutParams(params);
        }
        else {
            grid_item = (RelativeLayout) convertView;
        }

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

            Log.d(TAG, "onMenuItemClick:: file is " + record.getAbsolutePath());


            switch (item.getItemId()) {
                case R.id.record_view:
                    //todo show the image; use an intent to show in gallery(?) so they can get a better look i.e. zooming in
                    Toast.makeText(mContext, "Not yet implemented", Toast.LENGTH_LONG)
                            .show();
                    break;
                case R.id.record_rename:
                    final String newName="";
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
                            String fileExtension = Util.getLastPathComponent(record);
                            fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."));
                            String newName = input.getText().toString();
                            newName = Util.getImgFilepath()+"/"+newName+fileExtension;

                            File renamedFile = new File(newName);
                            if (record.renameTo(renamedFile)) {
                                Log.d(TAG, "File renamed correctly, and old file deleted: "+!record.exists());
                                record = renamedFile;
                                recordPath = record.getAbsolutePath();

                                onRecordItemChanged();
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
                    //todo create a share intent?
                    Toast.makeText(mContext, "Not yet implemented", Toast.LENGTH_LONG)
                            .show();
                    break;

                case R.id.record_delete:
                    // todo delete the file, update the adapter
                    //give alertdialog for confirmation
                    AlertDialog.Builder builderDelete = new AlertDialog.Builder(mContext);
                    builderDelete.setMessage(R.string.record_delete_confirmation);
                    builderDelete.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msg = Util.getLastPathComponent(record);
                            if (record.delete()) {

                                Toast.makeText(mContext, msg + " deleted", Toast.LENGTH_LONG)
                                        .show();

                                onRecordItemChanged();
                                //todo make the adapater update the list

                            } else
                                Log.d(TAG, "Failed to delete " + msg);
                        }
                    });
                    builderDelete.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builderDelete.show();
                    break;

                default:
                    break;


            }
            return false;
        }
    };

    public void updateView() {
        //todo make the bitmap an asynchronous task; probably what is slowing down the app
        Bitmap bitmap = BitmapFactory.decodeFile(record.getAbsolutePath());
        if(bitmap!=null) {
            long time= System.currentTimeMillis();

            imageView.setImageBitmap(bitmap);
            time = System.currentTimeMillis() - time;
            Log.d(TAG, "milliseconds to update bitmap: " + time);

            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            textView.setText(Util.getLastPathComponent(record));

            imageButton.setOnClickListener(buttonClick);
        }


    }




    /*
    create event mListener interface so fragment knows how to update the adapter list
     */
    public interface RecordViewListener {
        void onEvent();
    }

    public void setEventListener(RecordViewListener listener) {
        this.mListener = listener;
    }

    protected void onRecordItemChanged() {
        Log.d(TAG, "OnRecordItemChanged::");
        if(mListener!=null) {
            mListener.onEvent();
        }
    }




}
