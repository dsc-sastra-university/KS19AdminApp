package dpi.ks19.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import dpi.ks19.admin.app.R;
import dpi.ks19.admin.pojo.FirestoreData;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {
    boolean pr = false;
    String ksid;
    private FirestoreData firestoreData;
    ProgressDialog progressDialog;

    private void setupUserDetails(@NonNull JSONObject jsonObject) throws JSONException {
        String rawDecrypted = jsonObject.getString("decrypted");
        if (rawDecrypted == null)
            throw new JSONException("Data was null");
        String[] separated = rawDecrypted.split("\\$%");
        if (separated.length < 6)
            throw new JSONException("Low params count");

        ((TextView)findViewById(R.id.name)).setText(separated[0]);
        ((TextView)findViewById(R.id.email)).setText(separated[1]);
        ((TextView)findViewById(R.id.mobile)).setText(separated[4]);

        ksid = jsonObject.optString("ksid");
        ((TextView)findViewById(R.id.ksid)).setText(ksid);
        ((TextView)findViewById(R.id.college)).setText(jsonObject.optString("college"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        String data = getIntent().getStringExtra("data");
        if (data == null) {
            StyleableToast.makeText(EditActivity.this,"Invalid data",R.style.red_toast).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(data);
            setupUserDetails(jsonObject);
        } catch (JSONException e) {
            StyleableToast.makeText(EditActivity.this,"Invalid data",R.style.red_toast).show();
            return;
        }

        progressDialog = new ProgressDialog(EditActivity.this);
        progressDialog.setTitle("Fetching data");
        progressDialog.setMessage("Loading previously entered data from the server");
        progressDialog.setIndeterminate(true);
        progressDialog.show();


        // Access a Cloud FirestoreData instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(ksid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                firestoreData = documentSnapshot.toObject(FirestoreData.class);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });


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
                    if (Utils.isNotNetworkConnected(EditActivity.this)) {
                        StyleableToast.makeText(EditActivity.this,"No internet",R.style.red_toast).show();
                        return;
                    }
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
        } else {
            findViewById(R.id.change_btn).setOnClickListener(v -> {
                if (Utils.isNotNetworkConnected(EditActivity.this)) {
                    StyleableToast.makeText(EditActivity.this,"No internet",R.style.red_toast).show();
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        if (month != Calendar.MARCH || year != 2019 || dayOfMonth < 20 || dayOfMonth > 24) {
                            StyleableToast.makeText(EditActivity.this,"Invalid date range. Please select within the duration of KS 19", Toast.LENGTH_LONG,R.style.red_toast).show();
                            return;
                        }
                        Calendar setCalendar = Calendar.getInstance();
                        setCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(EditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                setCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                setCalendar.set(Calendar.MINUTE,minute);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a dd-MM-yyyy", Locale.ENGLISH);
                                ((TextView)findViewById(R.id.time)).setText(getString(R.string.checkin_time, simpleDateFormat.format(setCalendar.getTime())));
                                StyleableToast.makeText(EditActivity.this,"Checkin time changed", Toast.LENGTH_SHORT,R.style.success_toast).show();
                            }
                        },setCalendar.get(Calendar.HOUR_OF_DAY),setCalendar.get(Calendar.MINUTE),false);
                        timePickerDialog.show();
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            });
        }

        findViewById(R.id.button5).setOnClickListener(v -> {
            if (pr) {
                firestoreData.setFeePaid(getTextFromTIET(R.id.fee));
                firestoreData.setId_proof(getTextFromTIET(R.id.id_proof));
                firestoreData.setRemarks(getTextFromTIET(R.id.remarks));

            } else {
                try {
                    if (getTextFromTIET(R.id.numOfDays) != null)
                        firestoreData.setNumOfDays(Integer.parseInt(getTextFromTIET(R.id.numOfDays)));
                } catch (NumberFormatException ignore) {

                }
                firestoreData.setRoom(getTextFromTIET(R.id.room));
                firestoreData.setFeePaid(getTextFromTIET(R.id.fee));
                firestoreData.setRemarks(getTextFromTIET(R.id.remarks));

                switch (((RadioGroup)findViewById(R.id.radio_group)).getCheckedRadioButtonId()) {
                    case R.id.due:
                        firestoreData.setFeeDue(true);
                        break;
                    case R.id.not_due:
                        firestoreData.setFeeDue(false);
                        break;
                }
            }
            if (Utils.isNotNetworkConnected(EditActivity.this)) {
                StyleableToast.makeText(EditActivity.this,"No internet",R.style.red_toast).show();
                return;
            }
            StyleableToast.makeText(EditActivity.this,"Saved successfully", Toast.LENGTH_LONG,R.style.success_toast).show();
            EditActivity.this.finish();
        });
    }

    private String getTextFromTIET (int id) {
        Editable editable = ((TextInputEditText)findViewById(id)).getText();
        if (editable != null)
            return editable.toString();
        else
            return null;
    }
}
