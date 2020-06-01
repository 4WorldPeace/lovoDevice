package lovo.k7;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends Activity {
    private final long[] patterm = {100,800,100,800};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    //点击登录按钮到此方法
    public void login(View v) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sp = getSharedPreferences("peizhi", Context.MODE_PRIVATE);
        String url = sp.getString("url", null);
        if (StringUtils.isBlank(url)) {
            vibrator.vibrate(patterm,-1);
            Toast.makeText(getApplicationContext(), "请先配置服务器信息", Toast.LENGTH_SHORT).show();
        } else {
            //获取用户名
            EditText nameString = (EditText) findViewById(R.id.nameText);
            String employeeNo = nameString.getText().toString();

            //获取密码
            EditText passwordString = (EditText) findViewById(R.id.passwordText);
            String password = passwordString.getText().toString();

            //将帐号密码放到string数组中
            String[] logindata = { "工厂手持登录", employeeNo + ";" + password + ";", url};

            //启动线程
            //校对帐号和密码是否正确(webService)
            QueryClothesMessage queryClothesState = new QueryClothesMessage();
            try {
                String dataString = queryClothesState.execute(logindata).get();
                if (!"1".equals(dataString)) {
                    vibrator.vibrate(patterm,-1);
                    Toast.makeText(getApplicationContext(), "账号或密码错误,请重新输入", Toast.LENGTH_SHORT).show();
                } else {
                    //给登录按钮添加响应事件
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("employeeNo", employeeNo);
                    //启动
                    startActivity(intent);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    public void peizhi(View v) {
        //给配置按钮添加响应事件
        Intent intent = new Intent(LoginActivity.this, ConfigActivity.class);
        //启动
        startActivity(intent);
    }

}
