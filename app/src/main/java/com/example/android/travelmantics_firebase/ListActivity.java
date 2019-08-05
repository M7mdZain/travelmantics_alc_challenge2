package com.example.android.travelmantics_firebase;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ListActivity extends AppCompatActivity implements FirebaseDealsDBListener {

    public static final String LOG_TAG = "LOG_TAG";
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingSpinner;
    private TextView mTvOnline;
    private Handler mCheckInternetConnectivityHandler;
    private Runnable mCheckInternetConnectivityRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mLoadingSpinner = findViewById(R.id.loading_spinner);
        mTvOnline = findViewById(R.id.tv_online);
        mRecyclerView = findViewById(R.id.RVDeals);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insert_menu:
                if (!isOnline()) {
                    Toast.makeText(this, "Please connect to the internet", Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(LOG_TAG, "User Logged out");
                                FirebaseUtil.attachListener(); // back to login page
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnline()) {
            mRecyclerView.setVisibility(View.GONE);
            mLoadingSpinner.setVisibility(View.GONE);
            mTvOnline.setVisibility(View.VISIBLE);
            continueCheckingInternetUntilItComes();
            return;
        }
        loadRecyclerViewData();
    }

    @Override
    public void onContainsData() {
        Log.d(LOG_TAG, "onContainsData()");
        mLoadingSpinner.setVisibility(View.GONE);
    }


    boolean isOnline() {
        // Checking internet connectivity
        ConnectivityManager connectivityMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connectivityMgr != null) {
            activeNetwork = connectivityMgr.getActiveNetworkInfo();
        }
        return activeNetwork != null;
    }

    private void continueCheckingInternetUntilItComes() {
        if (mCheckInternetConnectivityHandler == null) {
            mCheckInternetConnectivityHandler = new Handler();
        }
        Log.i(LOG_TAG, "Handler is about to run");
        mCheckInternetConnectivityRunnable = new Runnable() {
            @Override
            public void run() {
                // work to be done after specific period of time (1000 msec)
                Log.i(LOG_TAG, "Handler is running");
                if (isOnline()) { // The internet comes again
                    Log.i(LOG_TAG, "I get back online");
                    loadRecyclerViewData();
                    mCheckInternetConnectivityHandler.removeCallbacks(this);
                    mCheckInternetConnectivityHandler = null;
                    Log.i(LOG_TAG, "Handler is stopped");
                }
                if (mCheckInternetConnectivityHandler != null) {
                    mCheckInternetConnectivityHandler.postDelayed(mCheckInternetConnectivityRunnable, 1000);
                }
            }
        };
        runOnUiThread(mCheckInternetConnectivityRunnable);
    }

    private void loadRecyclerViewData() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mTvOnline.setVisibility(View.GONE);
        mLoadingSpinner.setVisibility(View.VISIBLE);
        FirebaseUtil.openFirebaseReference("traveldeals", this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        DealAdapter adapter = new DealAdapter(this);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        FirebaseUtil.attachListener();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "SectionActivity onDestroy()");
        // Destroy internet checking handler
        if (mCheckInternetConnectivityHandler != null) {
            mCheckInternetConnectivityHandler.removeCallbacks(mCheckInternetConnectivityRunnable);
        }
        if (mCheckInternetConnectivityRunnable != null) {
            mCheckInternetConnectivityRunnable = null;
        }
    }


}
