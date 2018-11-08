package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.support.annotation.Nullable;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.app.AlertDialog;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.tamu.ecen.capstone.patientmd.database.*;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
<<<<<<< HEAD
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
=======
>>>>>>> 8a7efc0ecd8773a8eb06949114dc9786604aa4a3
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.util.MedicalSample;

import edu.tamu.ecen.capstone.patientmd.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DatabaseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DatabaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatabaseFragment extends Fragment {

    private String TAG = "DatabaseFragment";

    private DatabaseHelper myDb;
    private EditText editID, editDate, editTests, editResult, editUnits, editReference_Interval, editField, editText;
    private Button btnAdd, btnView, btnDelete, btnUpdate, btnQuery;
    private List<MedicalSample> medical_samples = new ArrayList<>();
    private List<DatabaseEntry> entries = new ArrayList<>();
    private DatabaseReference rootRef;

    private OnFragmentInteractionListener mListener;

    public DatabaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
    */
    // TODO: Rename and change types and number of parameters
    public static DatabaseFragment newInstance() {
        DatabaseFragment fragment = new DatabaseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getContext();
        myDb = new DatabaseHelper(context);
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = getView();
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }

        return inflater.inflate(R.layout.content_database, container, false);
//
//        TextView textView = new TextView(getActivity());
//        textView.setText(R.string.hello_blank_fragment);
//        return textView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // ConstraintLayout cl = view.findViewById(R.layout.content_database);

        editID = view.findViewById(R.id.editText_id);
        editDate = view.findViewById(R.id.editText_date);
        editTests = view.findViewById(R.id.editText_tests);
        editResult = view.findViewById(R.id.editText_result);
        editUnits = view.findViewById(R.id.editText_units);
        editReference_Interval = view.findViewById(R.id.editText_reference_interval);
        editField = view.findViewById(R.id.editText_field);
        editText = view.findViewById(R.id.editText_text);

        btnAdd = view.findViewById(R.id.button_add);
        btnView = view.findViewById(R.id.button_view);
        btnUpdate = view.findViewById(R.id.button_update);
        btnDelete = view.findViewById(R.id.button_delete);
        btnQuery = view.findViewById(R.id.button_query);


        AddData();
        ViewAll();
        UpdateData();
        DeleteData();
        QueryData();

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void ReadMedicalData() {
        Log.d(TAG, "ReadMedicalData:: Begin");
        InputStream is = getResources().openRawResource(R.raw.data);
        if (is==null) {
            Log.d(TAG, "FUCK INPUTSTREAM");
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";

        medical_samples.clear();

        try {
            reader.readLine();

            while ( (line = reader.readLine()) != null) {
                Log.d(TAG, "ReadMedicalDatabase:: " + line);
                String[] tokens = line.split(",");

                MedicalSample sample = new MedicalSample();
                sample.setDate(tokens[0]);
                sample.setTests(tokens[1]);

                if (tokens[2].length() > 0)
                    sample.setResult(tokens[2]);
                else sample.setResult("NA");

                if (tokens[3].length() > 0)
                    sample.setUnits(tokens[3]);
                else
                    sample.setUnits("NA");

                if (tokens[4].length() > 0 && tokens.length >= 5)
                    sample.setReference_interval(tokens[4]);
                else
                    sample.setReference_interval("NA");

                medical_samples.add(sample);
            }
        } catch (IOException e) {
            Log.wtf("DatabaseActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

<<<<<<< HEAD


=======
>>>>>>> 8a7efc0ecd8773a8eb06949114dc9786604aa4a3
    public void DeleteData() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer deletedRows = 0;

                        if (editID.getText().toString().matches("") || editID.getText().toString().matches("ID")) {
                            deletedRows = myDb.deleteAllData();
                        }

                        else {
                            deletedRows = myDb.deleteData(editID.getText().toString());
                        }

//                        if (deletedRows > 0)
//                            Toast.makeText(DatabaseActivity.this, "Data Deleted", Toast.LENGTH_LONG).show();
//                        else
//                            Toast.makeText(DatabaseActivity.this, "Data not Deleted", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void UpdateData() {
        btnUpdate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isUpdate = myDb.updateData(editID.getText().toString(),
                                editDate.getText().toString(),
                                editTests.getText().toString(),
                                editResult.getText().toString(),
                                editUnits.getText().toString(),
                                editReference_Interval.getText().toString());
//                        if(isUpdate)
//                            Toast.makeText(DatabaseActivity.this,"Data Update",Toast.LENGTH_LONG).show();
//                        else
//                            Toast.makeText(DatabaseActivity.this,"Data not Updated",Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public void AddData() {
        btnAdd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isInserted = false;

                        if (editDate.getText().toString().matches("") || editDate.getText().toString().matches("DATE")
                                || editTests.getText().toString().matches("") || editTests.getText().toString().matches("TESTS")
                                || editResult.getText().toString().matches("") || editResult.getText().toString().matches("RESULT")
                                || editUnits.getText().toString().matches("") || editUnits.getText().toString().matches("UNITS")
                                || editReference_Interval.getText().toString().matches("") || editReference_Interval.getText().toString().matches("REFERENCE INTERVAL")) {

                            ReadMedicalData();

                            boolean added[] = new boolean[medical_samples.size()];

                            for (int i = 0; i < medical_samples.size(); ++i) {
                                isInserted = myDb.insertData(
                                        medical_samples.get(i).getDate(),
                                        medical_samples.get(i).getTests(),
                                        medical_samples.get(i).getResult(),
                                        medical_samples.get(i).getUnits(),
                                        medical_samples.get(i).getReference_interval()
                                );

                                added[i] = isInserted;
                            }

                            isInserted = true;
                            for (int i = 0; i < added.length; ++i) {
                                if (!added[i])
                                    isInserted = false;
                            }

                        }

                        else {
                            isInserted = myDb.insertData(editDate.getText().toString(),
                                    editTests.getText().toString(),
                                    editResult.getText().toString(),
                                    editUnits.getText().toString(),
                                    editReference_Interval.getText().toString());
                        }

                        //if (isInserted)
                        //    Toast.makeText(DatabaseActivity.this, "Data Inserted", Toast.LENGTH_LONG).show();
                        //else
                        //   Toast.makeText(DatabaseActivity.this, "Data not Inserted", Toast.LENGTH_LONG).show();

                    }
                }
        );
    }

    public void ViewAll() {
        btnView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb.getAllData();
                        if(res.getCount() == 0) {
                            // show message
                            ShowMessage("Error","Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            buffer.append("ID :"+ res.getString(0)+"\n");
                            buffer.append("DATE :"+ res.getString(1)+"\n");
                            buffer.append("TESTS :"+ res.getString(2)+"\n");
                            buffer.append("RESULT :"+ res.getString(3)+"\n");
                            buffer.append("UNITS :"+ res.getString(4)+"\n");
                            buffer.append("REFERENCE INTERVAL :"+ res.getString(5)+"\n\n");
                        }

                        // Show all data
                        ShowMessage("Data",buffer.toString());
                    }
                }
        );
    }

    public void QueryData() {
        btnQuery.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res = myDb.getAllData();
                        if(res.getCount() == 0) {
                            myDb.pullData();
                            // show message
                            ShowMessage("Error","Nothing found");
                            return;
                        }

                        entries.clear();
                        entries = myDb.queryData(editField.getText().toString(), editText.getText().toString());

                        if(entries.size() == 0) {
                            // show message
                            ShowMessage("Error","Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < entries.size(); ++i) {
                            buffer.append("ID :"+ entries.get(i).getId()+"\n");
                            buffer.append("DATE :"+ entries.get(i).getDate()+"\n");
                            buffer.append("TESTS :"+ entries.get(i).getTests()+"\n");
                            buffer.append("RESULT :"+ entries.get(i).getResult()+"\n");
                            buffer.append("UNITS :"+ entries.get(i).getUnits()+"\n");
                            buffer.append("REFERENCE INTERVAL :"+ entries.get(i).getReference_interval()+"\n\n");
                        }

                        // Show all data
                        ShowMessage("Data",buffer.toString());
                    }
                }
        );
    }

    public void ShowMessage(String title,String Message){
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }


