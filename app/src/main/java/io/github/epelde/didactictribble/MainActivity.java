package io.github.epelde.didactictribble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 29/12/2015.
 */
public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    private Button mGenerateTicketButton;
    private BluetoothAdapter bluetoohAdapter;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();
        mGenerateTicketButton = (Button) findViewById(R.id.generate_ticket_button);
        mGenerateTicketButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //new GenerateTicketTask().execute();
                checkBluetoohEnabled();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                scanForDevices();
            } else {
                Toast.makeText(this, getString(R.string.msg_bluetooh_not_enabled),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBluetoohEnabled() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            bluetoohAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();
            if (bluetoohAdapter != null) {
                if (!bluetoohAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    scanForDevices();
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_bluetooh_adapter_available),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.msg_bluetooh_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void scanForDevices() {
        Toast.makeText(this, "Buscando impresora...", Toast.LENGTH_LONG).show();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final BluetoothLeScanner scanner = bluetoohAdapter.getBluetoothLeScanner();
            Log.i("XXX", "1");
            final ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i("XXX", "callbackType:" + String.valueOf(callbackType));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.i("XXX", "RESULT:" + result.toString());
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    Log.i("XXX", "RESULTS!!!!");
                    for (ScanResult sr : results) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Log.i("XXX", "ScanResult - Results" + sr.toString());
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.e("XXX", "Scan failed");
                }
            };

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("XXX", "STOPPING");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        scanner.stopScan(scanCallback);
                    }
                }
            }, 10000);
            Log.i("XXX", "2");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("XXX", "3");
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                List<ScanFilter> filters = new ArrayList<ScanFilter>();
                scanner.startScan(filters, settings, scanCallback);
            }
        } else {
            // TODO Provide scanning method for older versions
        }
    }


    private void printTickets() {
        Toast.makeText(this, "Listo para imprimir tickets", Toast.LENGTH_LONG).show();
    }


    private void displayTicket(Ticket ticket) {
        Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra("TICKET", ticket);
        startActivity(intent);
    }

    private class GenerateTicketTask extends AsyncTask<Void, Void, Ticket> {
        @Override
        protected Ticket doInBackground(Void... params) {
            Service client = GenerateTicketClient.createService(Service.class);
            Call<GenerateTicketResponse> call = client.generateTicket();
            Response<GenerateTicketResponse> apiResponse = null;
            try {
                apiResponse = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return apiResponse.body().getTickets().get(0);
        }

        @Override
        protected void onPostExecute(Ticket t) {
            super.onPostExecute(t);
            displayTicket(t);
        }
    }
}
