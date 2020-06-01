package lovo.k7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by 617 on 2020/4/13.
 * 查询
 */
public class QueryActivity extends Activity {
    private Vibrator vibrator;
    private final long[] patterm = {100, 800, 100, 800};
    private EditText selectNO;
    private MyReceiver myReceiver;
    private JSONObject printJson = null;
    private SharedPreferences sharedPreferences;
    private ListView queryListView;
    private List<Map<String, String>> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query);
        //设置进入页面不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPreferences = getSharedPreferences("peizhi", Context.MODE_PRIVATE);
        final String hgzIP = sharedPreferences.getString("hgzIP", null);

        //注册广播
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("com.android.receive_scan_action");
        registerReceiver(myReceiver, intentFilter);

        //扫描框
        selectNO = findViewById(R.id.selectNO);
        selectNO.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    setViewValues(selectNO.getText().toString());
                    return true;
                }
                return false;
            }
        });
        //补打质检签
        Button supply = findViewById(R.id.supply);
        supply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (printJson != null) {
                            if ("完成".equals(printJson.getString("zjstate"))) {
                                Socket socket;
                                try {
                                    socket = new Socket();
                                    socket.connect(new InetSocketAddress(hgzIP, 9100), 1000);
                                    boolean b = socket.isConnected();
                                    socket.close();
                                    if (b) {//判断打印机能否连接
                                        new PrintHGZ().printQinghai(hgzIP,printJson);
                                        // new PrintHGZ().printRelease(hgzIP, printJson);
                                        //new PrintHGZ().printOther(hgzIP, j);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    vibrator.vibrate(new long[]{100, 800, 100, 800}, -1);
                                    Toast.makeText(getApplicationContext(), "打印机连接失败", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                vibrator.vibrate(patterm, -1);
                                AlertDialog.Builder builder = new AlertDialog.Builder(QueryActivity.this);
                                builder.setIcon(R.drawable.error);
                                builder.setTitle("操作错误");
                                builder.setMessage("未质检衣服不可打印质检标签");
                                builder.setPositiveButton("确定", null);
                                builder.show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(QueryActivity.this);
                            builder.setIcon(R.drawable.error);
                            builder.setTitle("操作错误");
                            builder.setMessage("请先扫描");
                            builder.setPositiveButton("确定", null);
                            builder.show();
                        }
                        Looper.loop();
                    }
                }).start();
            }
        });

        //循环的listView标签
        queryListView = findViewById(R.id.queryListView);
    }

    /**
     * 取消注册广播
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }


    public void setViewValues(String datat) {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        String url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);
        //將扫描到的编号写到文本框中
        selectNO.setText(datat);
        //通过编号查询信息
        String[] clothesData = {"手持衣物查询", datat, url};
        try {
            String returnData = new QueryClothesMessage().execute(clothesData).get();
            if ("1".equals(returnData.substring(0, 1))) { //如果扫描有结果
                int lastIndex = returnData.lastIndexOf("}");
                String endMessage = returnData.substring(lastIndex + 1);
                String[] endArray = endMessage.split("###");
                String dijijianString = endArray[1];
                Map<String, String> other = new HashMap<>();
                other.put("cTotalNumber", dijijianString);
                if (endArray.length > 2) {
                    other.put("cBackReason", endArray[2]);
                } else {
                    other.put("cBackReason", "");
                }
                String newReturnDatas = returnData.substring(4, lastIndex + 1);
                newReturnDatas = JSONObject.parseObject(newReturnDatas).getString("Data");
                JSONArray jsonArray = JSONObject.parseArray(newReturnDatas);
                for (Object object : jsonArray) {
                    if (endArray.length > 2) {
                        putPrintData(endArray[2].split(" ")[2], dijijianString, object);
                    } else {
                        putPrintData("", dijijianString, object);
                    }
                    dataList = BaseData.addList(object, other);
                }
                SimpleAdapter simpleAdapter = new SimpleAdapter(QueryActivity.this, dataList, R.layout.list_query, BaseData.dataResource, BaseData.dataValue);
                queryListView.setAdapter(simpleAdapter);
            } else {
                returnData = returnData.substring(4);
                Toast.makeText(getApplicationContext(), returnData, Toast.LENGTH_SHORT).show();
                vibrator.vibrate(patterm, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //添加质检标签打印的信息
    private void putPrintData(String backReason, String dijijian, Object object) {
        String factoryNO = sharedPreferences.getString("factoryNO", null);
        printJson = JSONObject.parseObject(object.toString());
        printJson.put("dijijian", dijijian);
        printJson.put("factoryNO", factoryNO);
        printJson.put("backReason", backReason);
    }

    //广播接收者(内部类)
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "com.android.receive_scan_action")) {
                String data = intent.getStringExtra("data");
                setViewValues(data);
            }
        }
    }
}
