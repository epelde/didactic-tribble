package io.github.epelde.didactictribble.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.github.epelde.didactictribble.Ticket;
import io.github.epelde.didactictribble.fragment.TicketFragment;

/**
 * Created by epelde on 30/12/2015.
 */
public class TicketActivity extends SingleFragmentActivity implements TicketFragment.PrintTicketListener {

    private static final String LOG_TAG = TicketActivity.class.getSimpleName();

    private Ticket ticket;
    private static BluetoothSocket socket;
    private byte FONT_TYPE;

    @Override
    protected void onPause() {
        super.onPause();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }
        }
    }

    @Override
    public Fragment createFragment() {
        return new TicketFragment();
    }

    @Override
    public void printTicket(Ticket t) {
        //Log.i(LOG_TAG, "* * * Ticket:" + t.getCode());
        //ticket = t;
        if (socket == null) {
            startActivityForResult(new Intent(this, BTDeviceListActivity.class), BTDeviceListActivity.SELECT_DEVICE);
            //this.startActivityForResult(new Intent(getApplicationContext(), BTDeviceList.class), BTDeviceList.REQUEST_CONNECT_BT);
        }
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //btsocket = BTDeviceList.getSocket();
                    Log.i(">>>", "Printing");
                    BluetoothDevice device = (BluetoothDevice) data.getParcelableExtra("DEVICE");
                    UUID uuid = device.getUuids()[0].getUuid();
                    Log.i(">>>", "uuid:" + uuid.toString());
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                    socket.connect();
                    if(socket != null){
                        print_bt();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        connectThread.start();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Thread connectingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    }
                    BluetoothDevice device = (BluetoothDevice) data.getParcelableExtra(BTDeviceListActivity.SELECTED_DEVICE);
                    Log.i(LOG_TAG, "* * * Selected Device Name: " + device.getName());
                    Log.i(LOG_TAG, "* * * " + device.getUuids().length);
                    UUID uuid = device.getUuids()[0].getUuid();
                    Log.i(LOG_TAG, "* * * 3");
                    Log.i(LOG_TAG, "* * * Connecting to uuid:" + uuid.toString());
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                    Log.i(LOG_TAG, "* * * Calling to connect!");
                    socket.connect();
                    if(socket != null){
                        print_bt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        if (requestCode == BTDeviceListActivity.SELECT_DEVICE && resultCode == Activity.RESULT_OK) {
            connectingThread.start();
        }
    }


    private void print_bt() {
        Thread printingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OutputStream btoutputstream = socket.getOutputStream();
                    byte[] printformat = { 0x1B, 0x21, FONT_TYPE };
                    btoutputstream.write(printformat);
                    String msg = ticket.getDescription();
                    btoutputstream.write(msg.getBytes());
                    btoutputstream.write(0x0D);
                    btoutputstream.write(0x0D);
                    btoutputstream.write(0x0D);
                    btoutputstream.flush();
                    btoutputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        printingThread.start();
    }

}
