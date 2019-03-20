package dpi.ks19.admin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;

class Utils {

    static boolean isNotNetworkConnected(@NonNull Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(mConnectivityManager == null){
            mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }
}

class CustomRequestQueue {
    private Context ctx;
    private static CustomRequestQueue instance;

    CustomRequestQueue(Context ctx){
        this.ctx = ctx;

    }

    static CustomRequestQueue getInstance(Context ctx){
        if(instance == null){
            instance = new CustomRequestQueue(ctx);
        }
        return instance;
    }

    <T> void setRequest(Request<T> request){
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        requestQueue.add(request);
    }
}