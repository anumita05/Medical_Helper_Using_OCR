package medicalhelper.anumita.com.chat;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.adapter.ChatItem;
import medicalhelper.anumita.com.adapter.RecyclerChatAdapter;
import medicalhelper.anumita.com.doctor.MainActivity;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;
import medicalhelper.anumita.com.patient.MainActivityPatient;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerChat;
    private RecyclerView.LayoutManager recyclerLayout;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerChatAdapter recyclerChatAdapter;
    //SenderId,RecId,Message,cdate,ctime
    private ArrayList<ChatItem> ChatMessage;
    private SharedPreferences sharedPreferences;
    private String UID ="",UserType = "", destId = "", chatName ="", destType="";
    private EditText message;
    private ImageView sendText;

    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",Locale.US);
    private ChatItem addChatItem;
    private Dialog mDialog;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();
    private boolean async = false;
    private int chatCount = 0;
    private int nid=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_actvity);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");
        UserType = sharedPreferences.getString(UtilConstants.UserType, "");

        NotificationManager no = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        try{
            Intent intent = getIntent();

            destId = intent.getStringExtra(UtilConstants.Chat_ID);
            destType = intent.getStringExtra(UtilConstants.Chat_Type);
            chatName = intent.getStringExtra(UtilConstants.Chat_Name);
            nid = intent.getIntExtra(UtilConstants.Chat_NID, 0);
//            Toast.makeText(this, destId+"\n"+destType+"\n"+chatName+"\n"+nid, Toast.LENGTH_SHORT).show();
            if(nid != 0){
                no.cancel("Medical_OCR", nid);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if(UserType.compareTo("Doctor") == 0){
            getSupportActionBar().setTitle(chatName);
        }else {
            getSupportActionBar().setTitle(sharedPreferences.getString(UtilConstants.UserName, ""));
        }
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ChatMessage = new ArrayList<ChatItem>();

        mDialog = new Dialog(ChatActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);


        message = (EditText) findViewById(R.id.chatMessage);
        sendText = (ImageView) findViewById(R.id.sendMessage);

        recyclerChat = (RecyclerView) findViewById(R.id.chatRecycler);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerLayout = linearLayoutManager;
        recyclerChat.setLayoutManager(recyclerLayout);

        sendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
//                uid,did,message,date,time
                addChatItem = new ChatItem(UID, destId ,message.getText().toString(),
                        dateFormat.format(calendar.getTime()), timeFormat.format(calendar.getTime()), UID);

                ChatMessage.add(addChatItem);

                recyclerChatAdapter.notifyDataSetChanged();

                chatCount++;

                if(!isLastVisible()){
                    recyclerChat.scrollToPosition(recyclerChatAdapter.getItemCount() - 1);
                }

//                Toast.makeText(ChatActivity.this, destType+"\n"+UID+"\n"+destId+"\n"+message.getText().toString()+"\n"+
//                        dateFormat.format(calendar.getTime())+"\n"+timeFormat.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
//                string type,string myid,string destid,string mesg,string date,string time
                new AddChatMessage().execute(destType,UID, destId, message.getText().toString(),
                        dateFormat.format(calendar.getTime()), timeFormat.format(calendar.getTime()));
            }
        });

