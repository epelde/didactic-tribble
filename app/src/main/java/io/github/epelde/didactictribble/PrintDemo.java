package io.github.epelde.didactictribble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintDemo extends Activity {

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    Button btnConnect;
    Button btnPrint;
    BluetoothService mService;

    private static final String LOG_TAG = PrintDemo.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mService = new BluetoothService(this, mHandler);
        if (mService.isAvailable() == false) {
            Toast.makeText(this, R.string.msg_bluetooth_not_available, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mService.isBTopen() == false) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        btnConnect = (Button) this.findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new ClickEvent());
        btnPrint = (Button) this.findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new ClickEvent());
        btnPrint.setEnabled(false);
    }

/*    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null) {
            mService.stop();
            mService = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService == null) {
            mService = new BluetoothService(this, mHandler);
            if (mService.isAvailable() == false) {
                Toast.makeText(this, R.string.msg_bluetooth_not_available, Toast.LENGTH_LONG).show();
            }
        }
    }*/

    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == btnConnect) {
                Intent serverIntent = new Intent(PrintDemo.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else if (v == btnPrint) {
                printImage();
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), R.string.msg_connected,
                                    Toast.LENGTH_SHORT).show();
                            btnPrint.setEnabled(true);
                            btnConnect.setEnabled(false);
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), R.string.msg_connection_lost,
                            Toast.LENGTH_SHORT).show();
                    btnPrint.setEnabled(false);
                    btnConnect.setEnabled(true);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), R.string.msg_unable_connect,
                            Toast.LENGTH_SHORT).show();
                    btnPrint.setEnabled(false);
                    btnConnect.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.msg_bluetooth_enabled, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    if (mService == null) Log.i(LOG_TAG, "mService NULO");
                    else Log.i(LOG_TAG, "mService available:" + mService.isAvailable());
                    BluetoothDevice device = mService.getDevByMac(address);
                    mService.connect(device);
                }
                break;
        }
    }

    private void printImage() {
        new ImageFetcherTask().execute();
    }

    private class ImageFetcherTask extends AsyncTask<Void, Void, byte[]> {

        @Override
        protected byte[] doInBackground(Void... params) {
            byte[] imgBytes = null;
            try {
                String f = "https://chart.googleapis.com/chart?chs=320x320&cht=qr&chl=ES48001001000320160128133818";
                imgBytes = new ImageFetcher().getUrlBytes(f);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error generando ticket dinámicamente");
            }
            return imgBytes;
        }

        @Override
        protected void onPostExecute(byte[] result) {
            super.onPostExecute(result);
            try {
                File tempFile = File.createTempFile("temp", ".png");
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(result);
                fos.flush();
                if (tempFile.exists()) {
                   /* PrintPic pg = new PrintPic();
                    pg.initCanvas(384);
                    pg.initPaint();
                    pg.drawImage(0, 0, tempFile.getAbsolutePath());
                    mService.write(pg.printDraw());
                    fos.close();
                    tempFile.delete();*/

                    mService.write(Commands.PRINT_ALIGNMENT_CENTER);
                    mService.write(Commands.LEFT_MARGIN);
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    //mService.write(Commands.LF);
                    mService.sendMessage(df.format(new Date(System.currentTimeMillis())), "GBK");
                    mService.sendMessage("Nombre del comercio", "GBK");
                    mService.sendMessage("Dirección del comercio", "GBK");
                    mService.write(Commands.BOLD_FONT_ON);
                    mService.sendMessage("DESCRIPCIÓN DE LA OFERTA", "GBK");
                    mService.write(Commands.BOLD_FONT_OFF);
                    mService.sendMessage("ES48004001000220160202143642", "GBK");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Fichero temporal no encontrado");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Erro accediendo al fichero temporal");
            }
        }
    }
}
