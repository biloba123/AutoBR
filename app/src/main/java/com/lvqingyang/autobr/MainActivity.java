package com.lvqingyang.autobr;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends BaseActivity {

    private android.widget.Button btnborrow;
    private android.widget.Button btnrestore;
    private static final String TAG = "MainActivity";

    private int mOperation;
    public static final int OP_BORROW = 124;
    public static final int OP_RESTORE = 620;

    private AlertDialog mInfoDialog;
    private boolean mIsWaitCode;
    private boolean mIsForegroung =true;


    @Override
    protected void onStart() {
        super.onStart();
        mIsForegroung =true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyMqtt myMqtt=MqttService.getMyMqtt();
        if (myMqtt!=null&&!myMqtt.isConnect()) {
            myMqtt.connectMqtt();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsForegroung=false;
    }

    @Override
    protected void initContentView(Bundle bundle) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initView() {
        this.btnrestore = (Button) findViewById(R.id.btn_restore);
        this.btnborrow = (Button) findViewById(R.id.btn_borrow);
    }

    @Override
    protected void setListener() {
        btnborrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsWaitCode=true;
                mOperation=OP_BORROW;
                showScanCodeDialog();
            }
        });

        btnrestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsWaitCode=true;
                mOperation=OP_RESTORE;
                showScanCodeDialog();
            }
        });

        MqttService.addMqttListener(new MqttListener() {
            @Override
            public void onConnected() {
                cancelDialog();
                MqttService.getMyMqtt().subTopic("Code",1);
            }

            @Override
            public void onFail() {
                if (mIsForegroung) {
                    showPauseServiceDialog();
                }
            }

            @Override
            public void onLost() {
                if (mIsForegroung) {
                    showPauseServiceDialog();
                }
            }

            @Override
            public void onRecieive(String message) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onRecieive: "+message);
                if (mIsForegroung) {
                    if (!mIsWaitCode) {
                        MyToast.info(MainActivity.this, R.string.choose_op_type,Toast.LENGTH_SHORT);
                    } else {
                        try {
                            Code c = new Gson().fromJson(message, Code.class);
                            mIsWaitCode = false;
                            mInfoDialog.cancel();
                            OrderActivity.start(MainActivity.this, mOperation, c);
                        } catch (Exception e) {
//                            MyToast.error(MainActivity.this, R.string.code_error, Toast.LENGTH_SHORT);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void initData() {
        MqttService.start(this);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    private void showScanCodeDialog(){
        View view=getLayoutInflater().inflate(R.layout.dialog_meg,null);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);
        TextView tvcancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvsure = (TextView) view.findViewById(R.id.tv_sure);
        TextView tvmsg = (TextView) view.findViewById(R.id.tv_msg);
        TextView tvtitle = (TextView) view.findViewById(R.id.tv_title);
        mInfoDialog=new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        mInfoDialog.show();


        tvtitle.setText(R.string.info);
        tvmsg.setText(R.string.scan_code);
        tvsure.setVisibility(View.INVISIBLE);
        tvcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInfoDialog.cancel();
                mIsWaitCode=false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MqttService.stop(this);
    }
}
