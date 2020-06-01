package lovo.k7;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 617 on 2020/4/9.
 */
public class PrintHGZ extends Activity {
    private Socket socket = null;
    private OutputStream outputStream = null;

    //打印合格证标签(通用)
    public void printRelease(String ipAddress, JSONObject json) {
        init(ipAddress, 9100);
        barCode(140, 20, "128", 90, 1, 90, 2, 4, json.getString("sListID"));
        String storeName = "TEXT 160,20,\"TSS24.BF2\",0,0,0,\"店名：" + json.getString("sdeptname") + "\n";
        String clothesLabel = "TEXT 160,50,\"TSS24.BF2\",0,0,0,\"位置:" + json.getString("sAreaId") + json.getString("shookId") + " 标签:" + json.getString("sbqbh") + "\n";
        String clothesName = "TEXT 160,80,\"TSS24.BF2\",0,0,0,\"衣物:" + json.getString("sPriceName") + "/" + json.getString("sColor") + "\n";
        String clothesFlaw = "TEXT 160,110,\"TSS24.BF2\",0,0,0,\"瑕疵:" + json.getString("sState") + "\n";
        String clothesBrand = "TEXT 160,140,\"TSS24.BF2\",0,0,0,\"品牌:" + json.getString("sSizeUp") + "  第" + json.getString("dijijian") + "件\n";
        String customer = "TEXT 160,170,\"TSS24.BF2\",0,0,0,\"客户:" + json.getString("sBuyerName") + "/" + json.getString("sTele") + "\n";
        @SuppressLint("SimpleDateFormat") String inDate = "TEXT 160,200,\"TSS24.BF2\",0,0,0,\"收衣:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(Long.parseLong(json.getString("sInputDate")))) + "\n";
        String explain = "TEXT 160,230,\"TSS24.BF2\",0,0,0,\"反洗:" + json.getString("backReason") + "\n";
        String factoryNo = "TEXT 350,260,\"TSS24.BF2\",0,3,2,\"" + json.getString("sdeptid") + "\n";
        String disinfected = "TEXT 50,260,\"TSS24.BF2\",0,3,2,\"" + "已消毒" + "\n";

        try {
            sendCommand(storeName.getBytes("GBK"));
            sendCommand(clothesLabel.getBytes("GBK"));
            sendCommand(clothesName.getBytes("GBK"));
            sendCommand(clothesFlaw.getBytes("GBK"));
            sendCommand(clothesBrand.getBytes("GBK"));
            sendCommand(customer.getBytes("GBK"));
            sendCommand(inDate.getBytes("GBK"));
            sendCommand(explain.getBytes("GBK"));
            sendCommand(factoryNo.getBytes("GBK"));
            sendCommand(disinfected.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        printLabel(1, 1);
        closePort();
    }

    //打印合格证标签(邯郸)
    public void printOther(String ipAddress, JSONObject json) {
        init(ipAddress, 9100);
        barCode(400, 90, "128", 250, 1, 90, 4, 9, json.getString("sListID"));
        String storeName = "TEXT 460,90,\"TSS24.BF2\",90,1,1,\"店名：" + json.getString("sdeptname") + "    第" + json.getString("dijijian") + "件\n";
        String clothesLabel = "TEXT 430,90,\"TSS24.BF2\",90,1,1,\"客户:" + json.getString("sBuyerName") + "/" + json.getString("sTele") + "  标签:" + json.getString("sbqbh") + "\n";
        String clothesName = "TEXT 110,90,\"TSS24.BF2\",90,1,1,\"衣物名称:" + json.getString("sPriceName") + "    颜色:" + json.getString("sColor") + "\n";
        String clothesFlaw = "TEXT 80,90,\"TSS24.BF2\",90,1,1,\"瑕疵:" + json.getString("sState") + "\n";
        @SuppressLint("SimpleDateFormat") String inDate = "TEXT 50,90,\"TSS24.BF2\",90,1,1,\"收衣时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(json.getString("sInputDate")))) + "\n";

        try {
            sendCommand(storeName.getBytes("GBK"));
            sendCommand(clothesLabel.getBytes("GBK"));
            sendCommand(clothesName.getBytes("GBK"));
            sendCommand(clothesFlaw.getBytes("GBK"));
            sendCommand(inDate.getBytes("GBK"));
            printLabel(1, 1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            closePort();
        }
    }

    //打印合格证标签(青海)
    public void printQinghai(String ipAddress, JSONObject json) {
        init(ipAddress, 9100);
        barCode(140, 20, "128", 90, 1, 90, 2, 4, json.getString("sListID"));
        String storeName = "TEXT 150,20,\"TSS24.BF2\",0,1,1,\"" + json.getString("sdeptname") + "  第" + json.getString("dijijian") + "件\n";
        String clothesName = "TEXT 150,50,\"TSS24.BF2\",0,1,1,\"" + json.getString("sPriceName") + " " + json.getString("sColor") + "\n";
        String clothesLabel = "TEXT 350,50,\"TSS24.BF2\",0,2,1,\"" + json.getString("sAreaId") + json.getString("shookId") + "\n";
        String customer = "TEXT 150,80,\"TSS24.BF2\",0,1,2,\"" + json.getString("sBuyerName") + " " + json.getString("sTele") + "\n";
        @SuppressLint("SimpleDateFormat") String inDate = "TEXT 150,135,\"TSS24.BF2\",0,1,1,\"收衣时间:" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(Long.parseLong(json.getString("sInputDate")))) + "\n";
        String explain = "TEXT 150,170,\"TSS24.BF2\",0,1,1,\"说明:" + json.getString("backReason") + "\n";
        String factoryNo = "TEXT 50,270,\"TSS24.BF2\",0,3,2,\"" + json.getString("sdeptid") + "\n";
        String disinfected = "TEXT 200,220,\"TSS24.BF2\",0,4,4,\"" + "已消毒" + "\n";

        try {
            sendCommand(storeName.getBytes("GBK"));
            sendCommand(clothesLabel.getBytes("GBK"));
            sendCommand(clothesName.getBytes("GBK"));
            sendCommand(customer.getBytes("GBK"));
            sendCommand(inDate.getBytes("GBK"));
            sendCommand(explain.getBytes("GBK"));
            sendCommand(factoryNo.getBytes("GBK"));
            sendCommand(disinfected.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        printLabel(1, 1);
        closePort();
    }

    //初始化打印机
    public void init(String ipAddress, int port) {
        openPort(ipAddress, port);
        setup(60, 40, 2, 10, 0, 2, 0);
        clearBuffer();
        sendCommand("SET TEAR ON\n");//撕纸
    }

    public void clearBuffer() {
        String message = "CLS\n";
        byte[] msgBuffer = message.getBytes();

        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    //开始链接
    public void openPort(String ipAddress, int port) {
        //StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder()).detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        //StrictMode.setVmPolicy((new android.os.StrictMode.VmPolicy.Builder()).detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        try {
            this.socket = new Socket(ipAddress, port);
            this.outputStream = this.socket.getOutputStream();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    //打印设置
    public void setup(int width, int height, int speed, int density, int sensor, int sensor_distance, int sensor_offset) {
        String message;
        String size = "SIZE " + width + " mm" + ", " + height + " mm";
        String speed_value = "SPEED " + speed;
        String density_value = "DENSITY " + density;
        String sensor_value = "";
        if (sensor == 0) {
            sensor_value = "GAP " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        } else if (sensor == 1) {
            sensor_value = "BLINE " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        }

        message = size + "\n" + speed_value + "\n" + density_value + "\n" + sensor_value + "\n";
        byte[] msgBuffer = message.getBytes();

        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException var15) {
            var15.printStackTrace();
        }

    }

    //打印一维条码
    public void barCode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string) {
        String message;
        String barcode = "BARCODE ";
        String position = x + "," + y;
        String mode = "\"" + type + "\"";
        String height_value = "" + height;
        String human_value = "" + human_readable;
        String rota = "" + rotation;
        String narrow_value = "" + narrow;
        String wide_value = "" + wide;
        String string_value = "\"" + string + "\"";
        message = barcode + position + " ," + mode + " ," + height_value + " ," + human_value + " ," + rota + " ," + narrow_value + " ," + wide_value + " ," + string_value + "\n";
        byte[] msgBuffer = message.getBytes();

        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException var22) {
            var22.printStackTrace();
        }

    }

    //打印页面
    public void printLabel(int quantity, int copy) {
        String message;
        message = "PRINT " + quantity + ", " + copy + "\n";
        byte[] msgBuffer = message.getBytes();

        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException var6) {
            var6.printStackTrace();
        }

    }

    //关闭链接
    public void closePort() {
        try {
            if (socket != null) {
                this.socket.close();
            }
            if (outputStream != null) {
                this.outputStream.close();
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    //发送指令(数组)
    public void sendCommand(byte[] message) {
        try {
            this.outputStream.write(message);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    //发送指令(字符串)
    public void sendCommand(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    //暂停
    public void onPause() {
        super.onPause();
        if (this.outputStream != null) {
            try {
                this.outputStream.flush();
            } catch (IOException var3) {
                Log.e("THINBTCLIENT", "ON PAUSE: Couldn't flush output stream.", var3);
            }
        }

        try {
            this.socket.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    //开始
    public void onStart() {
        super.onStart();
    }

    //停止
    public void onStop() {
        super.onStop();
    }

    //销毁
    public void onDestroy() {
        super.onDestroy();
    }
}
