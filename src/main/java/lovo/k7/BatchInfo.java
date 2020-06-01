package lovo.k7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author : 617
 * @date 2020年04月28日 14:08
 **/
public class BatchInfo extends Activity {
    private List<Map<String, String>> dataList;
    private final Map<String, Integer> clothesClass = new HashMap<>();
    private final Map<String, String> otherMessage = new HashMap<>();
    private TextView totalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_info);

        mapInit(clothesClass);

        //总件数
        totalCount = findViewById(R.id.totalCount);

        //数据源
        String[] dataResource = new String[]{"customerName", "fan", "clothesName", "clothesColor", "clothesLabel"};
        //数据值
        int[] dataValue = new int[]{R.id.customerName, R.id.fan, R.id.clothesName, R.id.clothesColor, R.id.clothesLabel};

        //listView
        ListView batchMessage = findViewById(R.id.batchMessage);
        //批次号
        String batchNO = getIntent().getStringExtra("batchNO");
        dataList = clothesMessage(batchNO);
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, dataList, R.layout.list_item, dataResource, dataValue);
        batchMessage.setAdapter(simpleAdapter);
    }

    private void mapInit(Map<String, Integer> clothesClass) {
        // Map<String, String> otherMessage = new HashMap<>();
        //通过批次号查询衣物，返回集合
        OutputActivity.cClassInit(clothesClass);
    }

    public List<Map<String, String>> clothesMessage(String batchNO) {
        List<Map<String, String>> messages = new ArrayList<>();
        String url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);
        final String[] parameter = {"批次查询", batchNO, url};
        try {
            String clothesInfo = new QueryClothesMessage().execute(parameter).get();
            if ("1".equals(clothesInfo.substring(0, 1))) {
                clothesInfo = clothesInfo.substring(4);
                JSONObject infos = JSONObject.parseObject(clothesInfo);
                JSONArray jsonArray = JSONArray.parseArray(infos.getString("Data"));
                totalCount.setText(String.valueOf(jsonArray.size()));
                if (jsonArray.size() > 0) {
                    JSONObject oneJson = jsonArray.getJSONObject(0);
                    otherMessage.put("dianhao", oneJson.getString("sdeptid"));
                    otherMessage.put("piciNO", batchNO);
                    otherMessage.put("gong", String.valueOf(jsonArray.size()));
                    otherMessage.put("storeName", oneJson.getString("sdeptname"));
                    otherMessage.put("gonghao", oneJson.getString("psy"));
                    for (Object obj : jsonArray) {
                        Map<String, String> map = new HashMap<>();
                        JSONObject clothesJson = JSONObject.parseObject(obj.toString());
                        String clothesClassStr = clothesJson.getString("sClasName");
                        Set<String> set = clothesClass.keySet();
                        if (StringUtils.isNotBlank(clothesClassStr)) {
                            for (String str : set) {
                                if (clothesClassStr.equals(str)) {
                                    clothesClass.put(clothesClassStr, clothesClass.get(clothesClassStr) + 1);
                                }
                            }
                        }
                        map.put("customerName", clothesJson.getString("sBuyerName"));
                        map.put("clothesName", clothesJson.getString("sPriceName"));
                        map.put("clothesColor", clothesJson.getString("sColor"));
                        if (StringUtils.isNotBlank(clothesJson.getString("sbqbh"))) {
                            map.put("clothesLabel", clothesJson.getString("sbqbh"));
                        } else {
                            map.put("clothesLabel", clothesJson.getString("sListID"));
                        }
                        map.put("clothesID", clothesJson.getString("sListID"));
                        map.put("clothesReturn", clothesJson.getString("fxcs"));
                        if (StringUtils.isNotBlank(clothesJson.getString("fxcs"))) {
                            int i = Integer.parseInt(clothesJson.getString("fxcs"));
                            if (i > 0) {
                                map.put("fan", "(返)");
                            }
                        }
                        if (StringUtils.isNotBlank(clothesJson.getString("sfitting"))) {
                            map.put("clothesEnclosure", clothesJson.getString("sfitting"));
                        } else {
                            map.put("clothesEnclosure", "");
                        }
                        messages.add(map);
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return messages;
    }

    //补打出厂单
    public void print(View view) {
        final String ccdIP = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("ccdIP", null);
        AlertDialog.Builder builder = new AlertDialog.Builder(BatchInfo.this);
        builder.setIcon(R.drawable.error);
        builder.setTitle("请确认");
        builder.setMessage("出否打印出厂单");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (StringUtils.isNotBlank(ccdIP)) {
                            Socket socket = null;
                            try {
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(ccdIP, 9100), 1000);
                                boolean b = socket.isConnected();
                                socket.close();
                                if (b) {//判断打印机能否连接
                                    new PrintOUT().print(ccdIP, dataList, otherMessage, clothesClass);
                                    finish();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(new long[]{100, 800, 100, 800}, -1);
                                Toast.makeText(getApplicationContext(), "打印机连接失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BatchInfo.this);
                            builder.setIcon(R.drawable.error);
                            builder.setTitle("打印机连接失败");
                            builder.setMessage("请填写打印机ip地址");
                            builder.setPositiveButton("确定", null);
                            builder.show();
                        }
                        Looper.loop();
                    }
                }).start();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
