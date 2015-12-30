package io.github.epelde.didactictribble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by epelde on 30/12/2015.
 */
public class TicketActivity extends AppCompatActivity {

    private Ticket ticket;
    private TextView mDate;
    private TextView mBusinessName;
    private TextView mBusinessAddress;
    private TextView mDescription;
    private TextView mCode;
    private ImageView mQrImage;
    private Button mPrintButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        ticket = (Ticket) getIntent().getSerializableExtra("TICKET");
        mDate = (TextView) findViewById(R.id.date_text_view);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("es", "ES"));
        mDate.setText(df.format(ticket.getDate()));
        mBusinessName = (TextView) findViewById(R.id.business_name_text_view);
        mBusinessName.setText(ticket.getBusinessName());
        mBusinessAddress = (TextView) findViewById(R.id.business_address_text_view);
        mBusinessAddress.setText(ticket.getBusinessAddress());
        mDescription = (TextView) findViewById(R.id.description_text_view);
        mDescription.setText(ticket.getDescription());
        mCode = (TextView) findViewById(R.id.code_text_view);
        mCode.setText(ticket.getCode());
        mQrImage = (ImageView) findViewById(R.id.qr_image_view);
        Picasso.with(this)
                .load(ticket.getQrCodeUrl())
                .into(mQrImage);
        mPrintButton = (Button) findViewById(R.id.print_button);
        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Imprimiendo ticket", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
