package dpi.ks19.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import dpi.ks19.admin.app.R;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoast.StyleableToast;

public class EditActivity extends AppCompatActivity {
    boolean pr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        SharedPreferences preferences = getSharedPreferences("app",MODE_PRIVATE);
        ViewStub stub = findViewById(R.id.layout_stub);
        if (preferences.getBoolean("PR",false)) {
            stub.setLayoutResource(R.layout.card_pr);
            pr = true;
        }
        else {
            stub.setLayoutResource(R.layout.card_hosp);
            pr = false;
        }
        stub.inflate();

        if (!pr) {
            findViewById(R.id.button6).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                    builder.setTitle("Are you sure?");
                    builder.setMessage("You can not undo this action. Proceed with caution");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    Typeface font = ResourcesCompat.getFont(EditActivity.this, R.font.sniglet);
                    if (font != null) {
                        TextView titleView = dialog.findViewById(R.id.alertTitle);
                        TextView messageView = dialog.findViewById(android.R.id.message);
                        Button button1 = dialog.findViewById(android.R.id.button1);
                        Button button2 = dialog.findViewById(android.R.id.button2);
                        if (titleView != null) titleView.setTypeface(font, Typeface.BOLD);
                        if (messageView != null) messageView.setTypeface(font);
                        if (button1 != null) button1.setTypeface(font);
                        if (button2 != null) button2.setTypeface(font);
                    }
                }
            });
        }

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StyleableToast.makeText(EditActivity.this,"Saved successfully", Toast.LENGTH_LONG,R.style.success_toast).show();
                EditActivity.this.finish();
            }
        });
    }
}
