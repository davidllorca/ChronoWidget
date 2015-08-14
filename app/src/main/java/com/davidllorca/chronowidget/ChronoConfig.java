package com.davidllorca.chronowidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.GregorianCalendar;

/**
 * Config alarm parameters
 *
 * Created by David Llorca <davidllorcabaron@gmail.com> on 8/14/15.
 */
public class ChronoConfig extends Activity {

    private static final String CHRONO_PREFS = "chrono";
    private Context selfContext = this;
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get extras
        Intent launchIntent = getIntent();
        Bundle extras = launchIntent.getExtras();
        // Obtain id from widget to config
        appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        // Data to return in cancel action
        Intent cancelReturnValue = new Intent();
        cancelReturnValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, cancelReturnValue);

        // Load layout
        setContentView(R.layout.chrono_config);
        // References components
        // OK action
        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get date
                DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
                // Get time
                TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
                // Get alarm's description
                EditText alarmText = (EditText) findViewById(R.id.textAlarm);
                // Instance of calendar
                GregorianCalendar date = new GregorianCalendar(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute());

                // Save data of each widget that we want config(date & time)
                SharedPreferences prefs = selfContext.getSharedPreferences(CHRONO_PREFS,0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("date" + appWidgetId, date.getTime().getTime());
                editor.putString("description" + appWidgetId, alarmText.getText().toString());
                editor.commit();

                // Data to return in ok action
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                // Finish activity
                finish();
            }
        });
        // Cancel action
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish activity
                finish();
            }
        });
    }
}
