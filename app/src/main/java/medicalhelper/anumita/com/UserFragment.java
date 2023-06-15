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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
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

import medicalhelper.anumita.com.chat.ChatActivity;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;


public class UserFragment extends Fragment {
    private static final String REQUEST_TYPE = "RType";
    private static final String REQUEST_SRC = "RSrc";
    private static final String REQUEST_VAL = "RVal";

    private SharedPreferences sharedPreferences;
    private String UID = "", ReqType = "", ReqSrc = "", ReqVal = "";

    private SearchView searchView;
    private ListView patient_list;
    private Dialog mDialog;

    private ArrayList<String> tuid,name,contact,email,address;


    public UserFragment() {
    }

    public UserFragment newInstance(@NonNull String UType, @NonNull String USrc, @NonNull String UVal){
        UserFragment userFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(REQUEST_TYPE, UType);
        bundle.putString(REQUEST_SRC, USrc);
        bundle.putString(REQUEST_VAL, UVal);
        userFragment.setArguments(bundle);
        return userFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        Bundle bundle = getArguments();
        if(bundle != null){
            ReqType = bundle.getString(REQUEST_TYPE);
            ReqSrc = bundle.getString(REQUEST_SRC);
            ReqVal = bundle.getString(REQUEST_VAL);
        }

        mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_patient, container, false);
        searchView = view.findViewById(R.id.patient_search);
        patient_list = view.findViewById(R.id.list_patient);

//        Toast.makeText(getActivity(), ReqType+"\n"+ReqSrc+"\n"+ReqVal, Toast.LENGTH_SHORT).show();

        new GetPatient().execute(ReqType, ReqSrc, ReqVal);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                new GetPatient().execute(ReqType, "Name",s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                new GetPatient().execute(ReqType, "All","na");
                return false;
            }
        });

        patient_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString(UtilConstants.Pat_ID, tuid.get(position));
                bundle.putString(UtilConstants.Pat_Name, name.get(position));
                bundle.putString(UtilConstants.Pat_Contact, contact.get(position));
                bundle.putString(UtilConstants.Pat_Email, email.get(position));
                bundle.putString(UtilConstants.Pat_Addr, address.get(position));
                bundle.putString(UtilConstants.UserType,ReqType);

                Intent intent = new Intent(getActivity(), UserDetails.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }

    //type - Doctor/Patient
    //src - All/Name
    //if src - Name , Val - name of pat
    private class GetPatient extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            patient_list.setAdapter(null);
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            try {
                JSONObject jsonObject = restAPI.Searchuser(strings[0], strings[1], strings[2]);
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
            Log.d("GetPatient", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
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


                        tuid=new ArrayList<String>();
                        name=new ArrayList<String>();
                        contact=new ArrayList<String>();
                        email=new ArrayList<String>();
                        address=new ArrayList<String>();


                        JSONArray jsonArray = json.getJSONArray("Data");

                        for(int i = 0;i<jsonArray.length();i++){

                            json = jsonArray.getJSONObject(i);

                            tuid.add(json.getString("data0"));
                            name.add(json.getString("data1"));
                            contact.add(json.getString("data2"));
                            email.add(json.getString("data3"));
                            address.add(json.getString("data4"));
                        }

                        PatientAdapter patientAdapter = new PatientAdapter(getActivity().getApplicationContext(), R.layout.reminder_item, name);
                        patient_list.setAdapter(patientAdapter);

                    } else if (StatusValue.compareTo("no") == 0) {
                        patient_list.setAdapter(null);
                        Toast.makeText(getActivity(), "No "+ReqType+" found with this name, Try again", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("GetPatient", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("GetPatient", e.getMessage());
                }
            }
        }
    }

    public class PatientAdapter extends ArrayAdapter<String> {

        private Context appContext;

        public PatientAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> name) {
            super(context, resource, name);
            appContext = context;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = LayoutInflater.from(appContext).inflate(R.layout.tablet_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tab_image = convertView.findViewById(R.id.tablet_item_image);
                viewHolder.person_name = convertView.findViewById(R.id.tablet_item_name);
                viewHolder.person_contact = convertView.findViewById(R.id.tablet_item_type);
                viewHolder.chat_btn = convertView.findViewById(R.id.chat_view);
                convertView.setTag(viewHolder);
            }else {
                viewHolder =(ViewHolder) convertView.getTag();
            }


            viewHolder.tab_image.setImageDrawable(getResources().getDrawable(R.mipmap.ic_default));
            viewHolder.person_name.setText(name.get(position));
            viewHolder.person_contact.setTextColor(getResources().getColor(R.color.primaryTextColor));
            viewHolder.person_contact.setText(contact.get(position));
            viewHolder.chat_btn.setVisibility(View.VISIBLE);
            viewHolder.chat_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
                    intent.putExtra(UtilConstants.Chat_ID, tuid.get(position));
                    intent.putExtra(UtilConstants.Chat_Type, "Doctor");
                    intent.putExtra(UtilConstants.Chat_Name, sharedPreferences.getString(UtilConstants.UserName, ""));
                    intent.putExtra(UtilConstants.Chat_NID, 0);
                    startActivity(intent);
                }
            });

            return convertView;
        }

        private class ViewHolder{
            ImageView tab_image, chat_btn;
            TextView person_name, person_contact;
        }
    }
}
