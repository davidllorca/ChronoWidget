package com.davidllorca.chronowidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

/**
 * Implementation of App Widget functionality.
 */
public class ChronoWidget extends AppWidgetProvider {

    private static int UPDATE_RATE = 1000; // One second
    public static final String UPDATE = "update";

    /**
     * Exectute when widget is created and periodic calls to refresh.
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds,    ids of widgets active.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        /*// There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }*/
        for (int appWidgetId : appWidgetIds) {
            setAlarm(context, appWidgetId, UPDATE_RATE);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chrono_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When widget is deleted
        // Stop service when widget is disabled
        for (int appWidgetId : appWidgetIds) {
            setAlarm(context, appWidgetId, -1);
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

    }

    /**
     * Call service method.
     *
     * @param context
     * @param appWidgetId
     * @param updateRate
     */
    public static void setAlarm(Context context, int appWidgetId, int updateRate) {
        PendingIntent newPending = makeControlPendingIntent(context, ChronoWidget.UPDATE, appWidgetId);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (updateRate >= 0) {
            alarms.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), updateRate, newPending);
        } else {
            // If there are a updateRate negative cancel refresh
            alarms.cancel(newPending);
        }
    }

    public static PendingIntent makeControlPendingIntent(Context context, String command, int appWidgetId) {
        /*
            PendingIntent is Intent type. This remains on system although his parent disappear.
         */
        Intent activeIntent = new Intent(context, ChronoService.class);
        activeIntent.setAction(command);
        activeIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Uri is to do PendingIntent unique, if there are many instances these don't overwrite.
        Uri data = Uri.withAppendedPath(Uri.parse("cronowidget://widget/id/#+command+appWidgetId"), String.valueOf(appWidgetId));
        activeIntent.setData(data);
        return (PendingIntent.getService(context, 0, activeIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    /**
     * Service
     */
    public static class ChronoService extends Service {

        private static final String CHRONO_PREFS = "chrono";

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            String command = intent.getAction();
            // Obtain widget's id
            int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
            RemoteViews remoteView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.chrono_widget);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(CHRONO_PREFS, 0);
            // Limit date
            long goal = prefs.getLong("date" + appWidgetId, -1);
            if (goal != -1) {
                // Calculate time to rest or exceeded
                long past = System.currentTimeMillis() - goal;
                // Days
                int days = (int) Math.floor(past / (long) (60 * 60 * 24 * 1000));
                past = past - days * (long) (60 * 60 * 24 * 1000);
                // Hours
                int hours = (int) Math.floor(past / (long) (60 * 60 * 1000));
                past = past - hours * (long) (60 * 60 * 1000);
                // Minutes
                int mins = (int) Math.round(past / (long) (60 * 1000));
                past = past - mins * (long) (60 * 1000);
                // Seconds
                int secs = (int) Math.floor(past / (long) (1000));
                past = past - secs * (long) (1000);

                if (past > 0 && prefs.getBoolean("show_msg" + appWidgetId, true)) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                    String message = prefs.getString("description" + appWidgetId, formatter.format(goal));
                    ChronoMessageNotification.notify(getApplicationContext(), message, 1);
                    // Check like showed
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("show_msg" + appWidgetId, false);
                    editor.commit();
                }
                // Update view
                remoteView.setTextViewText(R.id.appwidget_text, days + ":" + hours + ":" + mins + ":" + secs);
                // Apply changes
                appWidgetManager.updateAppWidget(appWidgetId, remoteView);
                // If service is destroy, it will be relaunch by system
            }
                return START_STICKY;
            }
    }
}

