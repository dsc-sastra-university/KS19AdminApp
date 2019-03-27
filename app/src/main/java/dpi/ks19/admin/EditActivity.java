package dpi.ks19.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import dpi.ks19.admin.app.R;
import dpi.ks19.admin.pojo.FirestoreData;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditActivity extends AppCompatActivity {
    boolean pr = false;
    String ksid;
    FirebaseFirestore db;
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

        ksid = jsonObject.optString("ksid",null);
        if (ksid != null)
            ((TextView)findViewById(R.id.ksid)).setText(String.format("KSID: %s", ksid));
        ((TextView)findViewById(R.id.college)).setText(jsonObject.optString("college"));
    }

    private String getTextFromTIET (int id) {
        Editable editable = ((TextInputEditText)findViewById(id)).getText();
        if (editable != null)
            return editable.toString();
        else
            return null;
    }

    @SuppressLint("SetTextI18n")
    private void setTextInViews() {
        if (firestoreData.getFeePaid() != null)
            ((TextInputEditText)findViewById(R.id.fee)).setText(firestoreData.getFeePaid());
        if (firestoreData.getRemarks() != null)
            ((TextInputEditText)findViewById(R.id.remarks)).setText(firestoreData.getRemarks());
        if (firestoreData.getCheckinTime() != null)
            ((TextView)findViewById(R.id.checkin_time)).setText(firestoreData.getCheckinTime());

        if (pr) {
            if (firestoreData.getId_proof() != null)
                ((TextInputEditText)findViewById(R.id.id_proof)).setText(firestoreData.getId_proof());
        } else {
            if (firestoreData.getNumOfDays() != null)
                ((TextInputEditText)findViewById(R.id.numOfDays)).setText(Integer.toString(firestoreData.getNumOfDays()));
            if (firestoreData.getRoom() != null)
                ((TextInputEditText)findViewById(R.id.room)).setText(firestoreData.getRoom());
            if (firestoreData.isFeeDue() != null) {
                if (firestoreData.isFeeDue())
                    ((RadioGroup)findViewById(R.id.radio_group)).check(R.id.due);
                else
                    ((RadioGroup)findViewById(R.id.radio_group)).check(R.id.not_due);
            }
            if (firestoreData.getCheckoutTime() != null)
                ((TextView)findViewById(R.id.checkout_time)).setText(firestoreData.getCheckoutTime());
        }
    }

    private void saveAndUploadToFirestore(DialogInterface dialog) {
        firestoreData.setFeePaid(getTextFromTIET(R.id.fee));
        firestoreData.setRemarks(getTextFromTIET(R.id.remarks));
        firestoreData.setCheckinTime(((TextView)findViewById(R.id.checkin_time)).getText().toString());

        if (pr) {
            firestoreData.setId_proof(getTextFromTIET(R.id.id_proof));

        } else {
            try {
                if (getTextFromTIET(R.id.numOfDays) != null)
                    firestoreData.setNumOfDays(Integer.parseInt(getTextFromTIET(R.id.numOfDays)));
            } catch (NumberFormatException ignore) {
            }
            firestoreData.setRoom(getTextFromTIET(R.id.room));

            switch (((RadioGroup)findViewById(R.id.radio_group)).getCheckedRadioButtonId()) {
                case R.id.due:
                    firestoreData.setFeeDue(true);
                    break;
                case R.id.not_due:
                    firestoreData.setFeeDue(false);
                    break;
            }
        }

        progressDialog.setTitle("Saving");
        progressDialog.setMessage("Uploading data to the server");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        db.collection("users").document(ksid).set(firestoreData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                StyleableToast.makeText(EditActivity.this,"Saved successfully", Toast.LENGTH_LONG,R.style.success_toast).show();
                dialog.dismiss();
                progressDialog.dismiss();
                EditActivity.this.finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StyleableToast.makeText(EditActivity.this,"Error uploading data", Toast.LENGTH_LONG,R.style.red_toast).show();
            }
        });
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

        if (ksid == null) {
            StyleableToast.makeText(EditActivity.this,"User does not have KSID",R.style.red_toast).show();
            return;
        }

        progressDialog = new ProgressDialog(EditActivity.this,R.style.CustomProgress);
        progressDialog.setTitle("Fetching data");
        progressDialog.setMessage("Loading previously entered data from the server");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

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


        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(ksid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                firestoreData = documentSnapshot.toObject(FirestoreData.class);
                progressDialog.dismiss();
                if (firestoreData == null) {
                    firestoreData = new FirestoreData();

                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                    builder.setTitle("New user");
                    builder.setMessage("No records found for the scanned user. A profile will be created for the user with present time as checkin time");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a dd-MM-yyyy", Locale.ENGLISH);
                    ((TextView)findViewById(R.id.checkin_time)).setText(getString(R.string.checkin_time, simpleDateFormat.format(new Date(System.currentTimeMillis()))));
                } else {
                    setTextInViews();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StyleableToast.makeText(EditActivity.this,"Error fetching data", Toast.LENGTH_LONG,R.style.red_toast).show();
                findViewById(R.id.save_button).setVisibility(View.GONE);
                progressDialog.dismiss();
                finish();
            }
        });

        if (!pr) {
            findViewById(R.id.change_checkout_time_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firestoreData.getCheckoutTime() != null) {
                        StyleableToast.makeText(EditActivity.this,"User has been checked out. Can't change checkout time",R.style.red_toast).show();
                        return;
                    }
                    if (Utils.isNotNetworkConnected(EditActivity.this)) {
                        StyleableToast.makeText(EditActivity.this,"No internet",R.style.red_toast).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                    builder.setTitle("Are you sure?");
                    builder.setMessage("You can not undo this action. Proceed with caution");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            firestoreData.setCheckoutTime(((TextView)findViewById(R.id.checkout_time)).getText().toString());
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a dd-MM-yyyy", Locale.ENGLISH);
                            firestoreData.setCheckoutTime(simpleDateFormat.format(new Date(System.currentTimeMillis())));
                            saveAndUploadToFirestore(dialog);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
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
            findViewById(R.id.change_checkin_btn).setOnClickListener(v -> {
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
                                ((TextView)findViewById(R.id.checkin_time)).setText(getString(R.string.checkin_time, simpleDateFormat.format(setCalendar.getTime())));
                            }
                        },setCalendar.get(Calendar.HOUR_OF_DAY),setCalendar.get(Calendar.MINUTE),false);
                        timePickerDialog.show();
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            });
        }

        findViewById(R.id.save_button).setOnClickListener(v -> {
            if (Utils.isNotNetworkConnected(EditActivity.this)) {
                StyleableToast.makeText(EditActivity.this,"No internet",R.style.red_toast).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
            builder.setTitle("Save details?");
            builder.setMessage("Changes made will be uploaded to the server");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    saveAndUploadToFirestore(dialog);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            Typeface sniglet = ResourcesCompat.getFont(EditActivity.this, R.font.sniglet);
            if (sniglet != null) {
                TextView titleView = dialog.findViewById(R.id.alertTitle);
                TextView messageView = dialog.findViewById(android.R.id.message);
                Button button1 = dialog.findViewById(android.R.id.button1);
                Button button2 = dialog.findViewById(android.R.id.button2);
                if (titleView != null) titleView.setTypeface(sniglet, Typeface.BOLD);
                if (messageView != null) messageView.setTypeface(sniglet);
                if (button1 != null) button1.setTypeface(sniglet);
                if (button2 != null) button2.setTypeface(sniglet);
            }
        });
    }
}
