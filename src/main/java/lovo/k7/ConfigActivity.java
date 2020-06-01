package lovo.k7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConfigActivity extends Activity {

    /*创建一个SharedPreferences对象 -- 将数据存储到sd卡中或者从sd卡中读取配置信息*/
    private SharedPreferences sharedPreferences;

    /**测试服务器是否能连接成功*/
    private HttpURLConnection conn = null;
    private URL url;
    private String urlString = "";

    /**
     * 服务器IP
     */
    private EditText fuwuqiIp;
    //服务器端口
    private EditText dunakou;
    //工厂编号
    private EditText gongchangbianhao;
    //出厂单打印IP
    private EditText chuchangdanIp;
    //合格证打印IP
    private EditText hegezhengIp;
    //震动
    private Vibrator vibrator;
    private final long[] patterm = {100, 800, 100, 800};
    //出厂单打印份数
    private EditText printCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        fuwuqiIp = findViewById(R.id.fuwuqiIp);
        dunakou = findViewById(R.id.port);
        gongchangbianhao = findViewById(R.id.gongchangbianhao);
        chuchangdanIp = findViewById(R.id.chuchangdanIp);
        hegezhengIp = findViewById(R.id.hegezhengIp);
        printCount = findViewById(R.id.printCount);

        sharedPreferences = getSharedPreferences("peizhi", Context.MODE_PRIVATE);
        String serverIP = sharedPreferences.getString("serverIP", null);
        fuwuqiIp.setText(serverIP);
        String port = sharedPreferences.getString("port", null);
        dunakou.setText(port);
        String factoryNO = sharedPreferences.getString("factoryNO", null);
        gongchangbianhao.setText(factoryNO);
        //获取出厂单打印机IP
        String ccdIP = sharedPreferences.getString("ccdIP", null);
        chuchangdanIp.setText(ccdIP);
        //打印份数
        String printCountStr = sharedPreferences.getString("printCount", null);
        printCount.setText(printCountStr);
        //获取合格证打印机IP
        String hgz = sharedPreferences.getString("hgzIP", null);
        hegezhengIp.setText(hgz);
    }

    public void trueButton(View v) {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String serverIP = fuwuqiIp.getText().toString();
        String port = dunakou.getText().toString();
        String factoryNO = gongchangbianhao.getText().toString();
        final String ccdIP = chuchangdanIp.getText().toString();
        String printCountStr = printCount.getText().toString();
        final String hgzIP = hegezhengIp.getText().toString();

        //将获取过来的值放入文件
        editor.putString("serverIP", serverIP);
        editor.putString("port", port);
        editor.putString("factoryNO", factoryNO);
        editor.putString("ccdIP", ccdIP);
        editor.putString("hgzIP", hgzIP);
        editor.putString("printCount", printCountStr);
        if (StringUtils.isNotBlank(serverIP) && StringUtils.isNotBlank(port)) {
            editor.putString("url", "http://" + serverIP + ":" + port + "/SOAP");
            urlString = "http://" + serverIP + ":" + port + "/SOAP";
        } else {
            editor.putString("url", "");
        }
        editor.apply();
        //测试服务器是否能连接成功
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigActivity.this);
                int server;
                try {
                    url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(1000);
                    server = conn.getResponseCode();
                    if (server == 200) {
                        finish();
                    }
                } catch (IOException e) {
                    vibrator.vibrate(patterm, -1);
                    builder.setIcon(R.drawable.error);
                    builder.setTitle("服务器连接失败");
                    builder.setMessage("请检查IP或端口号是否填写正确");
                    builder.setPositiveButton("确定", null);
                    builder.show();
                }
                Looper.loop();
            }
        }).start();
    }
}
