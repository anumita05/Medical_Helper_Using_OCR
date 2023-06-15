package medicalhelper.anumita.com;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

import medicalhelper.anumita.com.OCR.OcrCaptureActivity;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class AddMedicine extends AppCompatActivity {

    public static final int PERMISSION_CODE = 1001;
    public static final int IMAGE_REQUEST_CODE = 1002;
    private static final String TAG = MedicineDetails.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private String UID, PID = "";

    private ImageView Med_pic;
    private EditText Med_name, Med_color, Med_details, Med_expirydate;
    private Spinner Med_type,Med_Size;
    private TextView btnAddMedicine;

    private int gtposition = -1, gsubposition=-1;

    private byte[] capturedImage = new byte[]{};
    private String capturedText = "";
    private String captureImage = "";
    private Dialog mDialog;

    private DatePickerDialog datePickerDialog;
    private Calendar currentDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        getSupportActionBar().setTitle("Add Medicine");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDialog = new Dialog(AddMedicine.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        initView();

        Med_pic.setImageDrawable(getResources().getDrawable(R.mipmap.ic_default_img));

        btnAddMedicine.setText("Add Medicine");
        String[] resources = getResources().getStringArray(R.array.medicine_type);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(AddMedicine.this, R.layout.spinner_item_med,resources);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
        Med_type.setAdapter(adapter);

        Intent intent = getIntent();
        PID = intent.getStringExtra(UtilConstants.Pat_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Med_expirydate.setText(dateFormat.format(currentDate.getTime()));

        Med_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSubCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Med_expirydate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(AddMedicine.this, dateListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                                    currentDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
                if(!datePickerDialog.isShowing())
                    datePickerDialog.show();

            }
        });

        Med_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(AddMedicine.this,
                        Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    // permission is not granted yet, let's ask for it
                    ActivityCompat.requestPermissions(AddMedicine.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);

                } else {

                    Intent intent = new Intent(AddMedicine.this, OcrCaptureActivity.class);
                    intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                    intent.putExtra(OcrCaptureActivity.UseFlash, false);
                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                }
            }
        });

        btnAddMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(AddMedicine.this, Med_name.getText().toString()+"\n"+Med_type.getSelectedItem().toString()+"\n"+
//                                Med_color.getText().toString()+"\n"+Med_Size.getSelectedItem().toString()+"\n"+Med_details.getText().toString(), Toast.LENGTH_SHORT).show();
                if(captureImage.length() == 0){
                    Snackbar.make(btnAddMedicine, "Please, Capture an image of Medicine", Snackbar.LENGTH_SHORT).show();
                } else if(Med_name.getText().length() == 0){
                    showSnackbar("Please, Enter Medicine Name", Med_name);
                }else if(Med_color.getText().length() == 0){
                    showSnackbar("Please, Enter Medicine Color", Med_name);
                }else if(Med_details.getText().length() == 0){
                    showSnackbar("Please, Enter Medicine Description", Med_name);
                }else {
                    //string name,string type,string color,string size,string details,string pic
                    new addMedicine().execute(PID, Med_name.getText().toString(), Med_type.getSelectedItem().toString(), Med_color.getText().toString(), Med_Size.getSelectedItem().toString(),
                                            Med_details.getText().toString(), captureImage, dateFormat.format(currentDate.getTime()));
                }
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            currentDate.set(Calendar.YEAR, year);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Med_expirydate.setText(dateFormat.format(currentDate.getTime()));
        }
    };

    private void showSnackbar(@NonNull String message,@NonNull EditText editText) {
        Snackbar.make(btnAddMedicine, message, Snackbar.LENGTH_SHORT).show();
        editText.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_CODE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Med_pic.performClick();

            } else {
                ActivityCompat.requestPermissions(AddMedicine.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
            }
        }
    }

    private void initView() {
        Med_pic = findViewById(R.id.medicine_image);
        Med_name = findViewById(R.id.medicine_name);
        Med_color = findViewById(R.id.medicine_color);
        Med_details = findViewById(R.id.medicine_details);
        Med_expirydate = findViewById(R.id.medicine_expiryDate);

        Med_type = findViewById(R.id.medicine_type);
        Med_Size = findViewById(R.id.medicine_size);

        btnAddMedicine = findViewById(R.id.btnEditMed);

    }

    private void setSubCategory(int pos) {

        String[] resources = new String[]{};
        switch (pos){
            case 0:
                resources = getResources().getStringArray(R.array.type_tablet);
                break;
            case 1:
                resources = getResources().getStringArray(R.array.type_syrup);
                break;
            case 2:
                resources = getResources().getStringArray(R.array.type_capsule);
                break;
            case 3:
                resources = getResources().getStringArray(R.array.type_inhaler);
                break;
            case 4:
                resources = getResources().getStringArray(R.array.type_syringe);
                break;
            case 5:
                resources = getResources().getStringArray(R.array.type_ointment);
                break;
            case 6:
                break;

        }
        if(resources.length > 0){
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(AddMedicine.this, R.layout.spinner_item_med,resources);
            adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
            Med_Size.setAdapter(adapter);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    capturedText = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    capturedImage = UtilConstants.getImageBytes();
                    if(capturedImage != null){
                        Bitmap bhmp = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);
                        Bitmap bmtp = UtilConstants.getResizedBitmap(bhmp,500,500);
                        Med_pic.setImageBitmap(bmtp);
                        new ProcessImage().execute(bmtp);
                    } else {
                        Med_pic.setImageDrawable(getResources().getDrawable(R.mipmap.ic_default_img));
                    }

                    Med_name.setText(capturedText);
                    Log.d(TAG, "Text read: " + capturedText);
                } else {
                    Toast.makeText(this.getApplicationContext(), "It was not possible to detect the Medicine Name. Try Again", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                Toast.makeText(this.getApplicationContext(),String.format(getString(R.string.ocr_error), CommonStatusCodes.getStatusCodeString(resultCode)) , Toast.LENGTH_LONG).show();
//                statusMessage.setText(String.format(getString(R.string.ocr_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
    }

    public class ProcessImage extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            captureImage = s;
        }
    }

    private class addMedicine extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.addMedicine(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
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
            Log.d("AddMedicine", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(AddMedicine.this);
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

                        Toast.makeText(AddMedicine.this, "Medicine Successfully Added", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 100);

                    }else if (StatusValue.compareTo("already") == 0) {

                        AlertDialog.Builder ad = new AlertDialog.Builder(AddMedicine.this);
                        ad.setTitle("Medicine already Exist");
                        ad.setMessage("Could not add this medicine, a medicine with same name exists, Try again with different name");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();

                    } else {
                        String error = json.getString("Data");
                        Log.d("AddMedicine", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AddMedicine", e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
