package com.example.bottomnavigation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private TextView textViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.wifi_on:
                        // Android 10-től (API 29) az alkalmazások nem kapcsolgathatják a wifit.
                        // Éppen ezért meg kell vizsgálnunk a telepített Android verzióját.
                        // Ha ez újabb akkor mást kell csinálnunk.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            textViewInfo.setText("Nincs jogosultság a wifi állapot módosítására");
                            // Megnyitunk 1 beállítási panelt
                            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                            // Panel bezárásakor szerentnénk valamit csinálni
                            startActivityForResult(panelIntent, 0);
                        } else {
                            // Szükséges engedély: CHANGE_WIFI_STATE
                            wifiManager.setWifiEnabled(true);
                            textViewInfo.setText("Wifi bekapcsolva");
                        }
                        break;
                    case R.id.wifi_off:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            textViewInfo.setText("Nincs jogosultság a wifi állapot módosítására");
                            // Másik panelen is megtalálható a wifi kapcsolásához szükséges gomb.
                            Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                            startActivityForResult(panelIntent, 0);
                        } else {
                            wifiManager.setWifiEnabled(false);
                            textViewInfo.setText("Wifi kikapcsolva");
                        }
                        break;
                    case R.id.wifi_info:
                        ConnectivityManager conManager =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        // Szükséges engedély: ACCESS_NETWORK_STATE
                        NetworkInfo netInfo = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (netInfo.isConnected()) {
                            int ip_number = wifiInfo.getIpAddress();
                            String ip = Formatter.formatIpAddress(ip_number);
                            textViewInfo.setText("IP: " + ip);
                        } else {
                            textViewInfo.setText("Nem csatlakoztál wifi hálózatra");
                        }
                        break;
                }
                return true;
            }
        });
    }

    // Akkor fog meghívódni amikor bezárjuk a megnyitott panelt.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // A requestCode az az érték amit mi adunk paraméterül startActivityForResult függvénynek.
        if (requestCode == 0) {
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
                    || wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING
            ) {
                textViewInfo.setText("Wifi bekapcsolva");
            } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED
                    || wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING
            ) {
                textViewInfo.setText("Wifi kikapcsolva");
            }
        }
    }

    private void init() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        textViewInfo = findViewById(R.id.textViewInfo);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // Szükséges engedély: ACCESS_WIFI_STATE
        wifiInfo = wifiManager.getConnectionInfo();
    }
}