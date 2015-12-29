package io.github.epelde.didactictribble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mGenerateTicketButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGenerateTicketButton = (Button) findViewById(R.id.generate_ticket_button);
        mGenerateTicketButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }
}
