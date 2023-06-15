package medicalhelper.anumita.com.patient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import medicalhelper.anumita.com.chat.ChatActivity;
import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class NotificationService extends Service {
    private String content, title, notification = "", pid = "", did = "";
    private Context notificationContext;

    private int notifyCount = 0;

    SharedPreferences sharedPreferences;

    Timer timer;
    TimerTask timerTask;
    Handler hand = new Handler();

    boolean checkFlag = true;
    //nid,uid,sid,title,message
    private ArrayList<String> nid, uid, title1, message;

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(NotificationService.this).registerReceiver(listener, new IntentFilter(UtilConstants.BROADCAST_CHAT));
    }

    public NotificationService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        pid = sharedPreferences.getString(UtilConstants.UID, "");

        notificationContext = NotificationService.this;
//        Toast.makeText(NotificationService.this, "Notification Service", Toast.LENGTH_SHORT).show();
//        Toast.makeText(notificationContext, "Notification Service", Toast.LENGTH_SHORT).show();

        timer = new Timer();
        checkNotifcation();
        timer.schedule(timerTask, 0,5000);

        return START_STICKY;
    }

    private void checkNotifcation() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                hand.post(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(notificationContext, checkFlag+" - "+pid, Toast.LENGTH_SHORT).show();
                        if (checkFlag) {
                            new GetNotification().execute(pid);
                            checkFlag = true;
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

    private class GetNotification extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            checkFlag = false;
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.getNotification(strings[0]);
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
            checkFlag = true;

            if (s.contains("Unable to resolve host")) {

            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    Log.d("NotificationLog", s);
                    if (ans.compareTo("ok") == 0) {
                        nid = new ArrayList<String>();
                        uid = new ArrayList<String>();
                        title1 = new ArrayList<String>();
                        message = new ArrayList<String>();
                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            //nid,uid,sid,title,message
                            nid.add(jdata.getString("data1"));
                            uid.add(jdata.getString("data2"));
                            title1.add(jdata.getString("data3"));
                            message.add(jdata.getString("data4"));
                        }

                        if (title1.size() > 0) {
                            notifyuser();
                        }

                    } else if (ans.compareTo("no") == 0) {

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Log.d("NotificationLog", error);

//                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    checkFlag = true;
                    e.printStackTrace();
                    Log.d("NotificationLog - Error", e.getMessage());
                }
            }
        }
    }

    private void notifyuser() {

//        Toast.makeText(notificationContext, title1.get(0), Toast.LENGTH_SHORT).show();

        if(title1.get(0).contains("Message from")){
            if(did.compareTo(uid.get(0)) != 0){
                title = title1.get(0);
                title = title.substring(13, title.length());
                Intent homeIntent = new Intent(NotificationService.this, ChatActivity.class);



                homeIntent.putExtra(UtilConstants.Chat_ID, uid.get(0));
                homeIntent.putExtra(UtilConstants.Chat_Type, sharedPreferences.getString(UtilConstants.UserType, ""));
                homeIntent.putExtra(UtilConstants.Chat_Name, sharedPreferences.getString(UtilConstants.UserName, ""));

                showNotification(0, homeIntent);
            }
        }
        notification = nid.get(0);
    }

    private void showNotification(int i, Intent homeIntent) {
        int randomValue = new Random().nextInt(10000) + 1;

        content = message.get(i);
        homeIntent.putExtra(UtilConstants.Chat_NID, randomValue);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingFinishIntent = PendingIntent.getActivity(NotificationService.this, randomValue, homeIntent, 0);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        UtilConstants.createNotificationChannel(NotificationService.this, UtilConstants.CHANNEL_ID);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, UtilConstants.CHANNEL_ID);

        builder.setContentIntent(pendingFinishIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title);
        builder.setContentText(content);

        Notification n = builder.build();
        nm.notify("Medical_OCR", randomValue, n);

        checkFlag = true;

    }

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            did = intent.getStringExtra(UtilConstants.ChatBroadcastID);
//            Toast.makeText(context, did+" - Broadcast", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(NotificationService.this).unregisterReceiver(listener);
    }
}
