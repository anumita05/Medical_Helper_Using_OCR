package medicalhelper.anumita.com.patient;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import medicalhelper.anumita.com.AddMedicine;
import medicalhelper.anumita.com.MedicineDetails;
import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.TabletAdapter;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;


public class TabletFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private String uid = "";

    private FloatingActionButton addTablets;
    private ListView tablets_list;

    private ArrayList<String> tid,tuid,name,type,color,size,detail,pic, expDate;

    private Dialog mDialog;

    public TabletFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        uid = sharedPreferences.getString(UtilConstants.UID, "");

        mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_tablet, container, false);
        tablets_list = view.findViewById(R.id.tablets_list);
        addTablets = view.findViewById(R.id.addMedicines);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        tablets_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle bundle = new Bundle();
                bundle.putString(UtilConstants.Med_Tid, tid.get(position));
                bundle.putString(UtilConstants.Med_Name, name.get(position));
                bundle.putString(UtilConstants.Med_Type, type.get(position));
                bundle.putString(UtilConstants.Med_Color, color.get(position));
                bundle.putString(UtilConstants.Med_Size, size.get(position));
                bundle.putString(UtilConstants.Med_Details, detail.get(position));
                bundle.putString(UtilConstants.Med_EXPDATE, expDate.get(position));
                UtilConstants.setMedPicImage(pic.get(position));

                Intent intent = new Intent(getActivity(), MedicineDetails.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        addTablets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddMedicine.class);
                intent.putExtra(UtilConstants.Pat_ID, uid);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetTablets().execute(uid);
    }

    private class GetTablets extends AsyncTask<String, JSONObject, String> {

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
                        tid=new ArrayList<String>();
                        tuid=new ArrayList<String>();
                        name=new ArrayList<String>();
                        type=new ArrayList<String>();
                        color=new ArrayList<String>();
                        size=new ArrayList<String>();
                        detail=new ArrayList<String>();
                        pic=new ArrayList<String>();
                        expDate=new ArrayList<String>();

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
                            expDate.add(json.getString("data8"));
                        }

                        TabletAdapter tabletAdapter = new TabletAdapter(getActivity(), R.layout.tablet_item, name, type, pic);
                        tablets_list.setAdapter(tabletAdapter);

                    } else if (StatusValue.compareTo("no") == 0) {
                        tablets_list.setAdapter(null);
                        Toast.makeText(getActivity(), "No Medicines found, Please Click Add button to Add one", Toast.LENGTH_SHORT).show();
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

}
