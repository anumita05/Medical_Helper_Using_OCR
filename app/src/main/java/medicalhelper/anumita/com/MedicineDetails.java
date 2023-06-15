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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import medicalhelper.anumita.com.OCR.OcrCaptureActivity;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;

public class MedicineDetails extends AppCompatActivity {
    public static final int PERMISSION_CODE = 1001;
    public static final int IMAGE_REQUEST_CODE = 1002;
    private static final String TAG = MedicineDetails.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private String UID;

    private ImageView Med_pic;
    private EditText Med_name, Med_color, Med_details, Med_expiryDate;
    private Spinner Med_type,Med_Size;
    private TableRow tblMedrow;
    private TextView btnSubmit, btnCancel, btnEdit, btnChaneImage;

    private String medId="",medName="", medType="", medColor="", medSize="", medDetails="", medPic="", medExpDate="NA";

    private EditText[] editTexts;
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

        getSupportActionBar().setTitle("Medicine Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        currentDate = Calendar.getInstance();

        initView();

        Bundle bundle = getIntent().getExtras();

        if(bundle !=  null){
            medId = bundle.getString(UtilConstants.Med_Tid);
            medName = bundle.getString(UtilConstants.Med_Name);
            medType = bundle.getString(UtilConstants.Med_Type);
            medColor = bundle.getString(UtilConstants.Med_Color);
            medSize = bundle.getString(UtilConstants.Med_Size);
            medDetails = bundle.getString(UtilConstants.Med_Details);
            medPic = UtilConstants.getMedPicImage();
            medExpDate = bundle.getString(UtilConstants.Med_EXPDATE);
        }

        setValues();

        hideUpdateAndDisable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDialog = new Dialog(MedicineDetails.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        Med_expiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog = new DatePickerDialog(MedicineDetails.this, dateListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(new Date().getTime());
                if(!datePickerDialog.isShowing())
                    datePickerDialog.show();

            }
        });

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

        btnChaneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(MedicineDetails.this,
                        Manifest.permission.CAMERA);
                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    // permission is not granted yet, let's ask for it
                    ActivityCompat.requestPermissions(MedicineDetails.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);

                } else {

                    Intent intent = new Intent(MedicineDetails.this, OcrCaptureActivity.class);
                    intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                    intent.putExtra(OcrCaptureActivity.UseFlash, false);

                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                }
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

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUpdateAndDisable(true);
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            currentDate.set(Calendar.YEAR, year);
            currentDate.set(Calendar.MONTH, month);
            currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            Med_expiryDate.setText(dateFormat.format(currentDate.getTime()));
        }
    };

    private void updateMedicine() {
        if(Med_name.getText().length() == 0){
            showSnackbar("Please, Enter Medicine Name", Med_name);
        }else if(Med_color.getText().length() == 0){
            showSnackbar("Please, Enter Medicine Color", Med_name);
        }else if(Med_details.getText().length() == 0){
            showSnackbar("Please, Enter Medicine Description", Med_name);
        }else {
//            String uid,String tid,String name,String type,String color,String size,String details,String pic
            if(captureImage.length() > 0){
                new UpdateMedicine().execute(UID, medId, Med_name.getText().toString(),Med_type.getSelectedItem().toString(),Med_color.getText().toString(),
                        Med_Size.getSelectedItem().toString(), Med_details.getText().toString(), captureImage, dateFormat.format(currentDate.getTime()));
            }else {
                new UpdateMedicine().execute(UID, medId, Med_name.getText().toString(),Med_type.getSelectedItem().toString(),Med_color.getText().toString(),
                        Med_Size.getSelectedItem().toString(), Med_details.getText().toString(), medPic, dateFormat.format(currentDate.getTime()));
            }
        }
    }

    private void showSnackbar(@NonNull String message,@NonNull EditText editText) {
        Snackbar.make(btnSubmit, message, Snackbar.LENGTH_SHORT).show();
        editText.requestFocus();
    }

    private void setSubCategory(int pos) {
        int position = -1;
        String[] resources = new String[]{};
        switch (pos){
            case 0:
                resources = getResources().getStringArray(R.array.type_tablet);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 1:
                resources = getResources().getStringArray(R.array.type_syrup);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 2:
                resources = getResources().getStringArray(R.array.type_capsule);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 3:
                resources = getResources().getStringArray(R.array.type_inhaler);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 4:
                resources = getResources().getStringArray(R.array.type_syringe);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 5:
                resources = getResources().getStringArray(R.array.type_ointment);
                for(int i = 0;i<resources.length;i++){
                    if(resources[i].compareTo(medSize) == 0){
                        position = i;
                        break;
                    }
                }
                break;
            case 6:
                break;

        }
        if(resources.length > 0){
            gsubposition = position;
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MedicineDetails.this, R.layout.spinner_item_med,resources);
            adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
            Med_Size.setAdapter(adapter);
            if(position > -1)
                Med_Size.setSelection(position);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_CODE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                btnChaneImage.performClick();

            } else {
                ActivityCompat.requestPermissions(MedicineDetails.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
            }
        }
    }

    private void setValues() {
        Med_name.setText(medName); Med_color.setText(medColor);

        String[] resources = getResources().getStringArray(R.array.medicine_type);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MedicineDetails.this, R.layout.spinner_item_med,resources);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
        Med_type.setAdapter(adapter);

