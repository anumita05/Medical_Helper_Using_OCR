package medicalhelper.anumita.com.doctor;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class AddReminderActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String UID ="", PID="";

    //Views
    private Spinner spinner_Medicine;

    private TextView med_Name, med_Type, med_Color, addReminder;
    private ImageView med_Image;
    private LinearLayout details_view;
    private EditText med_Quantiy, med_Time, med_Details;
    private Dialog mDialog;

    //Timer Picker Dialog
    private Calendar currentCal;
    private TimePickerDialog timerPickerDialog;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

    private ArrayList<String> tid,tuid,name,type,color,size,detail,pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        getSupportActionBar().setTitle("Add Reminder");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDialog = new Dialog(AddReminderActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        currentCal = Calendar.getInstance();

        initView();

        Intent intent = getIntent();
        PID = intent.getStringExtra(UtilConstants.Pat_ID);

        new GetMedicine().execute(PID);

    }

    private void initView() {
        spinner_Medicine = findViewById(R.id.medicine_spinner);

        details_view = findViewById(R.id.medicine_details);

        med_Image  = findViewById(R.id.tablet_item_image);
        med_Name = findViewById(R.id.tablet_item_name);
        med_Type = findViewById(R.id.tablet_item_type);
        med_Color = findViewById(R.id.tablet_item_Color);
        med_Color.setVisibility(View.VISIBLE);

        med_Quantiy = findViewById(R.id.rem_add_quantity);
        med_Time = findViewById(R.id.rem_add_time);
        med_Details = findViewById(R.id.rem_add_details);

        addReminder = findViewById(R.id.reminder_add);

        med_Time.setFocusable(false);
        med_Time.setFocusableInTouchMode(false);
        med_Time.setClickable(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        spinner_Medicine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    if(details_view.getVisibility() == View.VISIBLE)
                        details_view.setVisibility(View.INVISIBLE);
                }else {
                    updateView(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        med_Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerPickerDialog = new TimePickerDialog(AddReminderActivity.this, timeSetListner, currentCal.get(Calendar.HOUR_OF_DAY),
                        currentCal.get(Calendar.MINUTE), true);
                if (!timerPickerDialog.isShowing())
                    timerPickerDialog.show();
            }
        });

        addReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spinner_Medicine.getSelectedItemPosition() == 0){
                    Snackbar.make(v, "Please, Select a Medicine", Snackbar.LENGTH_SHORT).show();
                    med_Details.requestFocus();
                }else if(med_Quantiy.getText().length() == 0){
                    Snackbar.make(v, "Please, Enter quantity of Medicine", Snackbar.LENGTH_SHORT).show();
                    med_Quantiy.requestFocus();
                }else if(med_Time.getText().length() == 0){
                    Snackbar.make(v, "Please, Choose Time For Taking this Medicine", Snackbar.LENGTH_SHORT).show();
                    med_Time.requestFocus();
                }else if(med_Details.getText().length() == 0){
                    Snackbar.make(v, "Please, Enter Some Instruction or Precautions", Snackbar.LENGTH_SHORT).show();
                    med_Details.requestFocus();
                }else {
                    new AddReminder().execute(PID, tid.get(spinner_Medicine.getSelectedItemPosition()), med_Quantiy.getText().toString(),
                                                med_Time.getText().toString(), med_Details.getText().toString());
                }
            }
        });

    }

    TimePickerDialog.OnTimeSetListener timeSetListner = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            currentCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            currentCal.set(Calendar.MINUTE, minute);

            med_Time.setText(timeFormat.format(currentCal.getTime()));
        }
    };

    private class GetMedicine extends AsyncTask<String, JSONObject, String> {

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
                JSONObject jsonObject = restAPI.getMedicines(strings[0]);
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
            Log.d("GetMedicine", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder( AddReminderActivity.this);
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

                        tid.add("NA");
                        tuid.add("NA");
                        name.add("Choose Medicine");
                        type.add("NA");
                        color.add("NA");
                        size.add("NA");
                        detail.add("NA");
                        pic.add("NA");

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
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddReminderActivity.this, R.layout.spinner_item_med, name);
                        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        spinner_Medicine.setAdapter(adapter);

                    } else if (StatusValue.compareTo("no") == 0) {
                        spinner_Medicine.setAdapter(null);
                        Toast.makeText( AddReminderActivity.this, "No Medicines found", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("GetMedicine", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("GetMedicine", e.getMessage());
                }
            }
        }
    }

    private class AddReminder extends AsyncTask<String, JSONObject, String> {

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
                JSONObject jsonObject = restAPI.AddReminder(strings[0], strings[1], strings[2], strings[3], strings[4]);
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
            Log.d("AddReminder", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder( AddReminderActivity.this);
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
                    if (StatusValue.compareTo("true") == 0) {

                        Toast.makeText(AddReminderActivity.this, "Reminder Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();


                    } else if (StatusValue.compareTo("already") == 0) {

                        AlertDialog.Builder ad = new AlertDialog.Builder(AddReminderActivity.this);
                        ad.setTitle("Reminder already Exist");
                        ad.setMessage("Could not add reminder, a reminder for this Medicine already exists, Try again with different Medicine");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();

                    } else {
                        String error = json.getString("Data");
                        Log.d("AddReminder", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AddReminder", e.getMessage());
                }
            }
        }
    }

    private void updateView(int position) {
        if(details_view.getVisibility() == View.INVISIBLE)
            details_view.setVisibility(View.VISIBLE);

        med_Image.setImageBitmap(UtilConstants.getBitmap(pic.get(position)));
        med_Name.setText(name.get(position));
        med_Type.setText("Type : "+type.get(position));
        med_Color.setText("Color : "+color.get(position));
    }

}
