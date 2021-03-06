package io.github.epelde.didactictribble.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;
import java.util.List;

import io.github.epelde.didactictribble.CameraPreview;
import io.github.epelde.didactictribble.KobazuloService;
import io.github.epelde.didactictribble.R;
import io.github.epelde.didactictribble.ResultData;
import io.github.epelde.didactictribble.Results;
import retrofit.Call;
import retrofit.Response;

/**
 * Created by epelde on 12/02/2016.
 */
public class BarcodeScannerActivity extends AppCompatActivity {

    private static final String LOG_TAG = BarcodeScannerActivity.class.getSimpleName();

    private Camera camera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private ImageScanner scanner;
    private Button scanButton;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        initControls();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //autoFocusHandler = new Handler();
        //new CameraInstanceTask().execute();
        //new CameraHandlerThread().start();
    }

    private void initControls() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();
        camera = getCameraInstance();
        if (camera != null) {
            Log.i(LOG_TAG, "* * * PARECE QUE HAY CAMARA");
            // Instance barcode scanner
            scanner = new ImageScanner();
            scanner.setConfig(0, Config.X_DENSITY, 3);
            scanner.setConfig(0, Config.Y_DENSITY, 3);

            mPreview = new CameraPreview(BarcodeScannerActivity.this, camera, previewCb,
                    autoFocusCB);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            scanButton = (Button) findViewById(R.id.scan_button);

            scanButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (barcodeScanned) {
                        barcodeScanned = false;
                        camera.setPreviewCallback(previewCb);
                        camera.startPreview();
                        previewing = true;
                        camera.autoFocus(autoFocusCB);
                    }
                }
            });
        } else {
            Log.i(LOG_TAG, "* * * PARECE QUE NO HAY CAMARA");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Camera is not available (it's in use or does not exist)");
        }
        return c;
    }

    private void releaseCamera() {
        if (camera != null) {
            previewing = false;
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);
            int result = scanner.scanImage(barcode);
            if (result != 0) {
                previewing = false;
                BarcodeScannerActivity.this.camera.setPreviewCallback(null);
                BarcodeScannerActivity.this.camera.stopPreview();
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    new ValidateTicketTask().execute(sym.getData().trim());
                    barcodeScanned = true;
                    break;
                }
            }
        }
    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                camera.autoFocus(autoFocusCB);
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle(getResources().getString(R.string.app_name))
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BarcodeScannerActivity.this.finish();
                }
            }).show();
    }

    private class ValidateTicketTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String msg = "";
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(BarcodeScannerActivity.this);
            String code = sharedPref.getString(SettingsActivity.CODE_PREF, SettingsActivity.DEFAULT_VALUE);
            String key = sharedPref.getString(SettingsActivity.KEY_PREF, SettingsActivity.DEFAULT_VALUE);
            KobazuloService service = KobazuloService.Factory.create();
            Call<Results> call = service.validateTicket(code, key, params[0]);
            try {
                Response<Results> response = call.execute();
                Results results = response.body();
                List<ResultData> data =  results.getData();
                msg = data.get(0).getError();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return msg;
        }

        protected void onPostExecute(String msg) {
            showAlertDialog(msg);
        }
    }

   /* private class CameraHandlerThread extends android.os.HandlerThread {

        public CameraHandlerThread() {
            super("CameraThread");
        }

        @Override
        public void run() {
            camera = Camera.open();
            if (camera != null) {
                Log.i(LOG_TAG, "* * * CAMERATHREAD!!!!");
                initControls();
            }
        }
    }

    private class CameraInstanceTask extends AsyncTask<Void, Void, Camera> {
        @Override
        protected Camera doInBackground(Void... params) {
            try {
                return Camera.open();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Camera is not available (it's in use or does not exist)");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Camera c) {
            super.onPostExecute(camera);
            if (c != null) {
                Log.i(LOG_TAG, "* * * CAMERA");
            } else Log.i(LOG_TAG, "* * * NULL CAMERA");
            camera = c;
            initControls();
        }
    }*/
}
