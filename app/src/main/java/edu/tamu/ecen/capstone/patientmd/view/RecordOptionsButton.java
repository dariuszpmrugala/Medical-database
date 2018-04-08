package edu.tamu.ecen.capstone.patientmd.view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import java.io.File;

public class RecordOptionsButton extends AppCompatButton {



    //File associated with the button
    File record;

    public RecordOptionsButton(Context context, AttributeSet attrs, File record) {
        super(context, attrs);
        this.record = record;
    }

    public File getRecord() {
        return record;
    }

    public void setRecord(File record) {
        this.record = record;
    }


}