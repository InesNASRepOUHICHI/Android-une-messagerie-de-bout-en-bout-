package edu.unice.messenger.messageriembds.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
 
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;
 
import java.util.HashMap;
import java.util.Map;

import edu.unice.messenger.messageriembds.Model.User;
import edu.unice.messenger.messageriembds.R;
import edu.unice.messenger.messageriembds.app.AppConfig;
import edu.unice.messenger.messageriembds.app.AppController;
import edu.unice.messenger.messageriembds.helper.RestClient;
import edu.unice.messenger.messageriembds.helper.SQLiteHandler;
import edu.unice.messenger.messageriembds.helper.SessionManager;
 
public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputUsername;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        inputUsername = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
 
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
 
        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
 
        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }
 
        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
 
                if (!username.isEmpty() && !password.isEmpty()) {
                    registerUser(username, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Veuillez rentrer vos coordonnées SVP!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
 
        // Link to Login Screen
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
     * username, password) to register url
     * */
    private void registerUser(final String username,
                              final String password) {
        // Tag used to cancel the request
        String tag_json_req = "req_register";
 
        pDialog.setMessage("Création du nouvel utilisateur ...");
        showDialog();

        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> request = new RestClient().createJsonRequest(Method.POST, AppConfig.URL_REGISTER, params, new Response.Listener<JSONObject>() {

            public void onResponse(JSONObject response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    String username = response.getString("username");
                    //String access_token = response.getString("access_token");
                    User user = new User(username, password, null);

                    // Launch main activity
                    Toast.makeText(getApplicationContext(), "Utilisateur créé. Essayer de s'authentifier!", Toast.LENGTH_LONG).show();
                    // Launch login activity
                    Intent intent = new Intent(
                            RegisterActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Erreur de création du nouvel utilisateur", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Erreur de création du nouvel utilisateur: "+error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        });



        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request, tag_json_req);
    }
 
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
 
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}