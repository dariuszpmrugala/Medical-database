package edu.tamu.ecen.capstone.patientmd.activity.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseEntry;
import edu.tamu.ecen.capstone.patientmd.database.DatabaseHelper;
import edu.tamu.ecen.capstone.patientmd.plot.PlotActivity;
import edu.tamu.ecen.capstone.patientmd.plot.PlotField;
import edu.tamu.ecen.capstone.patientmd.util.FileUtil;

/**
 * Created by Reese on 3/21/2018.
 */

public class PlotFragment extends Fragment {

    private static final String TAG = "PlotFragment";

    private DatabaseHelper myDb;
    private Spinner spinner_tests;
    private Spinner spinner_dates;
    private GraphView line_chart;
    private PlotField plot_field;
    private Button lowDateBtn;
    private Button highDateBtn;

    static final int date_low_id = 0;
    static final int date_high_id = 1;

    static DatePickerDialog.OnDateSetListener date_high_listener, date_low_listener;

    public static PlotFragment newInstance() {
        return new PlotFragment();
    }

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
        return inflater.inflate(R.layout.activity_plot, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myDb = new DatabaseHelper(getContext());

        spinner_tests = view.findViewById(R.id.spinner_tests);
        spinner_dates = view.findViewById(R.id.spinner_dates);
        line_chart = view.findViewById(R.id.line_chart);

        lowDateBtn = view.findViewById(R.id.button_date_low);
        highDateBtn = view.findViewById(R.id.button_date_high);

        plot_field = new PlotField();

        setListeners(view);

        String first_test = SpinnerTests();
        SpinnerDates();
        DateHigh();
        DateLow();

        // set default plot fields
        Date low_date = new Date();
        Date high_date = new Date();

        SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
        Calendar calendar = Calendar.getInstance();
        high_date = calendar.getTime();
        calendar.add(Calendar.MONTH, -6);
        low_date = calendar.getTime();

        lowDateBtn.setText(format.format(low_date));
        plot_field.setDate_low(format.format(low_date));
        highDateBtn.setText(format.format(high_date));
        plot_field.setDate_high(format.format(high_date));

        plot_field.setTest(first_test);

        // plot default plot
        Plot(view);

    }

    public List<DatabaseEntry> OrderEntries(List<DatabaseEntry> entries) {

        DatabaseEntry temp;
        Date date1 = new Date();
        Date date2 = new Date();

        for (int i = 0; i < entries.size() - 1; ++i) {
            for (int j = 0; j < entries.size() - 1 - i; ++j) {
                SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(format.parse(entries.get(j).getDate()));
                    date1 = calendar.getTime();

                    calendar.setTime(format.parse(entries.get(j + 1).getDate()));
                    date2 = calendar.getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (date2.getTime() < date1.getTime()) {
                    temp = entries.get(j + 1);
                    entries.set(j + 1, entries.get(j));
                    entries.set(j, temp);
                }
            }
        }

        return entries;
    }

    public double MaxResult(List<DatabaseEntry> entries) {
        double max = Double.parseDouble(entries.get(0).getResult());
        for (int i = 1; i < entries.size(); ++i) {
            if (max < Double.parseDouble(entries.get(i).getResult()))
                max = Double.parseDouble(entries.get(i).getResult());
        }

        return max;
    }

    public double MinResult(List<DatabaseEntry> entries) {
        double min = Double.parseDouble(entries.get(0).getResult());
        for (int i = 1; i < entries.size(); ++i) {
            if (min > Double.parseDouble(entries.get(i).getResult()))
                min = Double.parseDouble(entries.get(i).getResult());
        }

        return min;
    }

    public double[] ReferenceInterval(DatabaseEntry entry) {
        double[] interval = new double[2];
        String interval_string = entry.getReference_interval();
        String interval_low = "";

        if (interval_string.contains("<")) {
            for (int i = 1; i < interval_string.length(); ++i) {
                interval_low = interval_low + interval_string.charAt(i);
            }

            interval[0] = Double.parseDouble(interval_low);
            interval[1] = -1.0;
        }

        else if (interval_string.contains(">")) {
            for (int i = 1; i < interval_string.length(); ++i) {
                interval_low = interval_low + interval_string.charAt(i);
            }

            interval[0] = Double.parseDouble(interval_low);
            interval[1] = -2.0;
        }

        else {

            int dash_position = interval_string.indexOf('-');

            for (int i = 0; i < dash_position; ++i) {
                interval_low = interval_low + interval_string.charAt(i);
            }
            //TODO handle interval low = "" - use a try catch block? return null interval?
            interval[0] = Double.parseDouble(interval_low);

            String interval_high = "";
            for (int j = dash_position + 1; j < interval_string.length(); ++j) {
                interval_high = interval_high + interval_string.charAt(j);
            }
            interval[1] = Double.parseDouble(interval_high);
        }

        return interval;
    }

