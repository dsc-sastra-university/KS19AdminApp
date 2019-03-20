package dpi.ks19.admin;

import androidx.appcompat.app.AppCompatActivity;
import dpi.ks19.admin.app.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.muddzdev.styleabletoast.StyleableToast;

public class LoginActivity extends AppCompatActivity {
    EditText accessCodeEditText;
    EditText nameEditText;

    private void setError(String message, View v) {
        Drawable d = getResources().getDrawable(R.drawable.ic_warning_white);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        ((EditText)v).setError(message,d);
        v.requestFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accessCodeEditText = findViewById(R.id.editText);
        nameEditText= findViewById(R.id.editText2);
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNotNetworkConnected(LoginActivity.this)) {
                    StyleableToast.makeText(LoginActivity.this,"No internet",R.style.red_toast).show();
                    return;
                }
                String accessKey = accessCodeEditText.getText().toString();
                String name = nameEditText.getText().toString();
                if (name.length() < 3) {
                    setError("Name is too short",nameEditText);
                    return;
                } else if (accessKey.isEmpty()) {
                    setError("Can't be empty",accessCodeEditText);
                    return;
                }
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                switch (accessKey) {
                    case "PR":
                        getSharedPreferences("app",MODE_PRIVATE).edit()
                                .putString("name",name)
                                .putBoolean("PR",true)
                                .putBoolean("slides",false).apply();
                        startActivity(intent);
                        LoginActivity.this.finish();
                        break;
                    case "HOSP":
                        getSharedPreferences("app",MODE_PRIVATE).edit()
                                .putString("name",name)
                                .putBoolean("PR",false)
                                .putBoolean("slides",false).apply();
                        startActivity(intent);
                        LoginActivity.this.finish();
                        break;
                    default:
                        setError("Incorrect access key", accessCodeEditText);
                        break;
                }
            }
        });
    }
}
