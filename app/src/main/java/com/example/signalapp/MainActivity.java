package com.example.signalapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;
    Button wifibtn;
    Button databtn;
    NetworkInfo Info;

    CheckBox strongcb;
    CheckBox goodcb;
    CheckBox faircb;
    CheckBox weakcb;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        wifibtn =  findViewById(R.id.wifibtn);
        databtn =  findViewById(R.id.databtn);
        strongcb =  findViewById(R.id.strongcb);
        goodcb =  findViewById(R.id.goodcb);
        faircb =  findViewById(R.id.faircb);
        weakcb =  findViewById(R.id.weakcb);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        checkNetwork();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Info = cm.getActiveNetworkInfo();
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        checkNetwork();

    }

    public void checkNetwork(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Info = cm.getActiveNetworkInfo();
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (Info == null || !Info.isConnectedOrConnecting()) {
            Log.i("connection", "No connection");
        } else {
            int netType = Info.getType();
            int netSubtype = Info.getSubtype();

            if (netType == ConnectivityManager.TYPE_WIFI) {
                Log.i("connection", "Wifi connection");
                Toast.makeText(this, "ON WIFI NETWORK", Toast.LENGTH_SHORT).show();
                wifibtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_button_red));
                databtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_button_grey));

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


                int rssi = wifiManager.getConnectionInfo().getRssi();
                Log.d("wifisignal", "" + rssi + " dbm");
                setCheckBox(rssi);
                Toast.makeText(this, "wifi signal strength in dbm "+rssi, Toast.LENGTH_SHORT).show();
                int level = WifiManager.calculateSignalLevel(rssi, 5);
                System.out.println("Level is " + level + " out of 5");


                // Need to get wifi strength
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                Log.i("connection", "Mobile data network connection");
                Toast.makeText(this, "ON Mobile Data", Toast.LENGTH_SHORT).show();

                wifibtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_button_grey));
                databtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_button_red));
                mPhoneStatelistener = new MyPhoneStateListener();

//                mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
                } else {
                    List<CellSignalStrength> cellInfoList;
                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cellInfoList = tm.getSignalStrength().getCellSignalStrengths();
                        for (CellSignalStrength cellInfo : cellInfoList) {
                            if (cellInfo instanceof CellSignalStrengthLte) {
                                int rsrp = ((CellSignalStrengthLte) cellInfo).getRsrp();
                                int rsrq = ((CellSignalStrengthLte) cellInfo).getRsrq();
                                int snr = ((CellSignalStrengthLte) cellInfo).getRssnr();
                                int rssi =  ((CellSignalStrengthLte) cellInfo).getRssi();
                                System.out.println("rsrq "+rsrq+" rsrp "+rsrp+" snr "+snr+" rssi "+rssi);
                                Toast.makeText(this, "mobile data signal strength in dbm "+rssi, Toast.LENGTH_SHORT).show();
                                setCheckBox(rssi);
                            }
                        }
                    }



                }



            }
        }
    }

    public class MyPhoneStateListener extends PhoneStateListener {
        public int signalSupport;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            signalSupport = signalStrength.getGsmSignalStrength();
            Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + signalSupport);

//            setCheckBox(signalSupport);
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 100;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    return;
                } else {
                    Log.d("mobiledata3",""+mTelephonyManager.getAllCellInfo());
                }
        }
    }


    public void setCheckBox(int rssi){

        /*
        * Excellent >-50 dBm

        Good -50 to -60 dBm

        Fair -60 to -70 dBm

        Weak < -70 dBm
        * */

        if (rssi > -50) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Strong");
            strongcb.setChecked(true);
            goodcb.setChecked(false);
            faircb.setChecked(false);
            weakcb.setChecked(false);
            strongcb.setHintTextColor(Color.RED);
        } else if (rssi >= -60 && rssi < -50) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Good");
            strongcb.setChecked(false);
            goodcb.setChecked(true);
            faircb.setChecked(false);
            weakcb.setChecked(false);
            goodcb.setHintTextColor(Color.RED);

        } else if (rssi < -60 && rssi >= -70) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Weak");
            strongcb.setChecked(false);
            goodcb.setChecked(false);
            faircb.setChecked(true);
            weakcb.setChecked(false);
            faircb.setHintTextColor(Color.RED);

        } else if (rssi <= -70) {
            Log.d(getClass().getCanonicalName(), "Signal GSM : Very weak");
            strongcb.setChecked(false);
            goodcb.setChecked(false);
            faircb.setChecked(false);
            weakcb.setChecked(true);
        }
    }
}