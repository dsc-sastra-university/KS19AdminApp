package dpi.ks19.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dpi.ks19.barcode_reader.BarcodeReaderActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import dpi.ks19.admin.app.R;

import static com.google.android.gms.vision.barcode.Barcode.QR_CODE;
import static dpi.ks19.barcode_reader.BarcodeReaderActivity.KEY_CAPTURED_BARCODE;
import static dpi.ks19.barcode_reader.BarcodeReaderActivity.KEY_CAPTURED_RAW_BARCODE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("app",MODE_PRIVATE);
        if (preferences.getBoolean("slides",true)) {
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            this.finish();
            return;
        }

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(MainActivity.this, true, false);
                startActivityForResult(launchIntent, 1208);
            }
        });

        TextView title = findViewById(R.id.welcome_text);
        title.setText("Welcome, " + preferences.getString("name"," error"));
        TextView sub = findViewById(R.id.welcome_text2);
        String msg = (preferences.getBoolean("PR",false)) ? "Kuruksastra 19 Public Relations Team": "Kuruksastra 19 Hospitality Team";
        sub.setText(msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == BarcodeReaderActivity.RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Barcode barcode = (Barcode) bundle.get(KEY_CAPTURED_BARCODE);
                if ((barcode != null ? barcode.format : 0) != QR_CODE) {
                    StyleableToast.makeText(MainActivity.this, "Invalid barcode. Please scan your ID card", Toast.LENGTH_LONG, R.style.red_toast).show();
                    return;
                }
                String rawData = (String) bundle.get(KEY_CAPTURED_RAW_BARCODE);
                if (rawData != null) {
                    Intent intent = new Intent(MainActivity.this,EditActivity.class);
                    startActivity(intent);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
