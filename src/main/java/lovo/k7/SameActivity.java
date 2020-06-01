package lovo.k7;

import android.annotation.SuppressLint;
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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 入厂/洗涤/熨烫/质检
 */
@SuppressLint("SimpleDateFormat")
public class SameActivity extends Activity {

    //创建一个SharedPreferences对象 -- 将数据存储到sd卡中或者从sd卡中读取配置信息
    private SharedPreferences sharedPreferences;
    private EditText clothesNo;
    private TextView clothesSumNo;
    private Vibrator vibrator;
    private long[] parameter;
    private MyReceiver myReceiver;
    private ListView centerMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.same);

        //设置进入页面不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //注册广播(接收扫描到的数据)
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("com.android.receive_scan_action");
        registerReceiver(myReceiver, intentFilter);

        sharedPreferences = getSharedPreferences("peizhi", Context.MODE_PRIVATE);
        parameter = new long[]{100, 800, 100, 800};

        //获取员工编号文本框
        TextView yuangongNo = findViewById(R.id.yuangongNo);
        //自动填写员工编号
        yuangongNo.setText("工号：" + getIntent().getStringExtra("employeeNo"));

        //扫描的编码
        clothesNo = findViewById(R.id.clothesNo);
        //监听enter(当按键抬起时触发否则被调用2次)
        clothesNo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    setViewValues(clothesNo.getText().toString());
                    return true;
                }
                return false;
            }
        });
        clothesNo.setText("");

        //listview标签
        centerMessage = findViewById(R.id.centerMessage);

        //当日共计
        clothesSumNo = findViewById(R.id.clothesSumNo);
        clothesSumNo.setText("0");
        //工序
        TextView gongxu = findViewById(R.id.gongxu);
        gongxu.setText(getIntent().getStringExtra("button") + ":");
    }


    /**
     * 当前页面关闭后，广播接收器反注册
     */
    @Override
    protected void onDestroy() {
        //取消注册
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }


    public void setViewValues(String data) {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        String url = sharedPreferences.getString("url", null);
        String factoryNO = sharedPreferences.getString("factoryNO", null);
        //將扫描到的编号写到文本框中
        clothesNo.setText(data);
        //通过编号查询信息
        String[] clothesData = {"工厂手持工序",
                getIntent().getStringExtra("button") + ";" + data + ";" + getIntent().getStringExtra("employeeNo") + ";;"
                        + getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("factoryNO", null), url};
        try {
            String returnData = new QueryClothesMessage().execute(clothesData).get();
            if ("1".equals(returnData.substring(0, 1))) { //如果扫描有结果
                int lastIndex = returnData.lastIndexOf("}");
                String[] endArray = returnData.substring(lastIndex + 1).split("###");
                Map<String, String> other = new HashMap<>();
                other.put("cTotalNumber", endArray[2]);
                if (endArray.length > 3) {
                    other.put("cBackReason", endArray[3]);
                } else {
                    other.put("cBackReason", "");
                }
                String newReturnDatas = returnData.substring(4, lastIndex + 1);
                newReturnDatas = JSONObject.parseObject(newReturnDatas).getString("Data");
                JSONArray jsonArray = JSONObject.parseArray(newReturnDatas);
                final JSONObject j = JSONObject.parseObject(jsonArray.get(0).toString());
                List<Map<String, String>> dataList = BaseData.addList(jsonArray.get(0), other);
                SimpleAdapter simpleAdapter = new SimpleAdapter(SameActivity.this, dataList, R.layout.list_query, BaseData.dataResource, BaseData.dataValue);
                centerMessage.setAdapter(simpleAdapter);
                clothesSumNo.setText(endArray[1]);
                //如果是质检则调用打印机打印合格证
                if ("质检".equals(getIntent().getStringExtra("button"))) {
                    final String hgzIP = sharedPreferences.getString("hgzIP", null);
                    j.put("dijijian", endArray[2]);
                    j.put("factoryNO", factoryNO);
                    if (endArray.length > 3) {
                        j.put("backReason", endArray[3]);
                    } else {
                        j.put("backReason", "");
                    }
                    if (StringUtils.isNoneBlank(hgzIP)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                Socket socket;
                                try {
                                    socket = new Socket();
                                    socket.connect(new InetSocketAddress(hgzIP, 9100), 1000);
                                    boolean b = socket.isConnected();
                                    socket.close();
                                    if (b) {//判断打印机能否连接
//                                        new PrintHGZ().printRelease(hgzIP, j);
                                        //new PrintHGZ().printOther(hgzIP, j);
                                        new PrintHGZ().printQinghai(hgzIP, j);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    vibrator.vibrate(parameter, -1);
                                    Toast.makeText(getApplicationContext(), "打印机连接失败", Toast.LENGTH_SHORT).show();
                                }
                                Looper.loop();
                            }
                        }).start();
                    }
                }
            } else {
                vibrator.vibrate(parameter, -1);
                AlertDialog.Builder builder = new AlertDialog.Builder(SameActivity.this);
                builder.setIcon(R.drawable.error);
                builder.setTitle("错误信息:");
                builder.setMessage(returnData.substring(4) + "\n\n" + "是否继续扫描");
                builder.setPositiveButton("确定", null);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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