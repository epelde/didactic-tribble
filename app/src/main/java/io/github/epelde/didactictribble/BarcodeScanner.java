package io.github.epelde.didactictribble;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by epelde on 12/02/2016.
 */
public class BarcodeScanner extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
    }
}
