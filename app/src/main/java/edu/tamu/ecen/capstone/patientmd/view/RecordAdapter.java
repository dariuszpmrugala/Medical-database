package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static android.content.ContentValues.TAG;

/**
 * Created by reesul on 4/3/2018.
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private File[] recordsList;

    public RecordAdapter(Context c) {
        mContext = c;
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

    public void updateRecordsList(Context context) {
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
        Log.d(TAG, "RecordsAdapter Constructor:: " + recordsList.length + " images for gridview");
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            //root should be parent??
            convertView = inflater.inflate(R.layout.record_image_layout, null);

            // if it's not recycled, initialize some attributes


             /*
            imageView.setLayoutParams(new ViewGroup.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setAdjustViewBounds(true);
            */
        } else {
        }

        //setup the image view here with an image taken from the file
        imageView = convertView.findViewById(R.id.imageview_record_image);
        TextView textView = (TextView) convertView.findViewById(R.id.textview_filename);

        File imgFile = recordsList[position];
        Log.d(TAG, "getView:: filename: " + imgFile.getPath());
        if(imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if(bitmap==null) {//set blank image here, do not set text
                imageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_action_info));
                textView.setText("Empty image file");
            }

            else {
                imageView.setImageBitmap(bitmap);
                textView.setText(imgFile.getPath());
            }
        }
        //imageView.setImageResource(recordsList[position]);
        return convertView;
    }

    //todo implement onClick
}
