package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.view.RecordAdapter;
import edu.tamu.ecen.capstone.patientmd.view.RecordView;

/**
 * Created by Reese on 3/21/2018.
 */

public class RecordFragment extends Fragment {
    //TODO show all existing records here
    //  probably use a recycler view

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
        return inflater.inflate(R.layout.record_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridView = (GridView) getActivity().findViewById(R.id.records_grid);
        final RecordAdapter adapter = new RecordAdapter(getContext(), gridView.getWidth());
        gridView.setAdapter(adapter);

        adapter.setEventListener(new RecordAdapter.RecordGridListener() {
            @Override
            public void onEvent() {
                adapter.notifyDataSetChanged();
            }
        });

    }

    public static RecordFragment newInstance() {

        return new RecordFragment();
    }

    //todo use event listener from RecordView

}
