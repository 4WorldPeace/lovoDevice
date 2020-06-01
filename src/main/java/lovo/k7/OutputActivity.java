package lovo.k7;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class OutputActivity extends Activity {

    private SharedPreferences sharedPreferences;
    private String url;
    private EditText piciNO;
    private EditText qingsaomiao;
    private TextView chuchangGong;
    private ListView clothesInformation;
    private LinearLayout clothesTitle;
    private List<Map<String, String>> dataList;
    public String[] dataResource;
    public int[] dataValue;
    private Vibrator vibrator;
    private final long[] patterm = {100, 800, 100, 800};
    private MyReceiver myReceiver;
    private EditText dianhao;
    private String gong;
    private String storeName;
    private TextView gonghao;
    private String data;
    private Boolean aBoolean = false;
    private final Map<String, Integer> cClass = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_out);

        cClassInit(cClass);

        //注册广播
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("com.android.receive_scan_action");
        registerReceiver(myReceiver, intentFilter);

        sharedPreferences = getSharedPreferences("peizhi", Context.MODE_PRIVATE);
        //设置进入页面不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //员工编号自动填写
        gonghao = findViewById(R.id.gonghao);
        gonghao.setFocusable(false);
        gonghao.setText(getIntent().getStringExtra("employeeNo"));

        //店号
        dianhao = findViewById(R.id.dianhao);
        //批次不可编辑
        piciNO = findViewById(R.id.pici);
        piciNO.setFocusable(false);

        //衣物信息标题
        clothesTitle = findViewById(R.id.clothesTitle);

        dataList = new ArrayList<>();
        //循环的listView标签
        clothesInformation = findViewById(R.id.clothesInformation);
        //数据源
        dataResource = new String[]{"customerName", "fan", "clothesName", "clothesColor", "clothesLabel"};
        //数据值
        dataValue = new int[]{R.id.customerName, R.id.fan, R.id.clothesName, R.id.clothesColor, R.id.clothesLabel};

        qingsaomiao = findViewById(R.id.qingsaomiao);
        //监听enter
        qingsaomiao.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    data = qingsaomiao.getText().toString();
                    //判断是否重复扫描
                    if (dataList.size() > 0) {
                        for (Map<String, String> m1 : dataList) {
                            if (m1.get("clothesLabel").equals(data) || m1.get("clothesID").trim().equals(data)) {
                                vibrator.vibrate(patterm, -1);
                                Builder builder = new Builder(OutputActivity.this);
                                builder.setIcon(R.drawable.error);
                                if (StringUtils.isNotBlank(m1.get("clothesLabel"))) {
                                    builder.setMessage("不能重复出厂" + "\n" + m1.get("clothesLabel").trim());
                                } else {
                                    builder.setMessage("不能重复出厂");
                                }
                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        qingsaomiao.requestFocus();
                                    }
                                });
                                builder.show();
                                aBoolean = false;
                            } else {
                                aBoolean = true;
                            }
                        }
                        if (aBoolean) {
                            getClothesInformation(data);
                            return true;
                        }
                    } else {
                        getClothesInformation(data);
                        return true;
                    }
                }
                return false;
            }
        });
        qingsaomiao.setText("");

        //当日共计
        chuchangGong = findViewById(R.id.chuchangGong);
    }

    static void cClassInit(Map<String, Integer> cClass) {
        cClass.put("上衣", 0);
        cClass.put("下衣", 0);
        cClass.put("套装", 0);
        cClass.put("鞋包", 0);
        cClass.put("家纺", 0);
        cClass.put("坐垫", 0);
        cClass.put("奢品", 0);
        cClass.put("皮草", 0);
        cClass.put("其他", 0);
        cClass.put("自定义", 0);
    }


    /**
     * 当页面被关闭，取消广播接收者
     */
    @Override
    protected void onDestroy() {
        //取消注册
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    /**
     * 将每次扫描返回的衣物信息添加到list中
     */
    public void addList(JSONObject json) {
        Map<String, String> map = new HashMap<>();
        map.put("customerName", json.getString("sBuyerName"));
        map.put("clothesName", json.getString("sPriceName"));
        map.put("clothesColor", json.getString("sColor"));
        if (StringUtils.isNotBlank(json.getString("sbqbh"))) {
            map.put("clothesLabel", json.getString("sbqbh"));
        } else {
            map.put("clothesLabel", json.getString("sListID"));
        }
        map.put("clothesID", json.getString("sListID"));
        map.put("clothesReturn", json.getString("fxcs"));
        if (StringUtils.isNotBlank(json.getString("fxcs"))) {
            int i = Integer.parseInt(json.getString("fxcs"));
            if (i > 0) {
                map.put("fan", "(返)");
            }
        }
        if (StringUtils.isNotBlank(json.getString("sfitting"))) {
            map.put("clothesEnclosure", json.getString("sfitting"));
        } else {
            map.put("clothesEnclosure", "");
        }
        dataList.add(map);
    }

    //查询衣物信息并将信息显示到列表中
    public void getClothesInformation(String data) {
        Builder builder = new Builder(OutputActivity.this);
        qingsaomiao.setText(data);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);

        String[] config = {"工厂手持工序", getIntent().getStringExtra("button") + ";" + data + ";" + getIntent().getStringExtra("employeeNo") + ";"
                + piciNO.getText().toString() + ";" + getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("factoryNO", null), url};
        try {
            //返回的数据
            String returnDatas = new QueryClothesMessage().execute(config).get();
            //如果有返回
            if ("1".equals(returnDatas.substring(0, 1))) {
                // Map<String, String> map = new HashMap<>();
                int lastIndex = returnDatas.lastIndexOf("}");
                String subReturnDatas = returnDatas.substring(4, lastIndex + 1);
                String jsonData = JSONObject.parseObject(subReturnDatas).getString("Data");
                jsonData = jsonData.substring(1, jsonData.length() - 1);
                JSONObject jsonData2 = JSONObject.parseObject(jsonData);
                String clothesClass = jsonData2.getString("sClasName");
                Set<String> set = cClass.keySet();
                if (StringUtils.isNotBlank(clothesClass)) {
                    for (String str : set) {
                        if (clothesClass.equals(str)) {
                            cClass.put(clothesClass, cClass.get(clothesClass) + 1);
                        }
                    }
                }
                storeName = jsonData2.getString("sdeptname");
                //String clothesLabel = JSONObject.parseObject(jsonData).getString("sbqbh");
                //衣物信息显示之前，先将标题显示出来
                clothesTitle.setVisibility(View.VISIBLE);
                addList(jsonData2);
                SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.list_item, dataResource, dataValue);
                clothesInformation.setAdapter(simpleAdapter);
                gong = String.valueOf(dataList.size());
                chuchangGong.setText(gong);
            } else {
                vibrator.vibrate(patterm, -1);
                builder.setIcon(R.drawable.error);
                builder.setTitle(returnDatas.substring(4, returnDatas.length() - 17));
                builder.setMessage("是否继续扫描");
                builder.setPositiveButton("确定", null);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //生成批次按钮
    public void pici(View v) {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        String factoryNO = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("factoryNO", null);
        String url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);
        if (StringUtils.isBlank(dianhao.getText().toString().trim())) { //判断是否输入店号
            vibrator.vibrate(patterm, -1);
            Toast toast = Toast.makeText(getApplicationContext(), "请输入店号", Toast.LENGTH_SHORT);
            toast.show();
            piciNO.setText("");
            dianhao.requestFocus();
        } else { //生成批次
            EditText qingsaomiao = findViewById(R.id.qingsaomiao);
            String[] datas = {"手持出厂批次", dianhao.getText().toString() + ";" + factoryNO, url};
            QueryClothesMessage queryClothesMessage = new QueryClothesMessage();
            try {
                String piciNOString = queryClothesMessage.execute(datas).get();
                if ("1".equals(piciNOString.substring(0, 1))) {
                    piciNO.setText(piciNOString.substring(4));
                    qingsaomiao.requestFocus();
                } else {
                    vibrator.vibrate(patterm, -1);
                    Toast.makeText(getApplicationContext(), "生成批次号失败", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    //确认出厂按钮
    public void chuchang(View v) {
        final String ccdIP = sharedPreferences.getString("ccdIP", null);
        final String printCountStr = sharedPreferences.getString("printCount", null);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);
        if (StringUtils.isBlank(piciNO.getText().toString())) {
            Toast.makeText(getApplicationContext(), "请先生成批次号", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(patterm, -1);
        } else {
            try {
                Builder builder = new Builder(OutputActivity.this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("是否确认出厂");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //确认出厂打印出厂单
                        if (StringUtils.isNotBlank(ccdIP)) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    String dianhao1 = dianhao.getText().toString().trim();
                                    String piciNO1 = piciNO.getText().toString().trim();
                                    String gonghao1 = gonghao.getText().toString().trim();
                                    Map<String, String> otherMessage = new HashMap<>();
                                    otherMessage.put("dianhao", dianhao1);
                                    otherMessage.put("piciNO", piciNO1);
                                    otherMessage.put("gong", gong);
                                    otherMessage.put("storeName", storeName);
                                    otherMessage.put("gonghao", gonghao1);
                                    int count = 1;
                                    if (StringUtils.isNotBlank(printCountStr) && StringUtils.isNumeric(printCountStr)) {
                                        count = Integer.parseInt(printCountStr);
                                    }
                                    Socket socket;
                                    try {
                                        socket = new Socket();
                                        socket.connect(new InetSocketAddress(ccdIP, 9100), 1000);
                                        boolean b = socket.isConnected();
                                        socket.close();
                                        if (b) {//判断打印机能否连接
                                            for (int i = 1; i <= count; i++) {
                                                new PrintOUT().print(ccdIP, dataList, otherMessage, cClass);
                                                finish();
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        vibrator.vibrate(patterm, -1);
                                        Toast.makeText(getApplicationContext(), "打印机连接失败", Toast.LENGTH_SHORT).show();
                                    }
                                    Looper.loop();
                                }
                            }).start();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //广播接收者
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "com.android.receive_scan_action")) {
                data = intent.getStringExtra("data");
                if (dataList.size() > 0) {
                    for (Map<String, String> m1 : dataList) {
                        if (m1.get("clothesLabel").trim().equals(data) || m1.get("clothesID").trim().equals(data)) {
                            vibrator.vibrate(patterm, -1);
                            Builder builder = new Builder(OutputActivity.this);
                            builder.setIcon(R.drawable.error);
                            if (StringUtils.isNotBlank(m1.get("clothesLabel").trim())) {
                                builder.setMessage("不能重复出厂" + "\n" + m1.get("clothesLabel").trim());
                            } else {
                                builder.setMessage("不能重复出厂");
                            }
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    qingsaomiao.requestFocus();
                                }
                            });
                            builder.show();
                            aBoolean = false;
                        } else {
                            aBoolean = true;
                        }
                    }
                    if (aBoolean) {
                        if (StringUtils.isNotBlank(data)) {
                            getClothesInformation(data);
                        }
                    }
                } else {
                    if (StringUtils.isNotBlank(data)) {

                        getClothesInformation(data);
                    }
                }
            }
        }
    }

}
