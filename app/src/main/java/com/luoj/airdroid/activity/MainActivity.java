package com.luoj.airdroid.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luoj.airdroid.BuildConfig;
import com.luoj.airdroid.R;
import com.luoj.airdroid.adapter.BaseRecyclerViewAdapter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    String[] mPermissionList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    RecyclerView recyclerView;
    MyAdapter adapter;

    //    String[] display = {"本地无编码屏幕共享", "本地编码屏幕共享", "发送端", "接收端", "AutoConnect"};
//    Class[] classes = {TestNoEncodeLoopback.class, TestEncodeLoopback.class, RTPProjectionActivity.class, RTPPlayActivity.class, AutoConnectActivity.class};
//    String[] display = {"以输入IP方式连接"};
//    Class[] classes = {InputActivity.class};
    String[] display;
    Class[] classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter = new MyAdapter());
        initListData();
        adapter.refreshDataAndNotifyDataSetChanged(display);

        checkPermission();
    }

    private void initListData() {
        if (BuildConfig.DEBUG) {
            display = new String[]{"本地无编码屏幕共享", "本地编码屏幕共享", "发送端", "接收端", "以输入IP方式连接"};
            classes = new Class[]{TestNoEncodeLoopback.class, TestEncodeLoopback.class, RTPProjectionActivity.class, RTPPlayActivity.class, InputActivity.class};
        } else {
            display = new String[]{"以输入IP方式连接"};
            classes = new Class[]{InputActivity.class};
        }
    }

    void checkPermission() {
        ActivityCompat.requestPermissions(this, mPermissionList, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isGrantAll = true;
        for (int grant : grantResults) {
            logd("(-1 mean denied) isGrant->" + grant);
            if (grant == PackageManager.PERMISSION_DENIED) {
                isGrantAll = false;
                break;
            }
        }
    }

    public class MyAdapter extends BaseRecyclerViewAdapter<String, MyAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
            viewHolder.textView.setText(get(i));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Class clz = classes[i];
                    if (null != clz) {
                        startActivity(new Intent(MainActivity.this, clz));
                    }
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }
    }

}
