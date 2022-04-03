package com.example.signalapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends AppCompatActivity {
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;
    int mSignalStrength = 0;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo Info = cm.getActiveNetworkInfo();
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (Info == null || !Info.isConnectedOrConnecting()) {
            Log.i("connection", "No connection");
        } else {
            int netType = Info.getType();
            int netSubtype = Info.getSubtype();

            if (netType == ConnectivityManager.TYPE_WIFI) {
                Log.i("connection", "Wifi connection");

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


                int rssi = wifiManager.getConnectionInfo().getRssi();
                Log.d("wifisignal", "" + rssi + " dbm");
                int level = WifiManager.calculateSignalLevel(rssi, 5);
                System.out.println("Level is " + level + " out of 5");


                // Need to get wifi strength
            } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                Log.i("connection", "Mobile data network connection");
                mPhoneStatelistener = new MyPhoneStateListener();

                mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

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

            if (signalSupport > 30) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Good");


            } else if (signalSupport > 20 && signalSupport < 30) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Avarage");


            } else if (signalSupport < 20 && signalSupport > 3) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Weak");


            } else if (signalSupport < 3) {
                Log.d(getClass().getCanonicalName(), "Signal GSM : Very weak");

            }
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
}