//        Toast.makeText(MedicineDetails.this, medType+" - "+gtposition, Toast.LENGTH_SHORT).show();

        for(int i = 0;i<resources.length;i++){
            Log.d("MedType", resources[i]);
            if(resources[i].compareTo(medType) == 0){
                gtposition = i;
                break;
            }
        }

        Med_type.setSelection(gtposition);

        Med_details.setText(medDetails);

        Med_pic.setImageBitmap(UtilConstants.getBitmap(medPic));

        SetdateToCalender();

        Med_expiryDate.setText(medExpDate);
    }

    private void SetdateToCalender() {
        if(medExpDate != null && medExpDate.contains("/")){
            String[] date = medExpDate.split("/");
            currentDate.set(Calendar.YEAR,Integer.parseInt( date[0]));
            currentDate.set(Calendar.MONTH,Integer.parseInt( date[1]));
            currentDate.set(Calendar.DAY_OF_MONTH,Integer.parseInt( date[2]));
        }
    }

    private void initView() {
        Med_pic = findViewById(R.id.medicine_image);
        Med_name = findViewById(R.id.medicine_name);
        Med_color = findViewById(R.id.medicine_color);
        Med_details = findViewById(R.id.medicine_details);
        Med_expiryDate = findViewById(R.id.medicine_expiryDate);

        Med_type = findViewById(R.id.medicine_type);
        Med_Size = findViewById(R.id.medicine_size);

        editTexts = new EditText[]{Med_name, Med_color, Med_details};

        tblMedrow = findViewById(R.id.update_row_med);
        btnSubmit = findViewById(R.id.btnUpdateMed);
        btnCancel = findViewById(R.id.btnCancelMed);
        btnEdit = findViewById(R.id.btnEditMed);

        btnChaneImage = findViewById(R.id.changeImage);

    }

    private void showUpdateAndEnable() {

        for (EditText editText : editTexts) {
            editText.setEnabled(true);
        }

        Med_type.setEnabled(true);
        Med_Size.setEnabled(true);

        if(btnChaneImage.getVisibility() == View.GONE)
            btnChaneImage.setVisibility(View.VISIBLE);

        if(tblMedrow.getVisibility() == View.GONE)
            tblMedrow.setVisibility(View.VISIBLE);

        if(btnEdit.getVisibility() == View.VISIBLE)
            btnEdit.setVisibility(View.GONE);

    }

    private void hideUpdateAndDisable(boolean default_v) {

        for (EditText editText : editTexts) {
            editText.setEnabled(false);
        }

        Med_type.setEnabled(false);
        Med_Size.setEnabled(false);

        if(btnChaneImage.getVisibility() == View.VISIBLE)
            btnChaneImage.setVisibility(View.GONE);

        if(tblMedrow.getVisibility() == View.VISIBLE)
            tblMedrow.setVisibility(View.GONE);

        if(btnEdit.getVisibility() == View.GONE)
            btnEdit.setVisibility(View.VISIBLE);

        if(default_v){
            captureImage = "";
            capturedImage = new byte[]{};
            Med_name.setText(medName); Med_color.setText(medColor);
            Med_details.setText(medDetails);
            Med_type.setSelection(gtposition);
            Med_pic.setImageBitmap(UtilConstants.getBitmap(medPic));
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
                        Log.d("CAPTUREDIMAGE_NULL", "Bitmap is null ");
                    }

                    Med_name.setText(capturedText);
                    Log.d(TAG, "Text read: " + capturedText);
                } else {
//                    statusMessage.setText(R.string.ocr_failure);
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

    private class UpdateMedicine extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.updateMedicine(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
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
            Log.d("UpdateMedicine", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MedicineDetails.this);
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

                        hideUpdateAndDisable(false);

                    }else if (StatusValue.compareTo("already") == 0) {

                        hideUpdateAndDisable(false);

                        AlertDialog.Builder ad = new AlertDialog.Builder(MedicineDetails.this);
                        ad.setTitle("Medicine already Exist");
                        ad.setMessage("A medicine with same name exist, Cannot update");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();

                    } else {
                        String error = json.getString("Data");
                        Log.d("UpdateMedicine", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("UpdateMedicine", e.getMessage());
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
        if(item.getItemId() == android.R.id.home){
            finish();
        }else if(item.getItemId() == R.id.action_delete){
            showDeleteAlert();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAlert() {
        AlertDialog.Builder ad = new AlertDialog.Builder(MedicineDetails.this);
        ad.setTitle("Are You Sure?");
        ad.setMessage("This medicine will be deleted.");
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                new DeleteMedicine().execute(medId);
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
                JSONObject jsonObject = restAPI.deleteMedicine(strings[0]);
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
            Log.d("DeleteMedicine", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MedicineDetails.this);
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

                        Toast.makeText(MedicineDetails.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        finish();

                    }else {
                        String error = json.getString("Data");
                        Log.d("DeleteMedicine", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("DeleteMedicine", e.getMessage());
                }
            }
        }
    }
}