<<<<<<< HEAD
    /*
   TODO: see if this actually works; just copying in Jonathan's code here to work for a new csv from OCR server
       May have some problems since this is to be accessed from another thread

       @param csv: a csv file containing properly parsed results for a lab record
    */
    private void ReadRecordCSV(File csv) {
        Log.d(TAG, "ReadMedicalData:: Begin");
        //InputStream is = getResources().openRawResource(R.raw.data);
        if (!csv.getName().contains("csv"))
            Log.e(TAG, "input csv file is bad!!!");

        String line = "";

        try {
            InputStream is = new FileInputStream(csv);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            medical_samples.clear();


            reader.readLine();

            while ( (line = reader.readLine()) != null) {
                Log.d(TAG, "ReadMedicalDatabase:: " + line);
                String[] tokens = line.split(",");

                MedicalSample sample = new MedicalSample();
                sample.setDate(tokens[0]);
                sample.setTests(tokens[1]);

                if (tokens[2].length() > 0)
                    sample.setResult(tokens[2]);
                else sample.setResult("NA");

                if (tokens[3].length() > 0)
                    sample.setUnits(tokens[3]);
                else
                    sample.setUnits("NA");

                if (tokens[4].length() > 0 && tokens.length >= 5)
                    sample.setReference_interval(tokens[4]);
                else
                    sample.setReference_interval("NA");

                medical_samples.add(sample);
            }
        } catch (IOException e) {
            Log.wtf("DatabaseActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

=======
>>>>>>> 8a7efc0ecd8773a8eb06949114dc9786604aa4a3

}
