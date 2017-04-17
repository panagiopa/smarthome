package com.example.android.smarthome;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.smarthome.onepv1.HttpRPCRequestException;
import com.example.android.smarthome.onepv1.HttpRPCResponseException;
import com.example.android.smarthome.onepv1.OnePlatformException;
import com.example.android.smarthome.onepv1.OnePlatformRPC;
import com.example.android.smarthome.onepv1.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private TextView mTxtConnected;
    private TextView mTxttemp;
    private TextView mtxtrele1state;
    private ToggleButton mtoggle;
    private ToggleButton mtogglelamp;

    static Device mDevice = new Device();

    Handler mReadHandler = new Handler();
    Runnable mReadRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoggle = (ToggleButton) findViewById(R.id.toggleButton);
        mtogglelamp = (ToggleButton) findViewById(R.id.toggleBtn_lamp);

        mTxtConnected = (TextView) findViewById(R.id.txtconnected);

        mTxttemp = (TextView) findViewById(R.id.txttemp);

        mtxtrele1state = (TextView) findViewById(R.id.txtrele1state);

        mtoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    mDevice.setWriteInProgress(true);
                    new WriteTask().execute(ALIAS_TOGGLECMD, "1");

                } else {
                    // The toggle is disabled
                    mDevice.setWriteInProgress(true);
                    new WriteTask().execute(ALIAS_TOGGLECMD, "1");

                }
            }
        });

        mtogglelamp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    mDevice.setWriteInProgress(true);
                    new WriteTask().execute(ALIAS_TOGGLECMD, "0x02");

                } else {
                    // The toggle is disabled
                    mDevice.setWriteInProgress(true);
                    new WriteTask().execute(ALIAS_TOGGLECMD, "0x02");

                }
            }
        });

        // configure to update widgets from platform periodically
        mReadRunnable = new Runnable() {
            @Override
            public void run() {
                new ReadTask().execute();
            }
        };

        // start worker thread for reading from OneP
        new ReadTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();
        if (menuItemThatWasSelected == R.id.action_search) {
            Context context = MainActivity.this;
            String message = "Search clicked";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }



    private final String ALIAS_CPU_TEMP = "jtemp";
    private final String ALIAS_RELE = "rele1state";
    private final String ALIAS_TOGGLECMD = "togglerelaycmd";
    // TI device CIK
    static String mCIK = "";

    void displayError() {
        // show a brief message if it hasn't already been shown
        String err = mDevice.getError();
            if (err.length() > 0) {
                Context context = MainActivity.this;
                Toast.makeText(context, err, Toast.LENGTH_LONG).show();
                mTxtConnected.setText(err);
            }
        }

    void update_graphs()
    {

        if(mDevice.getCPUTemperature()!=null) {
            mTxttemp.setText(mDevice.getCPUTemperature().toString());
        }
        if(mDevice.getmRele1status()!=null)
        {
            mtxtrele1state.setText(mDevice.getmRele1status().toString());
        }


    }
    class ReadTask extends AsyncTask<Void, Integer, ArrayList<Result>> {
        private static final String TAG = "ReadTask";
        private final String[] aliases = {ALIAS_CPU_TEMP,ALIAS_RELE,ALIAS_TOGGLECMD};
        private Exception exception;
        protected ArrayList<Result> doInBackground(Void... params) {
            exception = null;
            // call to OneP
            OnePlatformRPC rpc = new OnePlatformRPC();
            String responseBody = null;
            try {
                String requestBody = "{\"auth\":{\"cik\":\"" + mCIK
                        + "\"},\"calls\":[";
                for (String alias: aliases) {
                    requestBody += "{\"id\":\"" + alias + "\",\"procedure\":\"read\","
                            + "\"arguments\":[{\"alias\":\"" + alias + "\"},"
                            + "{\"limit\":1,\"sort\":\"desc\"}]}";
                    if (alias != aliases[aliases.length - 1]) {
                        requestBody += ',';
                    }
                }
                requestBody += "]}";
                Log.v(TAG, requestBody);
                // do this just to check for JSON parse errors on client side
                // while debugging. it can be removed for production.
                JSONObject jo = new JSONObject(requestBody);
                responseBody = rpc.callRPC(requestBody);

                Log.v(TAG, responseBody);

            } catch (JSONException e) {
                this.exception = e;
                Log.e(TAG, "Caught JSONException before sending request. Message:" + e.getMessage());
            } catch (HttpRPCRequestException e) {
                this.exception = e;
                Log.e(TAG, "Caught HttpRPCRequestException " + e.getMessage());
            } catch (HttpRPCResponseException e) {
                this.exception = e;
                Log.e(TAG, "Caught HttpRPCResponseException " + e.getMessage());
            }

            if (responseBody != null) {
                try {
                    ArrayList<Result> results = rpc.parseResponses(responseBody);
                    return results;
                } catch (OnePlatformException e) {
                    this.exception = e;
                    Log.e(TAG, "Caught OnePlatformException " + e.getMessage());
                } catch (JSONException e) {
                    this.exception = e;
                    Log.e(TAG, "Caught JSONException " + e.getMessage());
                }
            }
            return null;
        }

        // this is executed on UI thread when doInBackground
        // returns a result
        protected void onPostExecute(ArrayList<Result> results) {
            boolean hasError = false;
            if (results != null) {
                mTxtConnected.setText("CONNECTED");
                for(int i = 0; i < results.size(); i++) {
                    Result result = results.get(i);
                    String alias = aliases[i];

                    if (result.getResult() instanceof JSONArray) {
                        try {
                            JSONArray points = ((JSONArray)result.getResult());
                            if (points.length() > 0) {
                                JSONArray point = points.getJSONArray(0);
                                // this will break if results are out of order.
                                // need to fix OnePlatformRPC.java
                                if(alias.equals(ALIAS_CPU_TEMP))
                                {
                                    mDevice.setCPUTemperature(point.getDouble(1));
                                }
                                else if (alias.equals(ALIAS_RELE)) {
                                    mDevice.setmRele1status(point.getInt(1));

                                }
                                else if (alias.equals(ALIAS_TOGGLECMD)) {
                                    mDevice.setToggleReleCMD(point.getInt(1));


                                }
                            } else {
                                hasError = true;
                                if (alias.equals(ALIAS_CPU_TEMP)) {
                                    mDevice.setCPUTemperature(null);
                                    mDevice.setError("No ROOM temperature values.");
                                }
                                else if (alias.equals(ALIAS_RELE)) {
                                    mDevice.setmRele1status(null);
                                    mDevice.setError("No RELE IN value");
                                }
                                else if (alias.equals(ALIAS_TOGGLECMD)) {
                                    mDevice.setToggleReleCMD(null);
                                    mDevice.setError("No toggle cmd value");
                                }

                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException getting the result: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, result.getStatus() + ' ' + result.getResult().toString());
                    }
                }
               // updateWidgets();
                update_graphs();

            } else {
                Log.e(TAG, "null result in ReadTask.onPostExecute()");
                if (this.exception instanceof OnePlatformException) {
                    mDevice.setError("Received error from platform");
                } else {
                    mDevice.setError("Unable to connect to platform");
                }
                hasError = true;
            }
            if (!hasError) {
                mDevice.setError("");
            } else {
                displayError();
            }
            mReadHandler.postDelayed(mReadRunnable, 1000);
        }
    }

    class WriteTask extends AsyncTask<String, Integer, ArrayList<Result>> {
        private static final String TAG = "WriteTask";
        private Exception exception = null;
        // pass two values per alias to write -- alias followed by value to write
        // for example "foo", "1", "bar", "2"
        protected ArrayList<Result> doInBackground(String... values) {
            assert(values.length % 2 == 0);
            OnePlatformRPC rpc = new OnePlatformRPC();
            String responseBody = null;
            try {
                String requestBody = "{\"auth\":{\"cik\":\"" + mCIK
                        + "\"},\"calls\":[";
                for (int i = 0; i < values.length; i += 2) {
                    String alias = values[i];
                    requestBody += "{\"id\":\"" + alias + "\",\"procedure\":\"write\","
                            + "\"arguments\":[{\"alias\":\"" + alias + "\"},"
                            + "\"" + values[i + 1] + "\"]}";
                    // are we pointing to the last alias?
                    if (i != values.length - 2) {
                        requestBody += ',';
                    }
                }
                requestBody += "]}";
                Log.d(TAG, requestBody);
                // do this just to check for JSON parse errors on client side
                // while debugging. it can be removed for production.
                JSONObject jo = new JSONObject(requestBody);
                responseBody = rpc.callRPC(requestBody);

                Log.d(TAG, responseBody);
                mDevice.setError("");
            } catch (JSONException e) {
                this.exception = e;
                Log.e(TAG, "Caught JSONException before sending request. Message:" + e.getMessage());
                mDevice.setError("Unable to connect to platform");
            } catch (HttpRPCRequestException e) {
                this.exception = e;
                Log.e(TAG, "Caught HttpRPCRequestException " + e.getMessage());
                mDevice.setError("Unable to connect to platform");
            } catch (HttpRPCResponseException e) {
                this.exception = e;
                Log.e(TAG, "Caught HttpRPCResponseException " + e.getMessage());
                mDevice.setError("Unable to connect to platform");
            }

            if (responseBody != null) {
                try {
                    ArrayList<Result> results = rpc.parseResponses(responseBody);
                    return results;
                } catch (OnePlatformException e) {
                    this.exception = e;
                    Log.e(TAG, "Caught OnePlatformException " + e.getMessage());
                } catch (JSONException e) {
                    this.exception = e;
                    Log.e(TAG, "Caught JSONException " + e.getMessage());
                }
            }
            return null;
        }

        // this is executed on UI thread when doInBackground
        // returns a result
        protected void onPostExecute(ArrayList<Result> results) {
            mDevice.setWriteInProgress(false);
            displayError();
        }
    }



}
