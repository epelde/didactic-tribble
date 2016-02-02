package io.github.epelde.didactictribble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrintDemo extends Activity {
    Button btnSearch;
    Button btnSendDraw;
    Button btnSend;
    Button btnClose;
    EditText edtContext;
    EditText edtPrint;
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_SELECT_IMAGE = 3;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mService = new BluetoothService(this, mHandler);
        if (mService.isAvailable() == false) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mService.isBTopen() == false) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        try {
            btnSendDraw = (Button) this.findViewById(R.id.btn_test);
            btnSendDraw.setOnClickListener(new ClickEvent());
            btnSearch = (Button) this.findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(new ClickEvent());
            btnSend = (Button) this.findViewById(R.id.btnSend);
            btnSend.setOnClickListener(new ClickEvent());
            btnClose = (Button) this.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new ClickEvent());
            edtContext = (EditText) findViewById(R.id.txt_content);
            btnClose.setEnabled(false);
            btnSend.setEnabled(false);
            btnSendDraw.setEnabled(false);
        } catch (Exception ex) {
            Log.e("* * *", ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null)
            mService.stop();
        mService = null;
    }

    class ClickEvent implements View.OnClickListener {
        public void onClick(View v) {
            if (v == btnSearch) {
                Intent serverIntent = new Intent(PrintDemo.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else if (v == btnSend) {
                String msg = edtContext.getText().toString();
                if (msg.length() > 0) {
                    mService.sendMessage(msg + "\n", "GBK");
                }
            } else if (v == btnClose) {
                mService.stop();
            } else if (v == btnSendDraw) {
                String msg = "";
                String lang = getString(R.string.strLang);
                printImage();

            	/*byte[] cmd = new byte[3];
                cmd[0] = 0x1b;
        	    cmd[1] = 0x21;
            	if((lang.compareTo("en")) == 0){	
            		cmd[2] |= 0x225;
            		mService.write(cmd);
					//cmd[0] = 0x1b;
					//cmd[1] = 0x2D;
					//cmd[2] = 0x02;
					mService.write(cmd);
            		mService.sendMessage("PROBANDO PROBANDO\n", "GBK");
            		cmd[2] &= 0xEF;
            		mService.write(cmd);
            		msg = "Code: ES48001001000320160128133818\n\n";
            		mService.sendMessage(msg,"GBK");
            	}else if((lang.compareTo("ch")) == 0){
            		cmd[2] |= 0x10;
            		mService.write(cmd);
        		    mService.sendMessage("��ϲ����\n", "GBK"); 
            		cmd[2] &= 0xEF;
            		mService.write(cmd);
            		msg = "  ���Ѿ��ɹ��������������ǵ�������ӡ����\n\n"
            		+ "  ����˾��һ��רҵ�����з�����������������Ʊ�ݴ�ӡ��������ɨ���豸��һ��ĸ߿Ƽ���ҵ.\n\n";
            	    
            		mService.sendMessage(msg,"GBK");
            	}*/
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
                            Toast.makeText(getApplicationContext(), "Connect successful",
                                    Toast.LENGTH_SHORT).show();
                            btnClose.setEnabled(true);
                            btnSend.setEnabled(true);
                            btnSendDraw.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d("��������", "��������.....");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d("��������", "�ȴ�����.....");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();
                    btnClose.setEnabled(false);
                    btnSend.setEnabled(false);
                    btnSendDraw.setEnabled(false);
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth open successful", Toast.LENGTH_LONG).show();
                } else {
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    con_dev = mService.getDevByMac(address);

                    mService.connect(con_dev);
                }
                break;
        }
    }

    @SuppressLint("SdCardPath")
    private void printImage() {
        new ImageFetcherTask().execute();
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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
            }
            return imgBytes;
        }

        @Override
        protected void onPostExecute(byte[] result) {
            super.onPostExecute(result);
            Log.i("* * * ", "* * * RESULT LENGTH:" + result.length);
            try {
                File tempFile = File.createTempFile("temp", ".png");
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(result);
                fos.flush();
                if (tempFile.exists()) {
                    Log.i("* * *", "* * * FILE EXISTS!!!");
                    PrintPic pg = new PrintPic();
                    pg.initCanvas(384);
                    pg.initPaint();
                    pg.drawImage(0, 0, tempFile.getAbsolutePath());
                    fos.close();
                    mService.write(pg.printDraw());
                    //tempFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("* * * ", "* * * FILE NOT FOUND");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("* * * ", "* * * IO EXCEPTION");
            }
        }
    }
}
