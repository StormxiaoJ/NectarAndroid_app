package com.jianqingc.nectar.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.android.volley.AuthFailureError;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.jianqingc.nectar.activity.LoginActivity;
import com.jianqingc.nectar.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by Jianqing Chen on 2016/10/2.
 */
public class HttpRequestController {
    private Context mApplicationContext;
    private static HttpRequestController mInstance;
    private SharedPreferences sharedPreferences;

    public interface VolleyCallback {
        void onSuccess(String result);

    }

    public static HttpRequestController getInstance(Context context) {
        if (mInstance == null)
            mInstance = new HttpRequestController(context);
        return mInstance;

    }

    public HttpRequestController(Context context) {
        this.mApplicationContext = context.getApplicationContext();
    }

    /**
     * Login Http Request sent to Keystone.
     * @param tenantName
     * @param username
     * @param password
     * @param context
     */

    public void loginHttp(String tenantName, String username, String password, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String loginUri = "https://keystone.rc.nectar.org.au:5000/v2.0/tokens";
        /**
         * Assemble Json Object According to NeCTAR API documentation
         */
        JSONObject json0 = new JSONObject();
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("username", username);
            json2.put("password", password);
            json1.put("tenantName", tenantName);
            json1.put("passwordCredentials", json2);
            json0.put("auth", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, loginUri, json0, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ResponseParser.getInstance(mApplicationContext).loginParser(response);
                Intent i = new Intent(mApplicationContext, MainActivity.class);
                SharedPreferences sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                /**
                 * Enable auto-login function
                 */
                editor.putBoolean("isSignedOut", false);
                editor.apply();
                context.startActivity(i);
                Toast.makeText(mApplicationContext, "Login Succeed", Toast.LENGTH_SHORT).show();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Log.i("error", "onErrorResponse: ");
                Toast.makeText(mApplicationContext, "              Login Failed\nPlease check the required fields", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * List Overview Http Request
     * Pass the String response to Overview Fragment. Overview Fragment can then draw graphs based on the response.
     * @param callback
     * @param context
     */
    public void listOverview(final VolleyCallback callback,final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/limits";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                        // Display the first 500 characters of the response string.
                        //Toast.makeText(mApplicationContext, "Listing limits Succeed", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    /**
                     * Enable auto-login function
                     */
                    editor.putBoolean("isSignedOut", true);
                    editor.apply();
                    context.startActivity(i);
                    System.out.println("haha");
                }else{
                    Toast.makeText(mApplicationContext, "Listing limits Failed", Toast.LENGTH_SHORT).show();
                }


            }
        }) {
            /**
             * Set Token inside  the Http Request Header
             * @return
             * @throws AuthFailureError
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List Instance Http Request showing the servers detail
     * @param callback
     * @param context
     */
    public void listInstance(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/detail";
        System.out.println("hahaha");
        System.out.println(sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listInstance(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Listing Instances Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List flavor Http Request showing the available flavors
     * @param callback
     * @param context
     */

    public void listFlavor(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/flavors";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listFlavor(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting flavor Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List key pair Http Request showing the available key pairs
     * @param callback
     * @param context
     */
    public void listKeyPair(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        System.out.println(computeServiceURL);
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listKeyPair(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting key pairs Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     * @param callback
     * @param context
     * @param kpName
     */
    public void showKeyPairDetail(final VolleyCallback callback, final Context context, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs/" + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listkeypairDetail(response);
                        String result = resultObject.toString();
                        callback.onSuccess(result);


                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Getting the details of this key pair Failed", Toast.LENGTH_SHORT).show();
                callback.onSuccess("error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  Delete key pair
     * @param callback
     * @param kpName
     */
    public void deleteKeyPair(final VolleyCallback callback, String kpName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs/" + kpName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                                callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete Key pair failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List availability zone Http Request showing the availability zones
     * @param callback
     * @param context
     */
    public void listAvailabilityZone(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-availability-zone";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listAvabilityZone(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting avability zones Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List security group Http Request showing the available security groups
     * @param callback
     * @param context
     */
    public void listSecurityGroup(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        System.out.println(networkServiceURL);
        String fullURL = networkServiceURL + "/v2.0/security-groups";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listSecurityGroup(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting security groups Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  Delete security group
     * @param callback
     * @param sgID
     */
    public void deleteSecurityGroup(final VolleyCallback callback, String sgID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/" + sgID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    //Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List rules for the specific security group the user clicks in the AccessAndSecurityFragment listview
     * @param callback
     * @param context
     * @param sgId
     */
    public void listManageRuleSG(final VolleyCallback callback, final Context context, String sgId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/" + sgId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                            JSONArray resultArray;
                            resultArray = ResponseParser.getInstance(mApplicationContext).listRulesSG(response);
                            String result = resultArray.toString();
                            callback.onSuccess(result);


                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Getting Rules Failed", Toast.LENGTH_SHORT).show();
                callback.onSuccess("error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  Delete a rule
     * @param callback
     * @param ruleID
     */
    public void deleteRuleSG(final VolleyCallback callback, String ruleID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-group-rules/" + ruleID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     *  List alarm Http Request showing the created alarms of current project
     *  @param callback
     *  @param context
     */
    public void listAlarmProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL","Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String fullURL = alarmingServiceURL+"/v2/alarms";
        System.out.println("alarm_full: "+fullURL);

        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listAlarm(response);
                String result = resultArray.toString();
                //System.out.println("result:aaaaa");
                //System.out.println(result);
                callback.onSuccess(result);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                    // contect extend long time
                } else {
                    Toast.makeText(mApplicationContext, "Getting Alarms Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List image Http Request showing the available images of current project
     * @param callback
     * @param context
     */


    public void listImageProject(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");


        //String fullURL = imageServiceURL + "/v2/images?owner="+tenant;
        // api might be changed, the request is not response
        String fullURL = imageServiceURL+"/v2/images";

        System.out.println(fullURL);
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listImage(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List image Http Request showing the available images of NECTAR Official
     * @param callback
     * @param context
     */

    public void listImageOfficial(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String owner="28eadf5ad64b42a4929b2fb7df99275c";
        String fullURL = imageServiceURL + "/v2/images?owner="+owner;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listImageOfficial(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting images Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * Get the detailed info of a specific image
     * @param callback
     * @param context
     * @param id
     */

    public void showImageDetail(final VolleyCallback callback, final Context context, String id) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String imageServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String fullURL = imageServiceURL + "/v2/images/"+id;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listImageDetail(response);
                        String result = resultObject.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting image detail Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  Delete image
     * @param callback
     * @param imageID
     */
    public void deleteImage(final VolleyCallback callback, String imageID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2/images/" + imageID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete image failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /**
     * List Instance Detail for the specific instance the user clicks in the InstanceFragment listview
     * @param callback
     * @param context
     * @param instanceId
     */
    public void listSingleInstance(final VolleyCallback callback, final Context context, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /**
                         *  Pass the response of HTTP request to the Fragment
                         *  Server ID, AZ,IP address, Name, and Status
                         */
                        try {
                            JSONObject resp = new JSONObject(response);
                            JSONObject result = new JSONObject();
                            String id = resp.getJSONObject("server").getString("id");
                            String zone = resp.getJSONObject("server").getString("OS-EXT-AZ:availability_zone");
                            String address = resp.getJSONObject("server").getString("accessIPv4");
                            String name = resp.getJSONObject("server").getString("name");
                            String status = resp.getJSONObject("server").getString("status");
                            String created = resp.getJSONObject("server").getString("created");
                            String image = resp.getJSONObject("server").getJSONObject("image").getString("id");
                            String key = resp.getJSONObject("server").getString("key_name");
                            if(key.equals("null")){
                                key="None";
                            }
                            JSONArray sgArray=resp.getJSONObject("server").getJSONArray("security_groups");
                            String sg="";
                            if(sgArray.length()==0){
                                sg="None";
                            }else{
                                for(int i=0;i<sgArray.length();i++){
                                    JSONObject sgObject= (JSONObject) sgArray.get(i);
                                    if(i==0){
                                        sg=sgObject.getString("name");
                                    }else{
                                        sg=sg+", "+sgObject.getString("name");
                                    }
                                }
                            }

                            JSONArray vArray=resp.getJSONObject("server").getJSONArray("os-extended-volumes:volumes_attached");
                            int vNum=vArray.length();
                            for(int j=0;j<vNum;j++){
                                JSONObject vObject= (JSONObject) vArray.get(j);
                                String volume=vObject.getString("id");
                                result.put("volume"+j, volume);
                            }
                            result.put("id", id);
                            result.put("zone", zone);
                            result.put("address", address);
                            result.put("name", name);
                            result.put("status", status);
                            result.put("created", created);
                            result.put("image", image);
                            result.put("key", key);
                            result.put("securityg", sg);
                            result.put("volNum", vNum);
                            String stringResult = result.toString();
                            callback.onSuccess(stringResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Listing Instances Failed", Toast.LENGTH_SHORT).show();
                callback.onSuccess("error");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     * List available volume type
     * @param callback
     * @param context
     */
    public void listVolumeType(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/types";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listVolumeType(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Types Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * List Volume Snapshot Http Request
     * @param callback
     * @param context
     */
    public void listVolumeSnapshot(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/snapshots/detail";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listSnapshot(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Listing Snapshots Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /**
     * List Volume Http Request
     * @param callback
     * @param context
     */
    public void listVolume(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes/detail";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resultArray;
                        resultArray = ResponseParser.getInstance(mApplicationContext).listVolume(response);
                        String result = resultArray.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                }
                Toast.makeText(mApplicationContext, "Listing Volumes Failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }



    /**
     * Get the detailed info of a specific volume snapshot
     * @param callback
     * @param context
     * @param snapshotid
     */

    public void showVolumeSnapshotDetail(final VolleyCallback callback, final Context context, String snapshotid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/snapshots/"+snapshotid;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listVolumeSnapshotDetail(response);
                        String result = resultObject.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting snapshot detail Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Get the detailed info of a specific volume
     * @param callback
     * @param context
     * @param volumeid
     */

    public void showVolumeDetail(final VolleyCallback callback, final Context context, String volumeid) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes/"+volumeid;

        //System.out.println(sharedPreferences.getString("imageServiceURL", "Error Getting Compute URL"));
        //System.out.println(sharedPreferences.getString("tenantId", "Error Getting Compute URL"));
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject resultObject;
                        resultObject = ResponseParser.getInstance(mApplicationContext).listVolumeDetail(response);
                        String result = resultObject.toString();
                        callback.onSuccess(result);
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 401) {
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext, LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting image detail Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     * Server action: Pause Http Request
     * @param callback
     * @param instanceId
     */
    public void pause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("pause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /**
                 * Successful Response is null so we have to separate it from the real Errors
                 */
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                        }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                        }
                    }
                }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Server action: Unpause Http Request
     * @param callback
     * @param instanceId
     */
    public void unpause(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("unpause", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Server action: Stop Http Request
     * @param callback
     * @param instanceId
     */
    public void stop(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-stop", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Server action: Pause Start Request
     * @param callback
     * @param instanceId
     */
    public void start(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("os-start", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Server action: Suspend Http Request
     * @param callback
     * @param instanceId
     */
    public void suspend(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("suspend", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Server action: Resume Http Request
     * @param callback
     * @param instanceId
     */
    public void resume(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("resume", JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Server action: Reboot Http Request
     * @param callback
     * @param instanceId
     */
    public void reboot(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("type", "HARD");
            json1.put("reboot",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Server action: Delete Http Request
     * @param callback
     * @param instanceId
     */
    public void delete(final VolleyCallback callback, String instanceId) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json = new JSONObject();
        try {
            json.put("forceDelete",JSONObject.NULL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Server action: Snapshot Http Request
     * @param callback
     * @param instanceId
     * @param snapshotName
     */
    public void snapshot(final VolleyCallback callback, String instanceId, String snapshotName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceId + "/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();
        try {
            json3.put("meta_var", "meta_val");
            json2.put("metadata",json3);
            json2.put("name",snapshotName);
            json1.put("createImage",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {

                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Network Error", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Luanch a new server
     * @param callback
     * @param name
     * @param flavor
     * @param image
     * @param kp
     * @param az
     * @param sg
     */
    public void launchServer(final VolleyCallback callback, String name, String flavor, String image,String kp, String az, List<String> sg){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONArray sgArray=new JSONArray();;
        if(sg.size()!=0){
            for (int i=0; i<sg.size();i++){
                JSONObject sgChoose= new JSONObject();
                try{
                    sgChoose.put("name", sg.get(i));
                    sgArray.put(sgChoose);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("imageRef",image);
            json2.put("flavorRef",flavor);
            if(az!="Select Availability Zone please"){
                json2.put("availability_zone",az);
            }
            if(kp!="Select Key pair please"){
                json2.put("key_name",kp);
            }
            if(sg.size()!=0){
                json2.put("security_groups",sgArray);
            }
            json1.put("server",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create instance successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to create instance", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  create a new security group
     * @param callback
     * @param name
     * @param description
     */
    public void createSecurityGroup(final VolleyCallback callback, String name, String description){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("description",description);
            json1.put("security_group",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed2");
                    //Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  edit an existing security group
     * @param callback
     * @param sgid
     * @param name
     * @param description
     */
    public void editSecurityGroup(final VolleyCallback callback, String sgid,String name, String description){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-groups/"+sgid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("description",description);
            json1.put("security_group",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  import a key pair with a public key
     * @param callback
     * @param name
     * @param publicKey
     */
    public void importKeyPair(final VolleyCallback callback, String name, String publicKey){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("public_key",publicKey);
            json1.put("keypair",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Import successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to import", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Create a new key pair
     * @param callback
     * @param name
     */
    public void createKeyPair(final VolleyCallback callback, String name){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/os-keypairs";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json1.put("keypair",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    /**
     *  Luanch a new server
     * @param callback
     * @param sgID
     * @param protocol
     * @param dir
     * @param minPort
     * @param maxPort
     * @param cidr
     * @param ethertype
     */
    public void addNewRule(final VolleyCallback callback, String sgID, String protocol, String dir,String minPort, String maxPort,String cidr,String ethertype){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/v2.0/security-group-rules";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(fullURL);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("security_group_id",sgID);
            json2.put("protocol",protocol);
            json2.put("direction",dir);
            json2.put("port_range_min",minPort);
            json2.put("port_range_max",maxPort);
            json2.put("remote_ip_prefix",cidr);
            json2.put("ethertype",ethertype);

            json1.put("security_group_rule",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Add successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to add", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  attach a volume to a instance
     * @param callback
     * @param instanceID
     * @param mountpoint
     * @param volumeid
     */
    public void attachVolume(final VolleyCallback callback, String instanceID, String mountpoint,String volumeid){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + instanceID+"/os-volume_attachments";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("volumeId",volumeid);
            json2.put("device",mountpoint);
            json1.put("volumeAttachment",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Attach successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to attach", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  attach a volume to a instance
     * @param callback
     * @param attachID
     * @param serverid
     */
    public void detachVolume(final VolleyCallback callback, String attachID,String serverid){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String computeServiceURL = sharedPreferences.getString("computeServiceURL", "Error Getting Compute URL");
        String fullURL = computeServiceURL + "/servers/" + serverid+"/os-volume_attachments/"+attachID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        //final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Detach  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Detach failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  edit an existing volume
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void editVolume(final VolleyCallback callback, String name,String description, String volumeid){
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes/"+volumeid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("description",description);
            json1.put("volume",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Edit V successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to edit V", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  extend the size of a volume
     * @param callback
     * @param newSize
     * @param volumeid
     */
    public void extendVolume(final VolleyCallback callback, int newSize,String volumeid){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes/"+volumeid+"/action";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("new_size",newSize);
            json1.put("os-extend",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    //Toast.makeText(mApplicationContext, "Extend V successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to extend V", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Delete volume
     * @param callback
     * @param volumeID
     */
    public void deleteVolume(final VolleyCallback callback, String volumeID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes/"+volumeID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    /**
     *  create a snapshot based on an existing volume
     * @param callback
     * @param name
     * @param description
     * @param volumeid
     */
    public void createVolumeSnapshot(final VolleyCallback callback, String name,String description, String volumeid){
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/snapshots";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("volume_id",volumeid);
            json2.put("description",description);
            json1.put("snapshot",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "create snapshot successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to create snapshot", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Luanch a new server
     * @param callback
     * @param name
     * @param description
     * @param size
     * @param zone
     * @param type
     */
    public void createVolume(final VolleyCallback callback, String name, String description, int size,String zone, String type){
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/volumes";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println(token);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("size",size);
            json2.put("availability_zone",zone);
            json2.put("description",description);
            json2.put("name",name);
            json2.put("volume_type",type);
            JSONObject json3 = new JSONObject();
            json2.put("metadata",json3);
            json2.put("consistencygroup_id",null);

            json1.put("volume",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {

                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to Create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }


    /**
     *  Delete volume snapshot
     * @param callback
     * @param snapshotID
     */
    public void deleteVolumeSnapshot(final VolleyCallback callback, String snapshotID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/snapshots/"+snapshotID;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response){

                        callback.onSuccess("success");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete  successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }


    /**
     *  edit an existing volume snapshot
     * @param callback
     * @param name
     * @param description
     * @param volumeSnapshotid
     */
    public void editVolumeSnapshot(final VolleyCallback callback, String name,String description, String volumeSnapshotid){
        String tenant=sharedPreferences.getString("tenantId", "Error Getting Compute URL");
        String volumeV3ServiceURL = "https://cinder.rc.nectar.org.au:8776/v3/"+tenant;
        String fullURL = volumeV3ServiceURL +"/snapshots/"+volumeSnapshotid;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("name",name);
            json2.put("description",description);
            json1.put("snapshot",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(json1);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, fullURL, json1,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("dedededededededddededed");
                        callback.onSuccess("success");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    //System.out.println("dedededededededddededed");
                    Toast.makeText(mApplicationContext, "Edit VS successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else{
                    if (error.networkResponse.statusCode == 401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    }else {
                        Toast.makeText(mApplicationContext,"Fail to edit VS", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    public void createAlarm(final VolleyCallback callback, String name, String description, String type,
                            String metric, int threshold, String method, String operator, int granularity, String state,
                            String severity){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String alarmServiceURL = sharedPreferences.getString("alarmingServiceURL","Error Getting alarmServiceURL");
        String fullURL = alarmServiceURL+"/v2/alarms";
        final String token = sharedPreferences.getString("tokenId","Error Getting Token");
        System.out.println("alarm_token: "+token);
        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();

        try {
            json1.put("name",name);
            json1.put("description",description);
            json1.put("type",type);


            json2.put("metric",metric);
            json2.put("resource_id","INSTANCE_ID");
            json2.put("resource_type","instance");
            json2.put("threshold",threshold);
            json2.put("aggregation_method",method);
            json2.put("comparison_operator",operator);
            json2.put("granularity",granularity);
            json2.put("evaluation_periods",3);

            json1.put("gnocchi_resources_threshold_rule",json2);
            json1.put("state",state);
            json1.put("severity",severity);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("create alarm: "+json1);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL, json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println("dedededededededddedededaaaaaa");
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null) {

                    callback.onSuccess("success");

                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Fail to create", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                //headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        System.out.println("work here");
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);




    }

    public void deleteAlarm(final VolleyCallback callback, String alarmID){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String alarmingServiceURL = sharedPreferences.getString("alarmingServiceURL","Error Getting Compute URL");
        String fullURL = alarmingServiceURL + "/v2/alarms/"+alarmID;
        System.out.println(fullURL);
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse== null) {
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void listContainer(final VolleyCallback callback, final Context context) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URL");
        String fullURL = objectStorageServiceURL+"?format=json";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("container: "+fullURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listcontainer(response);
                String result = resultArray.toString();
                System.out.println("Con_Result: "+result);
                callback.onSuccess(result);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse== null) {
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "Delete failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void publicContainer(final VolleyCallback callback, String containerName){

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Public successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Public container failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read",".r:*");
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void privateContainer(final VolleyCallback callback, String containerName){

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Private successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Private container failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read","");
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void deleteContainer(final VolleyCallback callback, String containerName){

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete container failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void listObject(final VolleyCallback callback, final Context context, String containerName) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URL");
        String fullURL = objectStorageServiceURL+"/"+containerName+"?format=json";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("con_token: "+ token);
        System.out.println("container: "+fullURL);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listObject(response);
                String result = resultArray.toString();
                System.out.println("Con_Result: "+result);
                callback.onSuccess(result);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse== null) {
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);
                    } else {
                        Toast.makeText(mApplicationContext, "list failed", Toast.LENGTH_SHORT).show();
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteObject(final VolleyCallback callback,String containerName, String ObjectName){

        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName+"/"+ObjectName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("deleteFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);


    }

    public void createFolder(final VolleyCallback callback,String containerName, String ObjectName){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName+"/"+ObjectName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createContainer(final VolleyCallback callback, String containerName, String access){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL+"/"+containerName;
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        final String accessValue;
        if (access.equals("Private")){
            accessValue ="";
        } else {
            accessValue = ".r:*";
        }

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Container-Read",accessValue);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void createObjectFile(final VolleyCallback callback,String containerName, String ObjectName){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+containerName+"/"+ObjectName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void copyObject(final VolleyCallback callback,final String preContainer, final String preObjectName, String destionationContainer, String path, String newObjectName){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String objectStorageServiceURL = sharedPreferences.getString("objectStorageServiceURL","Error Getting Compute URl");
        String fullURL = objectStorageServiceURL + "/"+destionationContainer+"/"+path+newObjectName;
        final String token =sharedPreferences.getString("tokenId", "Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, fullURL, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "create object failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                headers.put("X-Copy-From",preContainer+"/"+preObjectName);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);

    }

    public void listFloatingIP(final VolleyCallback callback, final Context context){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId","Error Getting Compute URL");
        String fullURL = networkServiceURL +"v2.0/floatingips";

        final String token = sharedPreferences.getString("tokenId","Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listFloatingIP(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext,LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Floating IP Failed", Toast.LENGTH_SHORT).show();

                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteFloatingIP(final VolleyCallback callback, String floatingID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");
        System.out.println("floathaha: "+token);
        String fullURL = networkServiceURL + "v2.0/floatingips/" + floatingID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Release successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Release Floating IP failed", Toast.LENGTH_SHORT).show();
                        System.out.println("releaseFail");
                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createFloatingIP(final VolleyCallback callback, String floating_network_id){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/floatingips";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {
            json2.put("floating_network_id",floating_network_id);
            json1.put("floatingip",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL,json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Allocate successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Allocate Floating IP failed", Toast.LENGTH_SHORT).show();
                        System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;

            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);
    }

    public void listRouter(final VolleyCallback callback, final Context context){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId","Error Getting Compute URL");
        String fullURL = networkServiceURL +"v2.0/routers";

        final String token = sharedPreferences.getString("tokenId","Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listRouter(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext,LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Router  Failed", Toast.LENGTH_SHORT).show();

                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteRouter(final VolleyCallback callback, String routerID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/routers/" + routerID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete Router successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete  failed", Toast.LENGTH_SHORT).show();

                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createRouter(final VolleyCallback callback, String routerName, String networkID, boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/routers";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();
        JSONObject json3 = new JSONObject();

        try {
            json3.put("network_id",networkID);
            json2.put("external_gateway_info",json3);
            json2.put("name",routerName);
            json2.put("admin_state_up", admin_state);
            json1.put("router",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL,json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create Router successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Router failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;

            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void listNetwork(final VolleyCallback callback, final Context context){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId","Error Getting Compute URL");
        String fullURL = networkServiceURL +"v2.0/networks";

        final String token = sharedPreferences.getString("tokenId","Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listNetwork(response);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext,LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Network  Failed", Toast.LENGTH_SHORT).show();

                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void listSubnet(final VolleyCallback callback, final Context context, final String networkID){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android",0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String tenant = sharedPreferences.getString("tenantId","Error Getting Compute URL");
        String fullURL = networkServiceURL +"v2.0/subnets";

        final String token = sharedPreferences.getString("tokenId","Error Getting Token");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONArray resultArray;
                resultArray = ResponseParser.getInstance(mApplicationContext).listSubnet(response, networkID);
                System.out.println("networkID: "+ networkID);
                String result = resultArray.toString();
                callback.onSuccess(result);
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){
                    Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(mApplicationContext,LoginActivity.class);
                    context.startActivity(i);
                } else {
                    Toast.makeText(mApplicationContext, "Getting Subnet  Failed", Toast.LENGTH_SHORT).show();

                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String,String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void deleteNetwork(final VolleyCallback callback, String networkID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/networks/" + networkID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete Router failed", Toast.LENGTH_SHORT).show();

                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createNetwork(final VolleyCallback callback, String networkName,boolean admin_state) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/networks";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {

            json2.put("name",networkName);
            json2.put("admin_state_up", admin_state);
            json1.put("network",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL,json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create Network successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Network failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;

            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }

    public void deleteSubnet(final VolleyCallback callback, String SubnetID) {
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        String fullURL = networkServiceURL + "v2.0/subnets/" + SubnetID;

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, fullURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    Toast.makeText(mApplicationContext, "Delete Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if (error.networkResponse.statusCode == 401) {
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Delete  failed", Toast.LENGTH_SHORT).show();

                        callback.onSuccess("error");
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;
            }
        };

        NetworkController.getInstance(mApplicationContext).addToRequestQueue(stringRequest);
    }

    public void createSubnet( final VolleyCallback callback, String subnetName, String networkID, String networkAddress, int version){
        sharedPreferences = mApplicationContext.getSharedPreferences("nectar_android", 0);
        String networkServiceURL = sharedPreferences.getString("networkServiceURL", "Error Getting Compute URL");
        String fullURL = networkServiceURL + "v2.0/subnets";
        final String token = sharedPreferences.getString("tokenId", "Error Getting Token");

        JSONObject json1 = new JSONObject();
        JSONObject json2 = new JSONObject();


        try {

            json2.put("name",subnetName);
            json2.put("cidr", networkAddress);
            json2.put("ip_version", version);
            json2.put("network_id",networkID);
            json1.put("subnet",json2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, fullURL,json1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess("success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null ){
                    Toast.makeText(mApplicationContext, "Create Subnet successfully", Toast.LENGTH_SHORT).show();
                    callback.onSuccess("success");
                } else {
                    if(error.networkResponse.statusCode ==401){
                        Toast.makeText(mApplicationContext, "Expired token. Please login again", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(mApplicationContext, LoginActivity.class);
                        mApplicationContext.startActivity(i);

                    } else {
                        Toast.makeText(mApplicationContext, "Create Subnet failed", Toast.LENGTH_SHORT).show();
                        //System.out.println("createFail");
                        callback.onSuccess("error");
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", token);
                return headers;

            }
        };
        NetworkController.getInstance(mApplicationContext).addToRequestQueue(jsonObjectRequest);

    }


}
