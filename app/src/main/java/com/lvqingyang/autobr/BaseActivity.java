package com.lvqingyang.autobr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Author：LvQingYang
 * Date：2017/8/30
 * Email：biloba12345@gamil.com
 * Github：https://github.com/biloba123
 * Info：
 */
public abstract class BaseActivity extends AppCompatActivity {

    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //有数据则取出
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            getBundleExtras(extras);
        }

        initContentView(savedInstanceState);

        initView();
        setListener();
        initData();
        setData();

    }

    //替代onCreate的使用
    protected abstract void initContentView(Bundle bundle);

    //初始化view
    protected abstract void initView();

    //设置监听器
    protected abstract void setListener();

    //初始化数据
    protected abstract void initData();

    //view显示数据
    protected abstract void setData();

    /**
     * Bundle  传递数据
     * @param extras
     */
    protected abstract void getBundleExtras(Bundle extras);


    protected void showPauseServiceDialog(){
        if (mDialog==null) {
            View view=getLayoutInflater().inflate(R.layout.dialog_meg,null);
            mDialog=new AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .create();
            mDialog.show();


            LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);
            TextView tvcancel = (TextView) view.findViewById(R.id.tv_cancel);
            TextView tvsure = (TextView) view.findViewById(R.id.tv_sure);
            TextView tvmsg = (TextView) view.findViewById(R.id.tv_msg);
            TextView tvtitle = (TextView) view.findViewById(R.id.tv_title);
            tvtitle.setText(R.string.info);
            tvmsg.setText(R.string.pause_service);
            ll.setVisibility(View.GONE);
        }
    }

    protected void cancelDialog(){
        if (mDialog != null) {
            mDialog.cancel();
            mDialog=null;
        }
    }

}
