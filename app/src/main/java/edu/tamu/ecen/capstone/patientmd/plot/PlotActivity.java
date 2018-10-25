/*
Probably move the majority of this into the plotting fragment
 */

package edu.tamu.ecen.capstone.patientmd.plot;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.tamu.ecen.capstone.patientmd.R;
import edu.tamu.ecen.capstone.patientmd.database.*;

public class PlotActivity extends AppCompatActivity {

    private DatabaseHelper myDb;
    private Spinner spinner_tests;
    private TextView textView_date_low;
    private TextView textView_date_high;
    private GraphView line_chart;
    private PlotField plot_field;

    static final int date_low_id = 0;
    static final int date_high_id = 1;

    static DatePickerDialog.OnDateSetListener date_high_listener, date_low_listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_plot);

        myDb = new DatabaseHelper(this);
        spinner_tests = findViewById(R.id.spinner_tests);
        line_chart = findViewById(R.id.line_chart);
        plot_field = new PlotField();

        Spinner();
        DateHigh();
        DateLow();
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

        int dash_position = interval_string.indexOf('-');

        String interval_low = "";
        for (int i = 0; i < dash_position; ++i) {
            interval_low = interval_low + interval_string.charAt(i);
        }
        interval[0] = Double.parseDouble(interval_low);

        String interval_high = "";
        for (int j = dash_position + 1; j < interval_string.length(); ++j) {
            interval_high = interval_high + interval_string.charAt(j);
        }
        interval[1] = Double.parseDouble(interval_high);

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
               ShowMessage("Data To Plot", "No Data Found");
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

               line_chart.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getBaseContext()));
               line_chart.getGridLabelRenderer().setNumHorizontalLabels(5);
               line_chart.getGridLabelRenderer().setNumVerticalLabels(5);
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

                       Toast.makeText(getBaseContext(), "("+ date_string + ", " + dataPoint.getY() + ")", Toast.LENGTH_SHORT).show();
                   }
               });
               line_chart.addSeries(series);

               LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(points);
               series2.setTitle(entries.get(0).getTests());
               line_chart.addSeries(series2);

               double[] reference_interval = ReferenceInterval(entries.get(0));

               DataPoint[] end_points_low = new DataPoint[2];
               end_points_low[0] = new DataPoint(x_min, reference_interval[0]);
               end_points_low[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[0]);

               LineGraphSeries<DataPoint> series3 = new LineGraphSeries<>(end_points_low);
               series3.setTitle("Ref Interval");
               series3.setColor(Color.RED);
               line_chart.addSeries(series3);

               DataPoint[] end_points_high = new DataPoint[2];
               end_points_high[0] = new DataPoint(x_min, reference_interval[1]);
               end_points_high[1] = new DataPoint(points[entries.size() - 1].getX(), reference_interval[1]);

               LineGraphSeries<DataPoint> series4 = new LineGraphSeries<>(end_points_high);
               series4.setTitle("Ref Interval");
               series4.setColor(Color.RED);
               line_chart.addSeries(series4);

               line_chart.getViewport().setXAxisBoundsManual(true);
               line_chart.getViewport().setMinX(x_min.getTime());
               line_chart.getViewport().setMaxX(x_max.getTime() + x_max.getTime() * 0.001d);
               line_chart.getViewport().setYAxisBoundsManual(true);
               if (y_min < reference_interval[0])
                   line_chart.getViewport().setMinY(y_min - y_min * 0.1d);
               else
                   line_chart.getViewport().setMinY(reference_interval[0] - reference_interval[0] * 0.1d);
               if (y_max > reference_interval[1])
                   line_chart.getViewport().setMaxY(y_max + y_max * 0.1d);
               else
                   line_chart.getViewport().setMaxY(reference_interval[1] + reference_interval[1] * 0.1d);

               Log.d("max", String.valueOf(y_max));

               line_chart.getViewport().setScalable(true);
               line_chart.getViewport().setScalableY(true);
               line_chart.getViewport().setScrollable(true);
               line_chart.getViewport().setScrollableY(true);
               line_chart.getViewport().setScalable(true);
               line_chart.getViewport().setScalableY(true);
               line_chart.getLegendRenderer().setVisible(true);

               line_chart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);


               ShowMessage("Data To Plot", buffer.toString());
           }

       }

       else
           ShowMessage("Data To Plot", "No Data Found");
    }

//    public void LowDatePicker(View view){
//        Bundle bundle = new Bundle();
//        bundle.putInt("id", date_low_id);
//        DatePickerFragment fragment = new DatePickerFragment();
//        FragmentManager manager = getFragmentManager();
//        fragment.setArguments(bundle);
//        fragment.show(manager, "DatePicker");
//    }

    public void HighDatePicker(View view){
        Bundle bundle = new Bundle();
        bundle.putInt("id", date_high_id);
        DatePickerFragment fragment = new DatePickerFragment();
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

    public void Spinner() {
        Set<String> items = myDb.getAllField("tests");
        String[] items_strings = new String[items.size()];

        Iterator it = items.iterator();
        int i = 0;
        while(it.hasNext()) {
            items_strings[i] = it.next().toString();
            ++i;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items_strings);


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_tests.setAdapter(adapter);
        spinner_tests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                plot_field.setTest(parent.getItemAtPosition(position).toString());
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
                textView_date_high.setText(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                plot_field.setDate_high(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
            }
        };
    }

    public void DateLow() {
        date_low_listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar cal = new GregorianCalendar(year, month, day);
                textView_date_low.setText(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
                plot_field.setDate_low(android.text.format.DateFormat.format("M/dd/yyyy", cal.getTime()).toString());
            }
        };
    }

    public void ShowMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }


}
