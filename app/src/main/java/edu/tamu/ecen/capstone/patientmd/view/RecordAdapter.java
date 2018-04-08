package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.Util;

import static android.content.ContentValues.TAG;

/**
 * Created by reesul on 4/3/2018.
 */

public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private File[] recordsList;
    private int maxHeight=0;
    private int maxWidth=0;

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
        setHeight();
        maxWidth = columnWidth;
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
    //todo make this more efficient by using a custom view and view holder (??)
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout grid_item;
        if (convertView == null) {
            Log.d(TAG, "getView:: on item "+position);
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            grid_item = (RelativeLayout) inflater.inflate(R.layout.record_image_layout, null);


            int width = Util.getDeviceWidth()/2;
            int height  = (int) (Util.getDeviceHeight()/2.7);
            GridView.LayoutParams params = new GridView.LayoutParams(width,height);
            //need to make sure height is the same across grid items so scrolling works
            Log.d(TAG, "getView:: parent height is " +params.height);
            grid_item.setLayoutParams(params);

            ImageView imageView = (ImageView) grid_item.findViewById(R.id.imageview_record_image);
            TextView textView = (TextView) grid_item.findViewById(R.id.textview_filename);
            ImageButton optionsButton = (ImageButton) grid_item.findViewById(R.id.record_info_button);

            File imgFile = recordsList[position];
            if(imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if(bitmap!=null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    textView.setText(Util.getLastPathComponent(imgFile));

                    optionsButton.setOnClickListener(buttonClick);
                    optionsButton.setTag(imgFile);
                }

                grid_item.setLayoutParams(params);
            }

             /*
            imageView.setLayoutParams(new ViewGroup.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setAdjustViewBounds(true);
            */
        } else {
            grid_item = (RelativeLayout) convertView;
        }
        return grid_item;
    }

    private void setHeight() {
        for (File image : recordsList) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

//Returns null, sizes are in the options variable
            BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            int height = options.outHeight;

            if(height>maxHeight) maxHeight = height;

        }
    }


    //todo implement onClick, uses a dropdown menu, containing options: rename, delete, share/export
    View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //todo make dropdown menu, manipulate file from there: delete, rename, share, view
            File img = (File) v.getTag();
            Toast.makeText(v.getContext(), "Filename: "+img.getAbsolutePath(), Toast.LENGTH_LONG)
                    .show();
        }
    };
}
