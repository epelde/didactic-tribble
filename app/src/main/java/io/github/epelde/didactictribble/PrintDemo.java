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
import com.zj.btsdk.PrintPic;

import org.json.JSONException;
import org.json.JSONObject;

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
                    BluetoothDevice device = mService.getDevByMac(address);
                    mService.connect(device);
                }
                break;
        }
    }

    private void printImage() {
        new ImageFetcherTask().execute();
    }

    private class ImageFetcherTask extends AsyncTask<Void, Void, Ticket> {

        @Override
        protected Ticket doInBackground(Void... params) {
            Ticket ticket = null;
            try {
                String json = ImageFetcher.getUrlString("http://www.kobazulo.net/clientes/fidelizacion/generar_ticket.asp?Pais=ES&Codigo=480040010001&Clave=5714&Idioma=ES");
                JSONObject jsonBody = new JSONObject(json);
                JSONObject jsonTicket = jsonBody.getJSONArray("datos").getJSONObject(0);
                ticket = new Ticket();
                ticket.setDate(jsonTicket.getString("FechaHora"));
                ticket.setName(jsonTicket.getString("NombreComercio"));
                ticket.setAddress(jsonTicket.getString("DireccionComercio"));
                ticket.setDescription(jsonTicket.getString("DescripcionOferta"));
                ticket.setCodeUrl(jsonTicket.getString("UrlQr"));
                ticket.setCode(jsonTicket.getString("CodigoTicket"));
                byte[] imageByte = ImageFetcher.getUrlBytes(ticket.getCodeUrl());
                ticket.setImage(imageByte);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error generando ticket dinámicamente");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ticket;
        }

        @Override
        protected void onPostExecute(Ticket ticket) {
            super.onPostExecute(ticket);
            if (ticket != null) {
                File tempFile = null;
                FileOutputStream fos = null;
                try {
                    tempFile = File.createTempFile("temp", ".png");
                    fos = new FileOutputStream(tempFile);
                    fos.write(ticket.getImage());
                    if (tempFile.exists()) {
                        mService.write(Commands.PRINT_SPEED);
                        mService.write(Commands.INTERNATIONAL_CHARACTERSET);
                        mService.write(Commands.PRINT_ALIGNMENT_CENTER);
                        mService.sendMessage("******************************\n", "GBK");
                        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        mService.sendMessage(df.format(new Date(Long.valueOf(ticket.getDate()))) + "\n", "GBK");
                        mService.write(Commands.FONT_SIZE_NAME);
                        mService.sendMessage(ticket.getName(), "GBK");
                        mService.write(Commands.FONT_SIZE_DESCRIPTION);
                        mService.sendMessage(ticket.getAddress() + "\n", "GBK");
                        mService.write(Commands.FONT_BOLD_ON);
                        mService.sendMessage(ticket.getDescription() + "\n", "GBK");
                        mService.write(Commands.FONT_BOLD_OFF);
                        mService.sendMessage(ticket.getCode(), "GBK");
                        PrintPic pg = new PrintPic();
                        pg.initCanvas(384);
                        pg.initPaint();
                        pg.drawImage(0, 0, tempFile.getAbsolutePath());
                        mService.write(pg.printDraw());
                        mService.sendMessage("******************************\n\n", "GBK");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
