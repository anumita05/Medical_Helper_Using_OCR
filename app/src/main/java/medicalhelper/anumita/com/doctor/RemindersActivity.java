package medicalhelper.anumita.com.doctor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class RemindersActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String UID = "", pid = "";

    private FloatingActionButton add_reminders;
    private ListView reminders_list;
    private Dialog mDialog;
    private ArrayList<String> tid,tuid,name,type,color,size,detail,pic,rid,qty,time,details;

    public RemindersActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = RemindersActivity.this.getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        setContentView(R.layout.fragment_tablet);

        getSupportActionBar().setTitle("Reminder's");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDialog = new Dialog(RemindersActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        reminders_list = findViewById(R.id.tablets_list);
        add_reminders = findViewById(R.id.addMedicines);

        reminders_list.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));

        Intent intent = getIntent();
        pid = intent.getStringExtra(UtilConstants.Pat_ID);
    }

    @Override
    public void onStart() {
        super.onStart();

        reminders_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString(UtilConstants.Med_Tid, tid.get(position));
                bundle.putString(UtilConstants.Med_Name, name.get(position));
                bundle.putString(UtilConstants.Med_Type, type.get(position));
                bundle.putString(UtilConstants.Med_Color, color.get(position));
                bundle.putString(UtilConstants.Med_Size, size.get(position));
                bundle.putString(UtilConstants.Med_Details, details.get(position));
                bundle.putString(UtilConstants.Med_Quantity, qty.get(position));
                bundle.putString(UtilConstants.Med_Time, time.get(position));
                bundle.putString(UtilConstants.Med_Rid, rid.get(position));
                UtilConstants.setMedPicImage(pic.get(position));

                Intent intent = new Intent(RemindersActivity.this, ReminderDetails.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        add_reminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RemindersActivity.this, AddReminderActivity.class);
                intent.putExtra(UtilConstants.Pat_ID, pid);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetReminders().execute(pid);
    }

    private class GetReminders extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            try {
                JSONObject jsonObject = restAPI.getReminders(strings[0]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception e) {
                response = e.getMessage();
                e.printStackTrace();

            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("GetReminders", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(RemindersActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();

            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("ok") == 0) {

                        tid=new ArrayList<String>();
                        tuid=new ArrayList<String>();
                        name=new ArrayList<String>();
                        type=new ArrayList<String>();
                        color=new ArrayList<String>();
                        size=new ArrayList<String>();
                        detail=new ArrayList<String>();
                        pic=new ArrayList<String>();
                        rid=new ArrayList<String>();
                        qty=new ArrayList<String>();
                        time=new ArrayList<String>();
                        details=new ArrayList<String>();

                        JSONArray jsonArray = json.getJSONArray("Data");

                        for(int i = 0;i<jsonArray.length();i++){

                            json = jsonArray.getJSONObject(i);
                            tid.add(json.getString("data0"));
                            tuid.add(json.getString("data1"));
                            name.add(json.getString("data2"));
                            type.add(json.getString("data3"));
                            color.add(json.getString("data4"));
                            size.add(json.getString("data5"));
                            detail.add(json.getString("data6"));
                            pic.add(json.getString("data7"));

                            rid.add(json.getString("data8"));
                            qty.add(json.getString("data9"));
                            time.add(json.getString("data10"));
                            details.add(json.getString("data11"));
                        }

                        ReminderAdapter reminderAdapter = new ReminderAdapter(RemindersActivity.this.getApplicationContext(), R.layout.reminder_item, name);
                        reminders_list.setAdapter(reminderAdapter);

                    } else if (StatusValue.compareTo("no") == 0) {
                        reminders_list.setAdapter(null);
                        Toast.makeText(RemindersActivity.this, "No Reminders found, Please Click Add button to Add one", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("GetReminders", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("GetReminders", e.getMessage());
                }
            }
        }
    }

    public class ReminderAdapter extends ArrayAdapter<String> {

        private Context appContext;

        public ReminderAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> name) {
            super(context, resource, name);
            appContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(appContext).inflate(R.layout.reminder_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tab_image = convertView.findViewById(R.id.rem_item_image);
                viewHolder.tab_name = convertView.findViewById(R.id.rem_item_name);
                viewHolder.tab_type = convertView.findViewById(R.id.rem_item_type);
                viewHolder.tab_qty = convertView.findViewById(R.id.rem_item_qty);
                viewHolder.tab_time = convertView.findViewById(R.id.rem_item_time);
                convertView.setTag(viewHolder);
            }else {
                viewHolder =(ViewHolder) convertView.getTag();
            }

            viewHolder.tab_image.setImageBitmap(UtilConstants.getBitmap(pic.get(position)));
            viewHolder.tab_name.setText(name.get(position));
            viewHolder.tab_type.setText("Type : "+type.get(position));
            viewHolder.tab_qty.setText("Qty : "+qty.get(position));
            viewHolder.tab_time.setText("Time : "+time.get(position));

            return convertView;
        }

        private class ViewHolder{
            ImageView tab_image;
            TextView tab_name, tab_type, tab_qty, tab_time;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


}
