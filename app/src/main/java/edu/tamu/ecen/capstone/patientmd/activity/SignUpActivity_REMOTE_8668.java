package edu.tamu.ecen.capstone.patientmd.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseEntry;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseHelper;

public class SignUpActivity extends AppCompatActivity {

    private Button login;
    private Button signup;
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    public static String username;
    public static final String COL_1 = "ID";
    public static final String COL_2 = "DATE";
    public static final String COL_3 = "TESTS";
    public static final String COL_4 = "RESULT";
    public static final String COL_5 = "UNITS";
    public static final String COL_6 = "REFERENCE_INTERVAL";
    public static final String TABLE_NAME = "PMD_table";
    private EditText editText_username;
    private EditText editText_password;
    private TextView error_login;
    private DatabaseHelper myDb;
    boolean isRealUser;
    boolean isCorrectPassword;
    boolean usernameAlreadyExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editText_username = findViewById(R.id.username);
        editText_password = findViewById(R.id.password);
        error_login = findViewById(R.id.error_login);

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent newActivity = new Intent(SignUpActivity.this, MainActivity.class);

                isRealUser = false;
                isCorrectPassword = false;

                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Log.d("tag", snapshot.getKey().toString());
                            if (snapshot.getKey().toString().matches(editText_username.getText().toString())) {
                                Log.d("tag", "username matches");
                                isRealUser = true;
                                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                    if (snapshot2.getKey().toString().matches("Password")) {
                                        if (snapshot2.getValue().toString().matches(editText_password.getText().toString())) {
                                            Log.d("tag", "password matches");
                                            isCorrectPassword = true;
                                        }
                                    }
                                }
                            }
                        }

                        Log.d("tag", "we are here");
                        if (isRealUser && isCorrectPassword) {

                            DatabaseReference ref = rootRef.child(editText_username.getText().toString());
                            username = editText_username.getText().toString();

                            myDb = new DatabaseHelper(getApplicationContext());
                            myDb.deleteAllData();

                            ref.child("Entries")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            DatabaseEntry entry = new DatabaseEntry();
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Log.d("tag", snapshot.getKey());
                                                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                                    Log.d("tag", snapshot2.getKey() + " " + snapshot2.getValue());
                                                    if (snapshot2.getKey().toString().matches("Date")) {
                                                        entry.setDate(snapshot2.getValue().toString());
                                                    } else if (snapshot2.getKey().toString().matches("Reference Interval")) {
                                                        entry.setReference_interval(snapshot2.getValue().toString());
                                                    } else if (snapshot2.getKey().toString().matches("Tests")) {
                                                        entry.setTests(snapshot2.getValue().toString());
                                                    } else if (snapshot2.getKey().toString().matches("Units")) {
                                                        entry.setUnits(snapshot2.getValue().toString());
                                                    } else if (snapshot2.getKey().toString().matches("Result")) {
                                                        entry.setResult((snapshot2.getValue().toString()));
                                                    }
                                                }

                                                SQLiteDatabase db = myDb.getWritableDatabase();

                                                // building an object to be inserted
                                                ContentValues contentValues = new ContentValues();
                                                // the ID field will increase by 1 for each entry
                                                contentValues.put(COL_1, (int) (long) myDb.getProfilesCount() + 1);
                                                contentValues.put(COL_2, entry.getDate());
                                                contentValues.put(COL_3, entry.getTests());
                                                contentValues.put(COL_4, entry.getResult());
                                                contentValues.put(COL_5, entry.getUnits());
                                                contentValues.put(COL_6, entry.getReference_interval());

                                                db.insert(TABLE_NAME, null, contentValues);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                            error_login.setText("");

                            startActivity(newActivity);
                        }
                        else if (!isRealUser) {
                            error_login.setText("Username does not exist. Sign up to add username.");
                        }
                        else if (!isCorrectPassword) {
                            error_login.setText("Username and password do not match.");
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

        signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameAlreadyExist = false;

                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Log.d("tag", snapshot.getKey().toString());
                            if (snapshot.getKey().toString().matches(editText_username.getText().toString())) {
                                usernameAlreadyExist = true;
                                error_login.setText("An account with this username already exists.");
                                break;
                            }
                        }

                        if (!usernameAlreadyExist) {
                            if (editText_username.getText().toString().isEmpty() || editText_username.getText().toString().contains(".")
                                    || editText_username.getText().toString().contains("#") || editText_username.getText().toString().contains("$")
                                    || editText_username.getText().toString().contains("[") || editText_username.getText().toString().contains("]")) {
                                error_login.setText("Please enter a valid username. Username cannot contain '.', '#', '$', '[' or ']'.");
                            }
                            else if (editText_password.getText().toString().isEmpty()) {
                                error_login.setText("Please enter a valid password.");
                            }
                            else {
                                DatabaseReference ref = rootRef.child(editText_username.getText().toString());
                                ref.child("Password").setValue(editText_password.getText().toString());
                                ref.child("Entries").child("ID").setValue(0);
                                username = editText_username.getText().toString();
                                error_login.setText("");
                                Toast.makeText(getApplicationContext(), "Account successfully created", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



            }
        });
    }

}
