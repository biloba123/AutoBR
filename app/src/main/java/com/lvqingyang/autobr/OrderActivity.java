package com.lvqingyang.autobr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lvqingyang.autobr.net.RequestHelper;
import com.lvqingyang.autobr.net.RequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lvqingyang.autobr.MyToast.cancel;

public class OrderActivity extends BaseActivity {

    private static final String TAG = "OrderActivity";
    private static final String KEY_CODE = "CODE";
    private static final String KEY_TYPE = "TYPE";
    private Code mCode;
    private int mOperation;
    private User mUser;
    private List<OrderBooks> mBooks;
    private de.hdodenhof.circleimageview.CircleImageView civhead;
    private android.widget.TextView tvname;
    private android.widget.TextView tvcard;
    private android.support.v7.widget.RecyclerView rv;
    private SolidRVBaseAdapter mAdapter;
    private int mScanedCount;


    public static void start(Context context,int opType,Code c) {
        Intent starter = new Intent(context, OrderActivity.class);
        starter.putExtra(KEY_CODE,c);
        starter.putExtra(KEY_TYPE,opType);
        context.startActivity(starter);
    }


    @Override
    protected void initContentView(Bundle bundle) {
        setContentView(R.layout.activity_order);
    }

    @Override
    protected void initView() {
        this.rv = (RecyclerView) findViewById(R.id.rv);
        this.tvcard = (TextView) findViewById(R.id.tv_card);
        this.tvname = (TextView) findViewById(R.id.tv_name);
        this.civhead = (CircleImageView) findViewById(R.id.civ_head);
        mOperation=getIntent().getIntExtra(KEY_TYPE,-1);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null) {
            if (mOperation== MainActivity.OP_BORROW) {
                actionBar.setTitle("借书");
            }else if (mOperation== MainActivity.OP_RESTORE) {
                actionBar.setTitle("还书");
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void setListener() {
        MqttService.addMqttListener(new MqttListener() {
            @Override
            public void onConnected() {
                cancelDialog();
                MqttService.getMyMqtt().subTopic("Code",1);
            }

            @Override
            public void onFail() {
                showPauseServiceDialog();
            }

            @Override
            public void onLost() {
                showPauseServiceDialog();
            }

            @Override
            public void onRecieive(String message) {
                if (BuildConfig.DEBUG) Log.d(TAG, "onRecieive: "+message);
                if (isBarCode(message)) {
                    int index=-1;
                    for (int i = 0; i < mBooks.size(); i++) {
                        if (mBooks.get(i).getBarCode().equals(message)) {
                            index=i;
                            break;
                        }
                    }

                    if (index!=-1) {
                        View v=rv.getChildAt(index);
                        ImageView iv=v.findViewById(R.id.iv_scaned);
                        if (iv.getVisibility()== View.GONE) {
                            iv.setVisibility(View.VISIBLE);
                            mScanedCount++;
                            readyOp();
                        }else if (iv.getVisibility()== View.VISIBLE) {
                            MyToast.info(OrderActivity.this, R.string.scaned,Toast.LENGTH_SHORT);
                        }
                    }else {
                        MyToast.error(OrderActivity.this, R.string.bar_code_error,Toast.LENGTH_SHORT);
                    }
                }else {
                    MyToast.error(OrderActivity.this, R.string.bar_code_error,Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    protected void initData() {
        mCode= (Code) getIntent().getSerializableExtra(KEY_CODE);
        showLoadDtatDialog();

        MqttService.getMyMqtt().subTopic("Code",1);
    }

    @Override
    protected void setData() {

    }

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    private void showLoadDtatDialog(){
        MyToast.loading(this, R.string.load_data);

        RequestHelper.getOrder(this, mCode.getId()+"", new RequestListener() {
            @Override
            public void onResponse(String res) {
                cancel();
                try {
                    JSONObject object=new JSONObject(res);
                    Gson gson=new Gson();
                    Log.d(TAG, "onResponse: "+object.getString("User")+"\n"+object.getJSONArray("OrderBooks"));
                    mUser= gson.fromJson(object.getString("User"),User.class);
                    mBooks=new ArrayList<OrderBooks>();

                    JSONArray jsonArray=object.getJSONArray("OrderBooks");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo=jsonArray.getJSONObject(i);
                        Book book=gson.fromJson(jo.getString("Book"),Book.class);
                        OrderBooks orderBooks=new OrderBooks();
                        orderBooks.setBook(book);
                        orderBooks.setBarCode(jo.getString("BarCode"));
                        mBooks.add(orderBooks);
                    }
                    showOrder();
                } catch (JSONException e) {
                    e.printStackTrace();
                    onError();
                }
            }

            @Override
            public void onError() {
                cancel();
                showExitDialog();
            }
        });
    }

    private void showExitDialog(){

        View view=getLayoutInflater().inflate(R.layout.dialog_meg,null);
        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
                .show();


        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);
        TextView tvcancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvsure = (TextView) view.findViewById(R.id.tv_sure);
        TextView tvmsg = (TextView) view.findViewById(R.id.tv_msg);
        TextView tvtitle = (TextView) view.findViewById(R.id.tv_title);
        tvtitle.setText(R.string.info);
        tvmsg.setText(R.string.error_and_retry);
        tvcancel.setVisibility(View.GONE);
        tvsure.setText(R.string.exit);
        tvsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showOrder(){
        tvname.setText(mUser.getName());
        tvcard.setText(mUser.getIdCard());
        String icon=mUser.getIcon();
        if (icon!=null&&!TextUtils.isEmpty(icon)) {
            Glide.with(this)
                    .load("http://www.zeblog.cn"+icon)
                    .into(civhead);
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter=new SolidRVBaseAdapter<OrderBooks>(this, mBooks) {
            @Override
            protected void onBindDataToView(SolidCommonViewHolder holder, OrderBooks bean, int position) {
                holder.setText(R.id.name_tv,bean.getBook().getName());
                holder.setText(R.id.author_tv,bean.getBook().getAuthor());
                holder.setText(R.id.publisher_tv,bean.getBook().getPublisher());
                holder.setText(R.id.barcode_tv,bean.getBarCode());
                if (!bean.getBook().getImg().equals("不详")) {
                    holder.setImageFromInternet(R.id.cover_iv,bean.getBook().getImg());
                }
            }

            @Override
            public int getItemLayoutID(int viewType) {
                return R.layout.item_book;
            }

        };
        rv.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isBarCode(String msg){
        return msg.charAt(0)=='A'&&msg.length()==10;
    }

    private void readyOp(){
        if (mScanedCount==mBooks.size()) {
            MyToast.loading(this, R.string.do_operation);
            if (mOperation== MainActivity.OP_BORROW) {
                RequestHelper.confirmBorrow(this, mUser.getOpenID(), mCode.getId()+"", new RequestListener() {
                    @Override
                    public void onResponse(String res) {
                        MyToast.cancel();
                        Log.d(TAG, "onResponse: "+res);
                        if (res.equals("1")) {
                            showSuccDialog(getString(R.string.borrow_succ));

                        }else {
                            onError();
                        }
                    }

                    @Override
                    public void onError() {
                        MyToast.cancel();
                        showExitDialog();
                    }
                });
            }else {
                RequestHelper.confirmReturn(this, mUser.getOpenID(), mCode.getId()+"", new RequestListener() {
                    @Override
                    public void onResponse(String res) {
                        MyToast.cancel();
                        Log.d(TAG, "onResponse: "+res);
                        if (res.equals("1")) {
                            showSuccDialog(getString(R.string.return_succ));
                        }else {
                            onError();
                        }
                    }

                    @Override
                    public void onError() {
                        MyToast.cancel();
                        showExitDialog();
                    }
                });
            }
        }
    }

    private void showSuccDialog(String msg){
        View view=getLayoutInflater().inflate(R.layout.dialog_meg,null);
        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
                .show();


        LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll);
        TextView tvcancel = (TextView) view.findViewById(R.id.tv_cancel);
        TextView tvsure = (TextView) view.findViewById(R.id.tv_sure);
        TextView tvmsg = (TextView) view.findViewById(R.id.tv_msg);
        TextView tvtitle = (TextView) view.findViewById(R.id.tv_title);
        tvtitle.setText(R.string.info);
        tvmsg.setText(msg);
        ll.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        }).start();
    }
}
