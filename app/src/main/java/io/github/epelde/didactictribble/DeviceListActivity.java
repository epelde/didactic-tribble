package io.github.epelde.didactictribble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

import java.util.Set;

public class DeviceListActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_BT = 1;
    public static final String EXTRA_DEVICE_ADDRESS = "didactictribble.device_address";
    private static final String LOG_TAG = DeviceListActivity.class.getSimpleName();

    private BluetoothService service = null;
    private ArrayAdapter<String> devicesArrayAdapter;
    private Button scanButton;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED); // Set result CANCELED in case the user backs out

        // Register for broadcasts when a device is discovered
        this.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // Register for broadcasts when discovery has finished
        this.registerReceiver(receiver,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        devicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView devicesListView = (ListView) findViewById(R.id.devices_list_view);
        devicesListView.setAdapter(devicesArrayAdapter);
        devicesListView.setOnItemClickListener(deviceClickListener);
        progress = (ProgressBar) findViewById(R.id.progress);

        service = new BluetoothService(this, null);
        if (service.isAvailable() == false) {
            Toast.makeText(this, R.string.toast_msg_bluetooth_not_available, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (service.isBTopen() == false) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_CODE_ENABLE_BT);
            } else {
                loadPairedDevices();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.cancelDiscovery();
        }
        service = null;
        this.unregisterReceiver(receiver);
    }

    private void doDiscovery() {
        if (service.isBTopen()) {
            scanButton.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            // If we're already discovering, stop it
            if (service.isDiscovering()) {
                service.cancelDiscovery();
            }
            // Request discover from BluetoothAdapter
            service.startDiscovery();
        } else {
            Toast.makeText(this, R.string.toast_msg_must_enable_bluetooth,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = service.getPairedDev();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, R.string.toast_msg_must_enable_bluetooth,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    loadPairedDevices();
                }
                break;
        }
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener deviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we have done selection!!!
            service.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String item = device.getName() + "\n" + device.getAddress();
                // If it's already paired or already listed, skip it!!!
                if (device.getBondState() != BluetoothDevice.BOND_BONDED &&
                        devicesArrayAdapter.getPosition(item) == -1) {
                    devicesArrayAdapter.add(item);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progress.setVisibility(View.GONE);
                scanButton.setVisibility(View.VISIBLE);
                if (devicesArrayAdapter.getCount() == 0) {
                    if(devicesArrayAdapter.isEmpty()) {
                        devicesArrayAdapter.add(getResources().getText(R.string.no_devices_found).toString());
                    }
                }
            }
        }
    };

}