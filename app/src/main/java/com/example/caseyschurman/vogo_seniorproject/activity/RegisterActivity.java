/*
* Created By: Casey Schurman
* Purpose: RegisterActivity is the activity for the register page.
* Summary: After user enters in registration information, and selects the registration button, that
* information will be sent via a POST request to the PHP script (Which is on 000freewebhost server)
* for actually creating the user in the database. If credentials are validated, the user will be
* forwarded to the LoginActivity, where they can enter their login credentials.
* */

package com.example.caseyschurman.vogo_seniorproject.activity;

//Android libraries
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//Volley libraries
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

//JSON libraries
import org.json.JSONException;
import org.json.JSONObject;

//Java libraries
import java.util.HashMap;
import java.util.Map;

//Project libraries
import com.example.caseyschurman.vogo_seniorproject.R;
import com.example.caseyschurman.vogo_seniorproject.app.AppConfig;
import com.example.caseyschurman.vogo_seniorproject.app.AppController;
import com.example.caseyschurman.vogo_seniorproject.helper.SQLiteHandler;
import com.example.caseyschurman.vogo_seniorproject.helper.SessionManager;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFirstName = (EditText) findViewById(R.id.first_name);
        inputLastName = (EditText) findViewById(R.id.last_name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Session manager
        session = new SessionManager(getApplicationContext());

        //SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        //Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String first_name = inputFirstName.getText().toString().trim();
                String last_name = inputLastName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (!first_name.isEmpty() && !last_name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {

                    boolean emailResponse = isValidEmail(email);
                    boolean passwordResponse = isValidPassword(password);

                    if(emailResponse && passwordResponse) {
                        registerUser(first_name, last_name, email, password);
                    } else if (!emailResponse){
                        Toast.makeText(getApplicationContext(),
                                "Please enter a valid email!", Toast.LENGTH_LONG)
                                .show();
                    } else if(!passwordResponse) {
                        Toast.makeText(getApplicationContext(),
                                "Password must be at least 5 characters long and contain at least 1 digit", Toast.LENGTH_LONG)
                                .show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        //Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String first_name, final String last_name, final String email,
                              final String password) {
        //Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        //User successfully stored in MySQL
                        //Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String first_name = user.getString("first_name");
                        String last_name = user.getString("last_name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");

                        //Inserting row in users table
                        db.addUser(first_name, last_name, email, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        //Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        //Error occurred in registration. Get the error
                        //message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                //Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("first_name", first_name);
                params.put("last_name", last_name);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //method to check valid email
    private boolean isValidEmail(String email){
        Boolean isValid = false;

        if (email.length() > 10 && email.contains("@") && email.contains(".com")) {
            isValid = true;
        }

        return isValid;
    }

    //method to check valid password
    private boolean isValidPassword(String password){
        Boolean isValid = true;
        Boolean containsDigit = false;

        if(password.length() > 4){
            char[] arrPassword = password.toCharArray();

            for(int i = 0; i < password.length(); i++) {
                if (Character.isDigit(arrPassword[i])) {
                    containsDigit = true;
                }
            }

            if (!containsDigit) {
                isValid = false;
            }
        }
        else
        {
            isValid = false;
        }

        return isValid;
    }
}