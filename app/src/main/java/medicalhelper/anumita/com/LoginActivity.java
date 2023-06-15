package medicalhelper.anumita.com;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import medicalhelper.anumita.com.doctor.MainActivity;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;
import medicalhelper.anumita.com.patient.MainActivityPatient;


public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    private String username="", userType = "";
    private EditText useremail, userpassword;
    private Spinner usertype;
    private Button login, register;

    private Dialog mDialog;
    private String selected = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(UtilConstants.UserName, "");
        userType = sharedPreferences.getString(UtilConstants.UserType, "");
        if(username.compareTo("") == 0){

            setContentView(R.layout.activity_login);

            mDialog = new Dialog(LoginActivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDialog.setContentView(R.layout.loading);
            mDialog.setCancelable(false);

            useremail = findViewById(R.id.user_email);
            userpassword = findViewById(R.id.employee_pass);
            login = findViewById(R.id.btn_SignIn);
            usertype = findViewById(R.id.spinUserType);
            register = findViewById(R.id.btn_Register);

            usertype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selected = usertype.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }else {
//            Toast.makeText(this, "UserLoggedIn", Toast.LENGTH_SHORT).show();

            if(userType.compareTo("Doctor") == 0){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                startActivity(intent);
                finish();
            }
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        setUpSpinner();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(useremail.getText().toString().length() == 0){
                    Snackbar.make(v, "Please Enter Your Email", Snackbar.LENGTH_SHORT).show();
                    useremail.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(useremail.getText().toString()).matches()){
                    Snackbar.make(v, "Please Enter Valid Your Email", Snackbar.LENGTH_SHORT).show();
                    useremail.requestFocus();
                }else if(userpassword.getText().toString().length() == 0){
                    Snackbar.make(v, "Please Enter Your Password", Snackbar.LENGTH_SHORT).show();
                    userpassword.requestFocus();
                }else {
                    new Login().execute(useremail.getText().toString(), userpassword.getText().toString(), selected);
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setUpSpinner() {
        ArrayList<String> type = new ArrayList<String>();
        type.add("Doctor");
        type.add("Patient");

        ArrayAdapter<String> adapter =  new ArrayAdapter<String>(LoginActivity.this, R.layout.spinner_item, type);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
        usertype.setAdapter(adapter);

    }

    private class Login extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.Login(strings[0], strings[1], strings[2]);
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
            Log.d("LoginActivity", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
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

                        JSONArray jsonArray = json.getJSONArray("Data");
                        json = jsonArray.getJSONObject(0);

                        SharedPreferences.Editor login = sharedPreferences.edit();

                        login.putString(UtilConstants.UID, json.getString("data0"));
                        login.putString(UtilConstants.UserName, json.getString("data1"));
                        login.putString(UtilConstants.UserEmail, useremail.getText().toString());
                        login.putString(UtilConstants.UserType, usertype.getSelectedItem().toString());
                        login.apply();
                        login.commit();


                        if(usertype.getSelectedItemPosition() == 0){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                            startActivity(intent);
                            finish();
                        }

//                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    } else if (StatusValue.compareTo("false") == 0) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
                        ad.setTitle("Authentication Error");
                        ad.setMessage("Email Or Password you entered is Incorrect");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("LoginActivity", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", e.getMessage());
                }
            }
        }
    }
}
