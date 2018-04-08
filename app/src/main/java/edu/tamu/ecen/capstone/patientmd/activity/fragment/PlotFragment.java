package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.FileUtil;

/**
 * Created by Reese on 3/21/2018.
 */

public class PlotFragment extends Fragment {

    private static final String TAG = "PlotFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = getView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return inflater.inflate(R.layout.plot_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //initRecordView(view);
        //Log.d(TAG, "Trying dropbox: worked? "+ tryDropbox());

    }

    public static PlotFragment newInstance() {
        return new PlotFragment();
    }


    public boolean tryDropbox() {
        try {
            FileUtil.initDropbox();

        } catch (Exception e) {
            Log.e(TAG, "tryDropbox", e);
            return false;
        }

        return true;
    }
}
