package medicalhelper.anumita.com.doctor;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class ReminderDetails extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private String UID = "";

    private ImageView Med_pic;
    private EditText Med_name, Med_color, Med_details, Med_quantity, Med_Time;
    private Spinner Med_type, Med_Size;
    private TableRow tblMedrow;
    private TextView btnSubmit, btnCancel, btnEdit, btnChaneImage;

    private String medId = "", medName = "", medType = "", medColor = "", medSize = "", medDetails = "", medPic = "", medQty = "", medTime = "", medRid = "";

    private EditText[] editTexts;
    private String[] message;
    private int gtposition = -1, gsubposition = -1;

    private String captureImage = "";
    private Dialog mDialog;

    //Timer Picker Dialog
    private Calendar currentCal;
    private TimePickerDialog timerPickerDialog;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_details);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        getSupportActionBar().setTitle("Reminder Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDialog = new Dialog(ReminderDetails.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        currentCal = Calendar.getInstance();

        initView();

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            medId = bundle.getString(UtilConstants.Med_Tid);
            medName = bundle.getString(UtilConstants.Med_Name);
            medType = bundle.getString(UtilConstants.Med_Type);
            medColor = bundle.getString(UtilConstants.Med_Color);
            medSize = bundle.getString(UtilConstants.Med_Size);
            medDetails = bundle.getString(UtilConstants.Med_Details);
            medQty = bundle.getString(UtilConstants.Med_Quantity);
            medTime = bundle.getString(UtilConstants.Med_Time);
            medRid = bundle.getString(UtilConstants.Med_Rid);
            medPic = UtilConstants.getMedPicImage();
        }

        setValues();

        hideUpdateAndDisable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateAndEnable();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMedicine();
            }
        });

        Med_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSubCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Med_Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerPickerDialog = new TimePickerDialog(ReminderDetails.this, timeSetListner, currentCal.get(Calendar.HOUR_OF_DAY),
                        currentCal.get(Calendar.MINUTE), true);
                if (!timerPickerDialog.isShowing())
                    timerPickerDialog.show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUpdateAndDisable(true);
            }
        });
    }

    private void updateMedicine() {
        if (validate()) {
//            string rid,string qty, string time, string details
            new UpdateReminder().execute(medRid, Med_quantity.getText().toString(), timeFormat.format(currentCal.getTime()), Med_details.getText().toString());

        }
    }

    private boolean validate() {
        boolean check = true;
        for (int i = 2; i < editTexts.length; i++) {
            if (editTexts[i].getText().length() == 0) {
                Snackbar.make(btnSubmit, message[i], Snackbar.LENGTH_SHORT).show();
                editTexts[i].requestFocus();
                check = false;
                break;
            }
        }
        return check;
    }

    TimePickerDialog.OnTimeSetListener timeSetListner = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            currentCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            currentCal.set(Calendar.MINUTE, minute);

            Med_Time.setText(timeFormat.format(currentCal.getTime()));
        }
    };

    private void initView() {
        Med_pic = findViewById(R.id.rem_medicine_image);
        Med_name = findViewById(R.id.rem_name);
        Med_color = findViewById(R.id.rem_color);
        Med_details = findViewById(R.id.rem_details);
        Med_quantity = findViewById(R.id.rem_quantity);
        Med_Time = findViewById(R.id.rem_time);

        Med_type = findViewById(R.id.rem_type);
        Med_Size = findViewById(R.id.rem_size);

        editTexts = new EditText[]{Med_name, Med_color, Med_quantity, Med_Time, Med_details};
        message = new String[]{"","","Please, Enter Medicine Quantity (To Be Taken)"
                , "Please, Choose Time For Taking Medicine", "Please, Enter Medicine Details(Instructions)"};
        tblMedrow = findViewById(R.id.update_row_med_rem);
        btnSubmit = findViewById(R.id.btnUpdateMed_rem);
        btnCancel = findViewById(R.id.btnCancelMed_rem);
        btnEdit = findViewById(R.id.btnEditMed_rem);

        btnChaneImage = findViewById(R.id.rem_changeImage);

    }

    private void setSubCategory(int pos) {
        int position = -1;
        String[] resources = new String[]{};
        switch (pos) {
            case 0:
                resources = getResources().getStringArray(R.array.type_tablet);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 1:
                resources = getResources().getStringArray(R.array.type_syrup);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 2:
                resources = getResources().getStringArray(R.array.type_capsule);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 3:
                resources = getResources().getStringArray(R.array.type_inhaler);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 4:
                resources = getResources().getStringArray(R.array.type_syringe);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 5:
                resources = getResources().getStringArray(R.array.type_ointment);
                for (int i = 0; i < resources.length; i++) {
                    if (resources[i].compareTo(medSize) == 0) {
                        position = i;
                        break;
                    }
                }
                break;
            case 6:
                break;

        }
        if (resources.length > 0) {
            gsubposition = position;
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(ReminderDetails.this, R.layout.spinner_item_med, resources);
            adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
            Med_Size.setAdapter(adapter);
            if (position > -1)
                Med_Size.setSelection(position);
        }
    }

    private void setValues() {

        Med_name.setText(medName);
        Med_color.setText(medColor);
        Med_quantity.setText(medQty);
        Med_Time.setText(medTime);

        String[] resources = getResources().getStringArray(R.array.medicine_type);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(ReminderDetails.this, R.layout.spinner_item_med, resources);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
        Med_type.setAdapter(adapter);

//        Toast.makeText(ReminderDetails.this, medType+" - "+gtposition, Toast.LENGTH_SHORT).show();

        for (int i = 0; i < resources.length; i++) {
            Log.d("MedType", resources[i]);
            if (resources[i].compareTo(medType) == 0) {
                gtposition = i;
                break;
            }
        }

        Med_type.setSelection(gtposition);

        Med_details.setText(medDetails);

        Med_pic.setImageBitmap(UtilConstants.getBitmap(medPic));

        Log.d("Reminder", medTime);
        String[] time = medTime.split(":");
        currentCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        currentCal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
    }

    private void showUpdateAndEnable() {

        for (int i = 0; i < editTexts.length; i++) {
            if(i <= 1){
                editTexts[i].setEnabled(false);
            }else {
                if (editTexts[i].getId() == R.id.rem_time) {
                    editTexts[i].setFocusable(false);
                    editTexts[i].setFocusableInTouchMode(false);
                    editTexts[i].setClickable(true);
                    editTexts[i].setEnabled(true);
                } else {
                    editTexts[i].setEnabled(true);
                }
            }
        }


        if (tblMedrow.getVisibility() == View.GONE)
            tblMedrow.setVisibility(View.VISIBLE);

        if (btnEdit.getVisibility() == View.VISIBLE)
            btnEdit.setVisibility(View.GONE);

    }

    private void hideUpdateAndDisable(boolean default_v) {

        for (int i = 0; i < editTexts.length; i++) {
            if(i <= 1){
                editTexts[i].setEnabled(false);
            }else {
                if (editTexts[i].getId() == R.id.rem_time) {
                    editTexts[i].setFocusable(false);
                    editTexts[i].setFocusableInTouchMode(false);
                    editTexts[i].setClickable(false);
                    editTexts[i].setEnabled(false);
                } else {
                    editTexts[i].setEnabled(false);
                }
            }
        }

        Med_type.setEnabled(false);
        Med_Size.setEnabled(false);


        if (tblMedrow.getVisibility() == View.VISIBLE)
            tblMedrow.setVisibility(View.GONE);

        if (btnEdit.getVisibility() == View.GONE)
            btnEdit.setVisibility(View.VISIBLE);

        if (default_v) {
            Med_name.setText(medName);
            Med_color.setText(medColor);
            Med_details.setText(medDetails);
            Med_type.setSelection(gtposition);
            Med_pic.setImageBitmap(UtilConstants.getBitmap(medPic));

            String[] time = medTime.split(":");
            currentCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
            currentCal.set(Calendar.MINUTE, Integer.parseInt(time[1]));

            Med_Time.setText(timeFormat.format(currentCal.getTime()));
        }

    }

    private class UpdateReminder extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.UpdateReminder(strings[0], strings[1], strings[2], strings[3]);
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
            Log.d("UpdateReminder", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ReminderDetails.this);
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

                        Toast.makeText(ReminderDetails.this, "Successfully Updated", Toast.LENGTH_SHORT).show();

                        hideUpdateAndDisable(false);

                    } else {
                        String error = json.getString("Data");
                        Log.d("UpdateReminder", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("UpdateReminder", e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_delete) {
            showDeleteAlert();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAlert() {
        AlertDialog.Builder ad = new AlertDialog.Builder(ReminderDetails.this);
        ad.setTitle("Are You Sure?");
        ad.setMessage("This medicine will be deleted.");
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                new ReminderDetails.DeleteMedicine().execute(medId);
            }
        });
        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.show();
    }

    private class DeleteMedicine extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.deleteReminder(strings[0]);
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
            Log.d("DeleteReminder", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(ReminderDetails.this);
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

                        Toast.makeText(ReminderDetails.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        finish();

                    } else {
                        String error = json.getString("Data");
                        Log.d("DeleteReminder", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("DeleteReminder", e.getMessage());
                }
            }
        }
    }

}
