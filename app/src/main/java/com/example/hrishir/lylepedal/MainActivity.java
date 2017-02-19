package com.example.hrishir.lylepedal;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button recalibrate;
    Switch toggle;
    CheckBox autocaliberate,inversion;
    ImageButton power;
    TextView angletext;
    SeekBar maxIncline, smoother, delayBar;
    int pro, arm_flag;
    int seekBarVal;
    String TAG = this.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiManager wifi= (WifiManager) getSystemService(Context.WIFI_SERVICE);
        arm_flag = 1;
        try {
            callHttp("arm","1");
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
        if(!checkWifi(wifi,"My Jio")) {
            Toast.makeText(this,"Failed To Connect to pedalAP.",Toast.LENGTH_SHORT).show();
        }
        recalibrate = (Button) findViewById(R.id.recaliberate_button);
        autocaliberate = (CheckBox) findViewById(R.id.autocaliberate_checkBox);
        angletext = (TextView) findViewById(R.id.smoothing);
        maxIncline = (SeekBar) findViewById(R.id.seekBar3);
        smoother = (SeekBar) findViewById(R.id.seekBar7);
        toggle=(Switch)findViewById(R.id.discrete_choice);
        delayBar = (SeekBar) findViewById(R.id.seekBar5);
        power = (ImageButton) findViewById(R.id.button2);
        inversion=(CheckBox)findViewById(R.id.inversion_checkBox);
        toggle.setChecked(false);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try {
                        callHttp("pMode","2");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                if(!isChecked){
                    try {
                        callHttp("pMode","1");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        inversion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try {
                        callHttp("einvert","1");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                if(!isChecked){
                    try {
                        callHttp("einvert","0");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        autocaliberate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    recalibrate.setVisibility(View.VISIBLE);
                    angletext.setVisibility(View.GONE);
                    maxIncline.setVisibility(View.GONE);
                    try {
                        String content=callHttp("autoCalib","1");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    recalibrate.setVisibility(View.GONE);
                    angletext.setVisibility(View.VISIBLE);
                    maxIncline.setVisibility(View.VISIBLE);
                    try {
                        String content=callHttp("autoCalib","0");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        recalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String content = callHttp("recal", "1");
                } catch (UnsupportedEncodingException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arm_flag == 1) {
                    power.setBackgroundResource(R.mipmap.off);
                    arm_flag = 0;
                    String content = null;
                    try {
                        content = callHttp("arm", "0");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "ARM is 0 : ");
                } else {
                    power.setBackgroundResource(R.mipmap.on);
                    arm_flag = 1;
                    String content = null;
                    try {
                        content = callHttp("arm", "1");
                    } catch (UnsupportedEncodingException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "ARM is 1: ");
                }
            }
        });

        maxIncline.setOnSeekBarChangeListener(mySeekBarHandler(maxIncline, "maxIncline"));
        smoother.setOnSeekBarChangeListener(mySeekBarHandler(smoother, "smoothening"));
        delayBar.setOnSeekBarChangeListener(mySeekBarHandler(delayBar, "delay"));
    }

    private boolean checkWifi(WifiManager wifi,String ssid) {
        boolean flag=true;
        if(!wifi.setWifiEnabled(true))
            return false;
        List<ScanResult> scanResults=wifi.getScanResults();
        Log.d(TAG,"Scan Results: "+scanResults);
        for(ScanResult scanResult: scanResults)
            if(scanResult.SSID.equals("\"pedalAP\"")){
                Toast.makeText(this,"Not Found", Toast.LENGTH_SHORT).show();
                return false;
            }
        WifiConfiguration wc=new WifiConfiguration();
        wc.SSID="\"pedalAP\"";
        wc.preSharedKey="\"password\"";
        wc.status=WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
// connect to and enable the connection
        int netId = wifi.addNetwork(wc);
        wifi.enableNetwork(netId, true);
        return flag;
    }

    private SeekBar.OnSeekBarChangeListener mySeekBarHandler(SeekBar seekBar, final String name) {
        SeekBar.OnSeekBarChangeListener seekBarHandler = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarVal = pro;
                String url;
                Log.d(TAG, name + " is : " + seekBarVal);
                try {
                    if (seekBarVal < 10) {
                        url = callHttp(name, String.format("%02d", seekBarVal));
                    } else {
                        url = callHttp(name, String.format("%02d", seekBarVal));
                    }
                    Log.d(TAG, "URL is:" + url);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        return seekBarHandler;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_load_default) {
            try {
                String content = callHttp("default", "1");
            } catch (UnsupportedEncodingException | MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.action_reboot) {
            try {
                String content = callHttp("reboot", "1");
            } catch (UnsupportedEncodingException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if(id==R.id.action_save){
            try {
                String content=callHttp("save","1");
            } catch (UnsupportedEncodingException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if(id==R.id.action_update){
            try {
                String content=callHttp("upgrade","1");
            } catch (UnsupportedEncodingException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else if(id==R.id.action_exit){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public String callHttp(String var, String value) throws UnsupportedEncodingException, MalformedURLException {
        InputStream ins = null;
        StringBuilder result = new StringBuilder();
        result.append(URLEncoder.encode(var, "UTF-8"));
        result.append("/");
        result.append(URLEncoder.encode(value, "UTF-8"));
        result.append("/");
        AsyncTask<URL, String, String> asyncTask = new AsyncTask<URL, String, String>() {
            @Override
            protected String doInBackground(URL... params) {
                HttpURLConnection connection = null;
                InputStream is = null;
                try {

                    URL url = params[0];
                    Log.d(TAG, "Going here: " + url);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                    connection.getInputStream();
                    String content=readIt(is,2);
                    Log.d(TAG,"Content is: "+content);
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
            }
        };

        URL url = new URL(String.format("http://192.168.4.1/val/%s", result.toString()));
        asyncTask.execute(url);
        return url.toString();
    }

    private String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}
