package io.github.epelde.didactictribble.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.epelde.didactictribble.R;
import io.github.epelde.didactictribble.activity.BTDeviceListActivity;

/**
 * Created by epelde on 19/01/2016.
 */
public class BTDeviceListFragment extends ListFragment {

    private static final String LOG_TAG = BTDeviceListFragment.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter btAdapter;
    private static ArrayAdapter<String> arrayAdapter;
    private static List<BluetoothDevice> devices;
    private SelectionListener listener;

    public interface SelectionListener {
        public void selected();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkBluetooth();
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        setListAdapter(arrayAdapter);
        getActivity().registerReceiver(btFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionListener) {
            listener = (SelectionListener) context;
        }
    }

    private boolean checkBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getActivity(), R.string.msg_bluetooh_not_supported, Toast.LENGTH_LONG).show();
            return Boolean.FALSE;
        }

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        Log.i(LOG_TAG, "* * * BOUNDED DEVICES:" + btAdapter.getBondedDevices().size());

        if (!btAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(getActivity(), R.string.msg_searching_bluetooth_devices, Toast.LENGTH_SHORT).show();
            btAdapter.startDiscovery();
        }

        return Boolean.TRUE;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getActivity(), R.string.msg_searching_bluetooth_devices, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), R.string.msg_bluetooh_not_enabled, Toast.LENGTH_LONG).show();
                }
                break;
        }
        btAdapter.startDiscovery();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra(BTDeviceListActivity.SELECTED_DEVICE, devices.get(position));
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().unregisterReceiver(btFoundReceiver);
        //getActivity().finish();
        listener.selected();
    }

    private final BroadcastReceiver btFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            if (devices == null) {
                devices = new ArrayList<>();
            }
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //Log.i(LOG_TAG, "* * * BroadcastReceiver UUID=" + device.getUuids()[0].getUuid().toString());
            devices.add(device);
            arrayAdapter.add(device.getName() + "\n" + device.getAddress());
            arrayAdapter.notifyDataSetInvalidated();
        }
        }
    };
}
