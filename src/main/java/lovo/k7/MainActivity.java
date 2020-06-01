package lovo.k7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 首页面
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    //查询按钮
    public void chaxun(View v) {
        //给查询按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, QueryActivity.class);
        //启动
        startActivity(intent);
    }

    //分拣
    public void fenjian(View v) {
        //给分拣按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, SameActivity.class);
        //携带员工编号
        intent.putExtra("employeeNo", getIntent().getStringExtra("employeeNo"));
        intent.putExtra("button", "分拣");
        //启动
        startActivity(intent);
    }

    //洗涤
    public void xidi(View v) {
        //给洗涤按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, SameActivity.class);
        //携带员工编号
        intent.putExtra("employeeNo", getIntent().getStringExtra("employeeNo"));
        intent.putExtra("button", "洗涤");
        //启动
        startActivity(intent);

    }

    //熨烫
    public void yuntang(View v) {
        //给熨烫按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, SameActivity.class);
        //携带员工编号
        intent.putExtra("employeeNo", getIntent().getStringExtra("employeeNo"));
        intent.putExtra("button", "熨烫");
        //启动
        startActivity(intent);
    }

    //质检
    public void zhijian(View v) {
        //给质检按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, SameActivity.class);
        //携带员工编号
        intent.putExtra("employeeNo", getIntent().getStringExtra("employeeNo"));
        intent.putExtra("button", "质检");
        //启动
        startActivity(intent);
    }

    //出厂
    public void chuchang(View v) {
        //给出厂按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, OutputActivity.class);
        //携带员工编号
        intent.putExtra("employeeNo", getIntent().getStringExtra("employeeNo"));
        intent.putExtra("button", "出厂");
        //启动
        startActivity(intent);
    }

    public void queryPici(View view) {
        //给查询批次按钮添加响应事件
        Intent intent = new Intent(MainActivity.this, BatchQuery.class);
        //启动
        startActivity(intent);
    }

}