//        Toast.makeText(this, chatName+"", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(UtilConstants.BROADCAST_CHAT);
        intent.putExtra(UtilConstants.ChatBroadcastID, UID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        new GetChatMessage().execute(destType,UID, destId);
    }

    private class GetChatMessage extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            async = false;
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getChat(params[0], params[1], params[2]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            async = true;
            Log.d("GetChat", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");

                    if (ans.compareTo("ok") == 0) {
                        ChatMessage = new ArrayList<ChatItem>();

                        JSONArray jarray = json.getJSONArray("Data");
                        chatCount = jarray.length();

                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);

                            //cid,uid,did,message,date,time
                            ChatItem chatItem = new ChatItem(
                                    jdata.getString("data1"),
                                    jdata.getString("data2"),
                                    jdata.getString("data3"),
                                    jdata.getString("data4"),
                                    jdata.getString("data5"),
                                    jdata.getString("data6")
                            );

                            ChatMessage.add(chatItem);
                        }

                        recyclerChatAdapter = new RecyclerChatAdapter(ChatActivity.this, ChatMessage, UID);
                        recyclerChat.setAdapter(recyclerChatAdapter);
                        recyclerChat.scrollToPosition(recyclerChatAdapter.getItemCount() - 1);

                        timer = new Timer();
                        CheckChatHistory();
                        timer.schedule(timerTask, 0, 2000);

                    } else if (ans.compareTo("no") == 0) {

                        recyclerChatAdapter = new RecyclerChatAdapter(ChatActivity.this, ChatMessage, UID);
                        recyclerChat.setAdapter(recyclerChatAdapter);

                        timer = new Timer();
                        CheckChatHistory();
                        timer.schedule(timerTask, 0, 2000);

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                       Log.d("GetChat", error);
                    }
                } catch (Exception e) {
                    Log.d("GetChat", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void CheckChatHistory() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(async){
                            new GetChatMessage1().execute(destType,UID, destId);
                        }
                    }
                });
            }
        };
    }

    private class AddChatMessage extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            message.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                //String type,String myid,String destid,String mesg,String date,String time
                JSONObject json = api.addChat(params[0], params[1], params[2], params[3],params[4], params[5]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("AddChat", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
                    if (ans.compareTo("true") == 0) {

                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class GetChatMessage1 extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            async = false;
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getChat(params[0], params[1],params[2]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            async = true;
            Log.d("GetChat", s);
            if (s.contains("Unable to resolve host")) {
//                AlertDialog.Builder ad = new AlertDialog.Builder(ChatActivity.this);
//                ad.setTitle("Unable to Connect!");
//                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
//                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");

                    if (ans.compareTo("ok") == 0) {
                        JSONArray jarray = json.getJSONArray("Data");
//                        Toast.makeText(ChatActivity.this, chatCount+" - Jarray"+jarray.length(), Toast.LENGTH_SHORT).show();
                        if(chatCount != jarray.length()){
                            chatCount = jarray.length();
                            ChatMessage = new ArrayList<ChatItem>();
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject jdata = jarray.getJSONObject(i);
                                //SenderId,RecId,Message,cdate,ctime
                                ChatItem chatItem = new ChatItem(
                                        jdata.getString("data1"),
                                        jdata.getString("data2"),
                                        jdata.getString("data3"),
                                        jdata.getString("data4"),
                                        jdata.getString("data5"),
                                        jdata.getString("data6")
                                );

                                ChatMessage.add(chatItem);
                            }

                            recyclerChatAdapter = new RecyclerChatAdapter(ChatActivity.this, ChatMessage, UID);
                            recyclerChat.setAdapter(recyclerChatAdapter);
//                            recyclerChat.smoothScrollToPosition(recyclerChatAdapter.getItemCount() - 1);
                            recyclerChat.scrollToPosition(recyclerChatAdapter.getItemCount() - 1);
                        }

                    } else if (ans.compareTo("no") == 0) {

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
//                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
//                    Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerChat.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerChat.getAdapter().getItemCount();
        return (pos >= numItems);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(timer!= null){
            timer.cancel();
            timerTask.cancel();
            timer = null;
            timerTask = null;
        }

        Intent intent = new Intent(UtilConstants.BROADCAST_CHAT);
        intent.putExtra(UtilConstants.ChatBroadcastID, "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(nid!= 0) {
                if(UserType.compareTo("Doctor") == 0){
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(ChatActivity.this, MainActivityPatient.class);
                    startActivity(intent);
                    finish();
                }
            }else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(nid!= 0) {
            if(UserType.compareTo("Doctor") == 0){
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(ChatActivity.this, MainActivityPatient.class);
                startActivity(intent);
                finish();
            }
        }else {
            super.onBackPressed();
        }
    }

}
