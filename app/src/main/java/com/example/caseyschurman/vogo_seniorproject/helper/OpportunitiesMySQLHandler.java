package com.example.caseyschurman.vogo_seniorproject.helper;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.example.caseyschurman.vogo_seniorproject.activity.MainActivity;
import com.example.caseyschurman.vogo_seniorproject.app.AppConfig;
import com.example.caseyschurman.vogo_seniorproject.app.AppController;
import com.example.caseyschurman.vogo_seniorproject.app.VolunteerOpportunity;
import com.example.caseyschurman.vogo_seniorproject.helper.VolunteerOpportunityModel;
import com.example.caseyschurman.vogo_seniorproject.app.getComplete;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Created by casey.schurman on 3/1/2016.
 */
public class OpportunitiesMySQLHandler {

    private static final String TAG = OpportunitiesMySQLHandler.class.getSimpleName();

    public static ArrayList<VolunteerOpportunity> getOpportunities(final getComplete taskCompleted) {

        final ArrayList<VolunteerOpportunity> opportunities = new ArrayList<VolunteerOpportunity>();

        //Tag used to cancel the request
        String tag_string_req = "req_getOpportunities";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_VORetrieval, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d(TAG, "GET Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);

                    int success = jObj.getInt("success");

                    if(success == 1) {

                        JSONArray ja = jObj.getJSONArray("opportunities");

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject tempObj = ja.getJSONObject(i);

                            String name = tempObj.getString("name");
                            String relevant_skills = tempObj.getString("relevant_skills");
                            String organization = tempObj.getString("organization");
                            String address = tempObj.getString("address");
                            ArrayList<String> skills = new ArrayList<String>(Arrays.asList(relevant_skills.split("\\|")));
                            VolunteerOpportunity vo = new VolunteerOpportunity(name, skills, organization, address);
                            opportunities.add(i, vo);
                        }

                        taskCompleted.onGetCompleted(opportunities, false, null);

                    }
                    } catch (JSONException e) {
                        //error
                        e.printStackTrace();
                        Log.e(TAG, "error: " + e.getMessage());
                        taskCompleted.onGetCompleted(null, true, null);
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "GET Error: " + error.getMessage());
                    taskCompleted.onGetCompleted(null,true,null);
                }
            });

            //Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        return opportunities;
    }
}