    public void Plot(View view) {
        if (plot_field.getDate_high() != null && plot_field.getDate_low() != null && plot_field.getTest() != null) {
            line_chart.removeAllSeries();
            List<DatabaseEntry> entries;
            entries = myDb.queryRangeData(plot_field.getTest(), plot_field.getDate_low(), plot_field.getDate_high());
            entries = OrderEntries(entries);

            Date x_min = new Date();
            Date x_max;

            double y_min;
            double y_max;

            if(entries.size() == 0) {
                //ShowMessage("Data To Plot", "No Data Found");
            }
            else {
                String[] dates = new String[entries.size()];
                DataPoint[] points = new DataPoint[entries.size()];

                StringBuffer buffer = new StringBuffer();

                Date date;

                y_max = MaxResult(entries);
                y_min = MinResult(entries);

                for (int i = 0; i < entries.size(); ++i) {
                    SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(format.parse(entries.get(i).getDate()));
                        date = calendar.getTime();

                        if (i == 0)
                            x_min = date;

                        points[i] = new DataPoint(date, Double.parseDouble(String.valueOf(entries.get(i).getResult())));

                        dates[i] = entries.get(i).getDate();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    buffer.append("ID :" + entries.get(i).getId() + "\n");
                    buffer.append("DATE :" + entries.get(i).getDate() + "\n");
                    buffer.append("TESTS :" + entries.get(i).getTests() + "\n");
                    buffer.append("RESULT :" + entries.get(i).getResult() + "\n");
                    buffer.append("UNITS :" + entries.get(i).getUnits() + "\n");
                    buffer.append("REFERENCE INTERVAL :" + entries.get(i).getReference_interval() + "\n\n");
                }

                line_chart.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getContext()));
                line_chart.getGridLabelRenderer().setNumHorizontalLabels(4);
                line_chart.getGridLabelRenderer().setNumVerticalLabels(6);
                line_chart.getGridLabelRenderer().setTextSize(35f);
                line_chart.getGridLabelRenderer().reloadStyles();
                line_chart.getGridLabelRenderer().setHumanRounding(false);
                line_chart.getGridLabelRenderer().setVerticalAxisTitle(entries.get(0).getUnits());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(x_min);
                calendar.add(Calendar.MONTH, 3);
                x_max = calendar.getTime();

                PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(points);
                series.setShape(PointsGraphSeries.Shape.RECTANGLE);
                series.setTitle(entries.get(0).getTests());

                series.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        double date_double = Double.parseDouble(String.valueOf(dataPoint.getX()));

                        String date_string = new SimpleDateFormat("M/dd/yyyy", Locale.US).format(date_double);

                        Toast.makeText(getContext(), "("+ date_string + ", " + dataPoint.getY() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
                line_chart.addSeries(series);

                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(points);
                series2.setTitle(entries.get(0).getTests());
                line_chart.addSeries(series2);

                double[] reference_interval = ReferenceInterval(entries.get(0));

                Log.d("tag", String.valueOf(reference_interval[1]));

                if (reference_interval[1] > -.99 || reference_interval[1] > -.99) {
                    DataPoint[] end_points_low = new DataPoint[2];
                    end_points_low[0] = new DataPoint(x_min, reference_interval[0]);
                    end_points_low[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[0]);

                    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(end_points_low);
                    series3.setTitle("Min Level");
                    series3.setColor(Color.RED);
                    line_chart.addSeries(series3);

                    DataPoint[] end_points_high = new DataPoint[2];
                    end_points_high[0] = new DataPoint(x_min, reference_interval[1]);
                    end_points_high[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[1]);

                    LineGraphSeries<DataPoint> series4 = new LineGraphSeries<>(end_points_high);
                    series4.setTitle("Max Level");
                    series4.setColor(Color.RED);
                    line_chart.addSeries(series4);
                }

                else if (reference_interval[1] == -1.0){
                    DataPoint[] end_points_low = new DataPoint[2];
                    end_points_low[0] = new DataPoint(x_min, reference_interval[0]);
                    end_points_low[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[0]);

                    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(end_points_low);
                    series3.setTitle("Min Level");
                    series3.setColor(Color.RED);
                    line_chart.addSeries(series3);
                }

                else if (reference_interval[1] == -2.0){
                    DataPoint[] end_points_low = new DataPoint[2];
                    end_points_low[0] = new DataPoint(x_min, reference_interval[0]);
                    end_points_low[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[0]);

                    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(end_points_low);
                    series3.setTitle("Max Level");
                    series3.setColor(Color.RED);
                    line_chart.addSeries(series3);
                }

                line_chart.getViewport().setXAxisBoundsManual(true);
                line_chart.getViewport().setMinX(x_min.getTime());
                line_chart.getViewport().setMaxX(x_max.getTime() + x_max.getTime() * 0.001d);
                line_chart.getViewport().setYAxisBoundsManual(true);
                if (y_min < reference_interval[0])
                    line_chart.getViewport().setMinY(y_min - y_min * 0.1d);
                else
                    line_chart.getViewport().setMinY(reference_interval[0] - reference_interval[0] * 0.1d);

                if (reference_interval[1] != -1.0 || reference_interval[1] != -2.0) {

                    if (y_max > reference_interval[1])
                        line_chart.getViewport().setMaxY(y_max + y_max * 0.1d);
                    else
                        line_chart.getViewport().setMaxY(reference_interval[1] + reference_interval[1] * 0.1d);
                }
                else {
                    line_chart.getViewport().setMaxY(y_max + y_max * 0.1d);
                }

                Log.d("max", String.valueOf(y_max));

                line_chart.getViewport().setScalable(true);
                line_chart.getViewport().setScrollable(true);
                line_chart.getLegendRenderer().setVisible(true);
                line_chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);


                //ShowMessage("Data To Plot", buffer.toString());
            }

        }

        else
            ShowMessage("Data To Plot", "No Data Found");
    }

    public void LowDatePicker(View view){
        Bundle bundle = new Bundle();
        bundle.putInt("id", date_low_id);
        PlotFragment.DatePickerFragment fragment = new PlotFragment.DatePickerFragment();
        FragmentManager manager = getFragmentManager();
        fragment.setArguments(bundle);
        fragment.show(manager, "DatePicker");
    }

    public void HighDatePicker(View view){

        Bundle bundle = new Bundle();
        bundle.putInt("id", date_high_id);
        PlotFragment.DatePickerFragment fragment = new PlotFragment.DatePickerFragment();
        FragmentManager manager = getFragmentManager();
        fragment.setArguments(bundle);
        fragment.show(manager, "DatePicker");

    }

    public static class DatePickerFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            super.onCreateDialog(savedInstanceState);

            switch(getArguments().getInt("id")) {
                case date_low_id:
                    return new DatePickerDialog(getActivity(), date_low_listener, year, month, day);
                case date_high_id:
                    return new DatePickerDialog(getActivity(), date_high_listener, year, month, day);
            }

            return null;
        }

    }

    public String SpinnerTests() {
        if (myDb==null)
        Log.d(TAG, "FUCK DB");
        Set<String> items = myDb.getAllField("tests");

        if (items.size() != 0) {
            String[] items_strings = new String[items.size()];

            Iterator it = items.iterator();
            int i = 0;
            while (it.hasNext()) {
                items_strings[i] = it.next().toString();
                ++i;
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, items_strings);


            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner_tests.setAdapter(adapter);
            spinner_tests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    plot_field.setTest(parent.getItemAtPosition(position).toString());
                    Plot(view);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub
                }
            });

            return items_strings[0];

        }

        else return "First Test";
    }

    public void SpinnerDates() {
        String[] items_strings = new String[5];
        items_strings[0] = "past 6 months";
        items_strings[1] = "past 1 year";
        items_strings[2] = "past 3 years";
        items_strings[3] = "past 5 years";
        items_strings[4] = "past 10 years";


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, items_strings);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_dates.setAdapter(adapter);
        spinner_dates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                Date low_date = new Date();
                Date high_date = new Date();

                SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.US);
                Calendar calendar = Calendar.getInstance();
                high_date = calendar.getTime();

                switch(position) {
                    case 0:
                        calendar.add(Calendar.MONTH, -6);
                        low_date = calendar.getTime();
                        break;
                    case 1:
                        calendar.add(Calendar.YEAR, -1);
                        low_date = calendar.getTime();
                        break;
                    case 2:
                        calendar.add(Calendar.YEAR, -3);
                        low_date = calendar.getTime();
                        break;
                    case 3:
                        calendar.add(Calendar.YEAR, -5);
                        low_date = calendar.getTime();
                        break;
                    case 4:
                        calendar.add(Calendar.YEAR, -10);
                        low_date = calendar.getTime();
                        break;
                    default:
                        break;
                }

                lowDateBtn.setText(format.format(low_date));
                plot_field.setDate_low(format.format(low_date));
                highDateBtn.setText(format.format(high_date));
                plot_field.setDate_high(format.format(high_date));

                Plot(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void DateHigh() {
        date_high_listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar cal = new GregorianCalendar(year, month, day);
                highDateBtn.setText(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                plot_field.setDate_high(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                Plot(view);
            }
        };
    }

    public void DateLow() {
        date_low_listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar cal = new GregorianCalendar(year, month, day);
                lowDateBtn.setText(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                plot_field.setDate_low(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                Plot(view);
            }
        };
    }

    public void ShowMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }




    public void setListeners(View view) {
        lowDateBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LowDatePicker(v);

                    }
                }
        );

        highDateBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HighDatePicker(v);

                    }
                }
        );

    }


}
