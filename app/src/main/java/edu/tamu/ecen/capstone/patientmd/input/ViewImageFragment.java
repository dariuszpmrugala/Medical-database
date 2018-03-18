package edu.tamu.ecen.capstone.patientmd.input;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

import edu.tamu.ecen.capstone.patientmd.R;

import static android.content.ContentValues.TAG;


/**
 * Created by Reese on 3/18/2018.
 */

public class ViewImageFragment extends Fragment {

    //File to display in the image view
   private static File mFile;

   private final String TAG = "ViewImageFragment: ";


    public static ViewImageFragment newInstance(File file) {
        mFile = file;
        return new ViewImageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");


        LinearLayout linearLayout= new LinearLayout(this.getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        //ImageView Setup
        ImageView imageView = new ImageView(this.getContext());

        Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());

        imageView.setImageBitmap(bitmap);

        return imageView;
    }

}
