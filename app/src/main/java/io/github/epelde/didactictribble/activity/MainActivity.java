package io.github.epelde.didactictribble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

import io.github.epelde.didactictribble.R;

/**
 * Created by epelde on 08/02/2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private static final int REQUEST_CODE_ENABLE_BT = 2;

    private BluetoothService service = null;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new BluetoothService(this, mHandler);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Button printBtn = (Button) findViewById(R.id.print_button);
        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(v.getContext(), PrintTicketActivity.class));
                if (service.isDiscovering())
                    service.cancelDiscovery();
                if (service.isAvailable() == false) {
                    Toast.makeText(MainActivity.this, R.string.toast_msg_bluetooth_not_available,
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (service.isBTopen() == false) {
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                                REQUEST_CODE_ENABLE_BT);
                    } else {
                        setConnection();
                    }
                }

            }
        });
        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BarcodeScannerActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putString(getString(R.string.KEY_PRINTER_ADRESS), address);
                    editor.commit();
                    Toast.makeText(MainActivity.this, R.string.toast_msg_printer_configured, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.toast_msg_must_enable_bluetooth,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    setConnection();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_conf_printer:
                startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class),
                        REQUEST_CODE_SELECT_DEVICE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setConnection() {
        String address = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.KEY_PRINTER_ADRESS), null);
        if (address == null) {
            Toast.makeText(this, R.string.toast_msg_printer_no_configured, Toast.LENGTH_SHORT).show();
        } else {
            BluetoothDevice device  = service.getDevByMac(address);
            service.connect(device);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //connected = Boolean.TRUE;
                            //progress.setVisibility(View.VISIBLE);
                            //new GenerateTicketTask().execute();
                            Toast.makeText(MainActivity.this, "DEVICE CONNECTED", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    //progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.toast_msg_connection_lost,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    //progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.toast_msg_unable_connect_device,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

}
