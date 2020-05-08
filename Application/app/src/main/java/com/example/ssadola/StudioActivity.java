package com.example.ssadola;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class StudioActivity extends AppCompatActivity {

    private static final String TAG_WORK_NM = "WORK_NM";
    private static final String TAG_POTOGRF_PLC_NM= "POTOGRF_PLC_NM";
    private static final String TAG_POTOGRF_YY ="POTOGRF_YY";


    private static final String open_gg_api_key = "=ebdf40781aa946c6af58a9e4ee3a91c6";
    ArrayList<HashMap<String,String>> mArrayList;
    ListView mListView;
    String sigun_nm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sigun_nm = query;
                OpenApiExplorer searchAPI = new OpenApiExplorer();
                searchAPI.execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mListView = findViewById(R.id.listView2);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView work_nm = view.findViewById(R.id.list_work_nm);
                TextView plc_nm = view.findViewById(R.id.list_plc_nm);
                //Toast.makeText(StudioActivity.this,plc_nm.getText().toString(),Toast.LENGTH_LONG).show();
                Bundle bundle = new Bundle();
                bundle.putString( "work_nm", work_nm.getText().toString());
                bundle.putString( "plc_nm", plc_nm.getText().toString());
                bundle.putString( "sigun_nm", sigun_nm);
                Intent img = new Intent(StudioActivity.this,ImageStudioActivity.class);
                img.putExtras(bundle);
                startActivity(img);
            }
        });

        mArrayList = new ArrayList<>();
    }
    protected void showResult(String result){
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray tmp = jsonObject.getJSONArray("PhotographySupport");
            JSONObject tmp2 = tmp.getJSONObject(1);
            JSONArray jsonArray = tmp2.getJSONArray("row");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject item = jsonArray.getJSONObject(i);

                String work_name = item.getString(TAG_WORK_NM);
                String plc_name = item.getString(TAG_POTOGRF_PLC_NM);
                String year = item.getString(TAG_POTOGRF_YY);

                HashMap<String,String> hashMap = new HashMap<>();

                hashMap.put(TAG_WORK_NM, work_name);
                hashMap.put(TAG_POTOGRF_PLC_NM, plc_name);
                hashMap.put(TAG_POTOGRF_YY, year);

                mArrayList.add(hashMap);

            }

        } catch (JSONException e) {
            Log.d("StudioActivity", "showResult : ", e);
        }
        ListAdapter adapter = new SimpleAdapter(
                StudioActivity.this, mArrayList, R.layout.item_list,
                new String[]{TAG_WORK_NM,TAG_POTOGRF_PLC_NM, TAG_POTOGRF_YY},
                new int[]{R.id.list_work_nm, R.id.list_plc_nm, R.id.list_yy}
        );

        mListView.setAdapter(adapter);
    }

    class OpenApiExplorer extends AsyncTask<String, Void, String> {
        ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(StudioActivity.this, "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loading.dismiss();
            if(result == null){
                Toast.makeText(StudioActivity.this,"resut is null",Toast.LENGTH_LONG).show();
            }else{
                showResult(result);
            }
        }
        protected String doInBackground(String... params) {
            try {
                String SIGUN_NM = params[0];

                StringBuilder urlBuilder = new StringBuilder("https://openapi.gg.go.kr/PhotographySupport"); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("KEY","UTF-8") + open_gg_api_key); /*Service Key*/
                //urlBuilder.append("&" + URLEncoder.encode("KEY","UTF-8") + "=" + URLEncoder.encode("-", "UTF-8")); /*공공데이터포털에서 받은 인증키*/
                urlBuilder.append("&" + URLEncoder.encode("Type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*페이지번호*/
                urlBuilder.append("&" + URLEncoder.encode("pIndex","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("pSize","UTF-8") + "=" + URLEncoder.encode("20", "UTF-8"));
                urlBuilder.append("&" + URLEncoder.encode("SIGUN_NM","UTF-8") + "=" + URLEncoder.encode(SIGUN_NM, "UTF-8"));
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-type", "application/json");
                System.out.println("Response code: " + conn.getResponseCode());
                BufferedReader rd;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                conn.disconnect();
                return sb.toString();
            }catch (Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }
    }


}
