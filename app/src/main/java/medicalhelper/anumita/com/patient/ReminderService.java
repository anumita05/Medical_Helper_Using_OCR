package medicalhelper.anumita.com.patient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class ReminderService extends Service {
    private String content, title, notification = "", pid = "", did = "";
    private Context notificationContext;

    private int notifyCount = 0;

    SharedPreferences sharedPreferences;

    Timer timer;
    TimerTask timerTask;
    Handler hand = new Handler();

    boolean checkFlag = true;
    //tid,uid,name,type,color,size,detail,pic,rid,qty,time,details
    private ArrayList<String> time, name, pic;
    private DateFormat mFormatDate = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private Calendar mExpiryCalender;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ReminderService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        pid = sharedPreferences.getString(UtilConstants.UID, "");

        notificationContext = ReminderService.this;

//        Toast.makeText(notificationContext, "ReminderService", Toast.LENGTH_SHORT).show();

        timer = new Timer();
        checkNotifcation();
        timer.schedule(timerTask, 0, 5000);

        return START_STICKY;
    }

    private void checkNotifcation() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                hand.post(new Runnable() {
                    @Override
                    public void run() {
                        if (checkFlag) {
                            checkFlag = false;
                            mExpiryCalender = Calendar.getInstance();
                            new GetReminder().execute(pid);
                        }
                    }
                });
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GetReminder extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.getReminders(strings[0]);
                JSONParse jp = new JSONParse();
                answer = jp.Parse(jsonObject);

            } catch (Exception e) {
//                e.printStackTrace();
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.contains("Unable to resolve host")) {
                checkFlag = true;
                Log.d("ReminderLog", "No Internet");
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("ok") == 0) {

                        time = new ArrayList<String>();
                        name = new ArrayList<String>();
                        pic = new ArrayList<String>();
                        JSONArray jarray = json.getJSONArray("Data");

                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            //tid,uid,name,type,color,size,detail,pic,rid,qty,time,details
//                            Log.d("ReminderLog", jdata.getString("data2")+"\n"+ jdata.getString("data10"));
                            name.add(jdata.getString("data2"));
                            pic.add(jdata.getString("data7"));
                            time.add(jdata.getString("data12"));
                        }

                        if (time.size() > 0) {
                            notifyuser();
                        }

                    } else if (ans.compareTo("no") == 0) {

                        new GetExpiryDate().execute(pid, mFormatDate.format(mExpiryCalender.getTime()));

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Log.d("ReminderLog", error);

                        new GetExpiryDate().execute(pid, mFormatDate.format(mExpiryCalender.getTime()));

                    } else {

                        new GetExpiryDate().execute(pid, mFormatDate.format(mExpiryCalender.getTime()));

                    }
                } catch (Exception e) {

                    e.printStackTrace();
                    Log.d("ReminderLog", e.getMessage());
                }
            }
        }
    }

    private void notifyuser() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        for (int i = 0; i < time.size(); i++) {
            Log.d("ReminderLog", timeFormat.format(new Date().getTime()) + "\n" + time.get(i));

            if (timeFormat.format(new Date().getTime()).compareTo(time.get(i)) == 0) {
                Log.d("ReminderLog", "True");

                Intent homeIntent = new Intent(ReminderService.this, MainActivityPatient.class);
                showNotification("Reminder For Medicine", new Pair<String, String>(name.get(i), " Time : "+time.get(i)), i, homeIntent);
            }
        }

        new GetExpiryDate().execute(pid, mFormatDate.format(mExpiryCalender.getTime()));

    }

    private class GetExpiryDate extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            try {
                RestAPI resstApi = new RestAPI();
                response = new JSONParse().Parse(resstApi.getExpiryMedicines(strings[0], strings[1]));
            } catch (Exception e) {
                response = e.getMessage();
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("GetExpiry", s);
            if (s.contains("Unable to resolve host")) {
                checkFlag = true;
                Log.d("GetExpiry", "No Internet");
            } else {
                JSONObject json = null;
                try {
                    json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("ok") == 0) {
                        JSONArray jarray = json.getJSONArray("Data");
                        JSONObject jdata = jarray.getJSONObject(0);
                        String name = jdata.getString("data0");

                        if (name.length() > 0) {
                            checkFlag = true;
                            showNotification("Reminder For Expiry", new Pair<String, String>("", name+" Medicine is going to Expire Today")
                                    , -1, new Intent(ReminderService.this, MainActivityPatient.class));

                            resetTimer();
                        }
                    } else if (ans.compareTo("no") == 0) {
                        checkFlag = true;
                        resetTimer();
                    } else if (ans.compareTo("error") == 0) {
                        checkFlag = true;
                        String error = json.getString("Data");
                        resetTimer();
                        Log.d("GetExpiry", error);
//                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    checkFlag = true;
                    resetTimer();
                    e.printStackTrace();
                    Log.d("GetExpiry", e.getMessage());
                }
            }

        }
    }

    private void resetTimer() {
        if(timer != null && timerTask!=null){
            timer.cancel();
            timerTask.cancel();

            checkFlag = true;
            timer = new Timer();
            checkNotifcation();
            timer.schedule(timerTask, 60000, 5000);
        }
    }

    private void showNotification(@NonNull String title, @NonNull Pair<String, String> contents, int i, Intent homeIntent) {
        try {
            int randomValue = new Random().nextInt(10000) + 1;

            content = contents.first + contents.second;
            homeIntent.putExtra(UtilConstants.Chat_NID, randomValue);
            if(i != -1)
                homeIntent.putExtra(UtilConstants.REMINDER_BOOLEAN, true);
            else
                homeIntent.putExtra(UtilConstants.REMINDER_BOOLEAN, false);

            PendingIntent pendingFinishIntent = PendingIntent.getActivity(ReminderService.this, randomValue, homeIntent, 0);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            UtilConstants.createNotificationChannel(ReminderService.this, UtilConstants.CHANNEL_ID_REM);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ReminderService.this, UtilConstants.CHANNEL_ID_REM);

            builder.setContentIntent(pendingFinishIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(content);
            if (pic != null && i != -1 && pic.size() > 0) {
                builder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(UtilConstants.getBitmap(pic.get(i))));
            }

            Notification n = builder.build();
            nm.notify("Medical_OCR_REM", randomValue, n);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
