package lovo.k7;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author : 617
 * @date 2020年04月28日 10:14
 **/
public class BatchQuery extends Activity {
    private EditText storeNO;
    private EditText batchNO;
    private ListView batchList;

    private List<Map<String, String>> batchNOList;
    private String[] dataSource;
    private int[] dataValue;

    private BroadcastReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_main);
        //注册广播
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("com.android.receive_scan_action");
        registerReceiver(myReceiver, intentFilter);

        batchList = findViewById(R.id.batchList);
        batchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String batchNum = batchNOList.get(position).get("pici");
                Intent intent = new Intent(BatchQuery.this, BatchInfo.class);
                intent.putExtra("batchNO", batchNum);
                startActivity(intent);
            }
        });
        //数据源
        dataSource = new String[]{"pici"};
        //数据值
        dataValue = new int[]{R.id.batchNOs};

        //设置进入页面不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //门店编号
        storeNO = findViewById(R.id.storeNO);
        //批次号
        batchNO = findViewById(R.id.batchNO);

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

    //通过门店编号查询近5天的批次号
    public List<Map<String, String>> queryBatchs(String storeNOString) {
        List<Map<String, String>> batchNOList2 = new ArrayList<>();
        String url = getSharedPreferences("peizhi", Context.MODE_PRIVATE).getString("url", null);
        String[] storeNOStr = {"门店批次列表", storeNOString, url};
        try {
            String batchNOs = new QueryClothesMessage().execute(storeNOStr).get();
            batchNOs = batchNOs.substring(4);
            JSONObject nums = JSONObject.parseObject(batchNOs);
            JSONArray jsonArray = JSONArray.parseArray(nums.getString("Data"));
            for (Object o : jsonArray) {
                Map<String, String> map = new HashMap<>();
                JSONObject batchNOJson = JSONObject.parseObject(o.toString());
                map.put("pici", batchNOJson.getString("pici"));
                batchNOList2.add(map);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return batchNOList2;
    }

    //批次号查询
    public void batchNOQuery(View view) {
        String batchNOString = batchNO.getText().toString().trim();
        if (StringUtils.isNotBlank(batchNOString)) {
            Intent intent = new Intent(BatchQuery.this, BatchInfo.class);
            intent.putExtra("batchNO", batchNOString);
            startActivity(intent);
        }
    }

    //门店编号查询
    public void storeNOQuery(View view) {
        String storeNOString = storeNO.getText().toString().trim();
        if (StringUtils.isNotBlank(storeNOString)) {
            batchNOList = queryBatchs(storeNOString);
            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), batchNOList, R.layout.list_batch, dataSource, dataValue);
            batchList.setAdapter(simpleAdapter);
        }
    }

    //接收广播
    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "com.android.receive_scan_action")) {
                batchNO.setText(intent.getStringExtra("data"));
            }
        }
    }

}
