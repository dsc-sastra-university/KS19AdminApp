package dpi.ks19.admin;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import dpi.ks19.barcode_reader.BarcodeReaderActivity;
import dpi.ks19.barcode_reader.BarcodeReaderFragment;
import com.muddzdev.styleabletoast.StyleableToast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dpi.ks19.admin.app.R;

import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.vision.barcode.Barcode.EAN_13;
import static com.google.android.gms.vision.barcode.Barcode.QR_CODE;


public class HomeFragment extends Fragment implements BarcodeReaderFragment.BarcodeReaderListener {
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;
    private BarcodeReaderFragment readerFragment;
    private Recycler_View_Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private LinearLayout overview;
    private Group noDataGroup;
    private Button saveButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    private void setViews(View view) {
        if (getActivity() != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences("user", MODE_PRIVATE);
            String name = preferences.getString("name",null);
            TextView welcomeTv = view.findViewById(R.id.welcome_text);
            if (name != null) {
                welcomeTv.setText("Welcome, " + name);
            } else {
                welcomeTv.setText("Welcome");
                view.findViewById(R.id.different_user_group).setVisibility(View.GONE);
                view.findViewById(R.id.signin_group).setVisibility(View.VISIBLE);
            }
        }
    }

    private void addBarcodeReaderFragment() {
        readerFragment = BarcodeReaderFragment.newInstance(true, false, View.VISIBLE);
        readerFragment.setListener(this);
        FragmentManager supportFragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fm_container, readerFragment, "barcode");
        fragmentTransaction.commitAllowingStateLoss();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews(view);

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSharedPreferences("user", MODE_PRIVATE).edit()
                            .remove("name").apply();
                    setViews(view);
                }
            }
        });

        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(getActivity(), true, false);
                    startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
                    addBarcodeReaderFragment();
                }
            }
        });

        saveButton = view.findViewById(R.id.cancel);
        saveButton.setTag(view.findViewById(R.id.scan));
        view.findViewById(R.id.scan).setTag(view.findViewById(R.id.cancel));
        view.findViewById(R.id.cancel).setTag(view.findViewById(R.id.scan));
        overview = view.findViewById(R.id.overview);
        view.findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null && v.getVisibility() == View.VISIBLE) {
                    addBarcodeReaderFragment();
                    v.setVisibility(View.INVISIBLE);
                    ((View)v.getTag()).setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null && v.getVisibility() == View.VISIBLE) {
                    getFragmentManager().beginTransaction().remove(readerFragment).commitAllowingStateLoss();
                    v.setVisibility(View.INVISIBLE);
                    ((View)v.getTag()).setVisibility(View.VISIBLE);
                }
            }
        });

        noDataGroup = view.findViewById(R.id.no_data_group);
        recyclerView = view.findViewById(R.id.books_recycler);
        ArrayList<String> list = new ArrayList<>();
        recyclerViewAdapter = new Recycler_View_Adapter(list,getActivity());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void vibrate(boolean error) {
        if (getActivity() != null && error) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            long[] time = {0, 100, 100, 100, 200};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(time,-1));
            } else {
                v.vibrate(time, -1);
            }
        } else if (getActivity() != null) {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(200);
            }
        }
    }

    @Override
    public void onScanned(Barcode barcode) {
        if (getActivity() != null) {
            if (barcode.format == EAN_13) {
                vibrate(false);
                String rawData = barcode.rawValue;
                noDataGroup.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
                recyclerViewAdapter.insert(0,rawData);
            } else if (barcode.format == QR_CODE) {
                vibrate(false);
                String rawData = barcode.rawValue;
                int count = 0;
                for(int i=0; i < rawData.length(); i++) {
                    if(rawData.charAt(i) == '$')
                        count++;
                }
                if (count == 1 && rawData.length() > 3) {
                    String name = rawData.substring(0,rawData.indexOf('$'));
                    String limitStr = rawData.substring(rawData.indexOf('$')+1);
                    try {
                        int limit = Integer.parseInt(limitStr);
                        ((TextView)overview.findViewById(R.id.name)).setText(name);
                        ((TextView)overview.findViewById(R.id.limit)).setText(limitStr + " books");
                    } catch (NumberFormatException e) {
                        StyleableToast.makeText(getActivity(), "Invalid ID card", Toast.LENGTH_LONG, R.style.red_toast).show();
                    }
                } else {
                    StyleableToast.makeText(getActivity(), "Invalid ID card", Toast.LENGTH_LONG, R.style.red_toast).show();
                }
            } else {
                StyleableToast.makeText(getActivity(),"Invalid barcode format",Toast.LENGTH_LONG,R.style.red_toast).show();
                vibrate(true);
            }
        }
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }

    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {
        List<String> list;
        Context context;

        Recycler_View_Adapter(List<String> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public View_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
            return new View_Holder(v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull View_Holder holder, int position) {
            holder.isbn.setText("ISBN: " + list.get(position));
//            if (position == 0) {
//                holder.line.setVisibility(View.INVISIBLE);
//            }
            holder.delete.setTag(list.get(position));
        }

        @Override
        public int getItemCount() {
            //returns the number of elements the RecyclerView will display
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView on a predefined position
        void insert(int position, String data) {
            if(list.contains(data)) {
                if (getActivity() != null)
                    StyleableToast.makeText(getActivity(),"Book already added",Toast.LENGTH_LONG,R.style.red_toast).show();
                return;
            }
            list.add(position, data);
            notifyItemInserted(position);
            recyclerView.smoothScrollToPosition(0);
        }

        // Remove a RecyclerView item containing a specified AttendanceData object
        void remove(String data) {
            int position = list.indexOf(data);
            list.remove(position);
            if (list.isEmpty()) {
                noDataGroup.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.INVISIBLE);
            }
            notifyItemRemoved(position);
        }

    }

    class View_Holder extends RecyclerView.ViewHolder {

        TextView isbn;
        ImageButton delete;
        View line;

        View_Holder(View itemView) {
            super(itemView);
            isbn = itemView.findViewById(R.id.isbn);
            line = itemView.findViewById(R.id.line);
            delete = itemView.findViewById(R.id.delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pos = (String) v.getTag();
                    recyclerViewAdapter.remove(pos);
                }
            });
        }
    }
}
