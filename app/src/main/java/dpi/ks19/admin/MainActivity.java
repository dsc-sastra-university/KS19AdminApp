package dpi.ks19.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.vision.barcode.Barcode;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONObject;

import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import dpi.ks19.admin.app.R;
import dpi.ks19.barcode_reader.BarcodeReaderActivity;

import static com.google.android.gms.vision.barcode.Barcode.QR_CODE;
import static dpi.ks19.barcode_reader.BarcodeReaderActivity.KEY_CAPTURED_BARCODE;
import static dpi.ks19.barcode_reader.BarcodeReaderActivity.KEY_CAPTURED_RAW_BARCODE;

public class MainActivity extends AppCompatActivity {
    Group progress_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences preferences = getSharedPreferences("app",MODE_PRIVATE);
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

        findViewById(R.id.button7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putBoolean("slides",true).apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

        TextView title = findViewById(R.id.welcome_text);
        title.setText("Welcome, " + preferences.getString("name"," error"));
        TextView sub = findViewById(R.id.welcome_text2);
        String msg = (preferences.getBoolean("PR",false)) ? "Kuruksastra 19 Public Relations Team": "Kuruksastra 19 Hospitality Team";
        sub.setText(msg);
        progress_group = findViewById(R.id.progress_group);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == BarcodeReaderActivity.RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Barcode barcode = (Barcode) bundle.get(KEY_CAPTURED_BARCODE);
                if ((barcode != null ? barcode.format : 0) != QR_CODE) {
                    StyleableToast.makeText(MainActivity.this, "Invalid barcode. Please scan the QR code", Toast.LENGTH_LONG, R.style.red_toast).show();
                    return;
                }
                String rawData = (String) bundle.get(KEY_CAPTURED_RAW_BARCODE);
                if (rawData != null) {
                    /* OkHttp code (semi implemented, Not working as of 16/3/19) */
//                    RequestBody body = new FormBody.Builder()
//                            .add("key", "AniruthRocksTheWorld1999")
//                            .add("text", rawData)
//                            .build();
//                    Request request = new Request.Builder().url("https://protocolfest.co.in/ks/participants/decryptQR.php").post(body).build();
//                    OkHttpClient client = new OkHttpClient();
//                    Call call = client.newCall(request);
//                    call.enqueue(new Callback() {
//                        @Override
//                        public void onFailure(@NonNull Call call,@NonNull IOException e) {
//                            runOnUiThread(() -> StyleableToast.makeText(MainActivity.this,"Uh oh. Something went wrong",R.style.red_toast).show());
//                        }
//
//                        @Override
//                        public void onResponse(@NonNull Call call,@NonNull Response response) {
//                            runOnUiThread(() -> StyleableToast.makeText(MainActivity.this,"Scan success",R.style.success_toast).show());
//                            if (response.body() != null)
//                                Log.e("RESPONSE",response.body().toString());
//                        }
//                    });

                    progress_group.setVisibility(View.VISIBLE);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("key","AniruthRocksTheWorld1999");
                    params.put("text",rawData);
                    JSONObject json = new JSONObject(params);
                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Method.POST,"https://protocolfest.co.in/ks/participants/decryptQR.php",json, response -> {
                        runOnUiThread(() -> StyleableToast.makeText(MainActivity.this,"Successfully fetched details",R.style.success_toast).show());
                        Log.e("RESPONSE",response.toString());
                        progress_group.setVisibility(View.GONE);
                        Intent intent = new Intent(MainActivity.this,EditActivity.class);
                        intent.putExtra("data",response.toString());
                        startActivity(intent);
                    }, error -> { runOnUiThread(() -> StyleableToast.makeText(MainActivity.this,"Uh oh. Invalid barcode",R.style.red_toast).show());
                        progress_group.setVisibility(View.GONE);});
                    jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    CustomRequestQueue.getInstance(MainActivity.this).setRequest(jsonRequest);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
