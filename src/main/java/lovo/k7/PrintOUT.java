package lovo.k7;

import android.annotation.SuppressLint;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : 617
 * @date 2020年04月23日 11:33
 **/
public class PrintOUT {
    private Socket socket = null;
    private OutputStream sout = null;

    public void print(String ipAddress, List<Map<String, String>> dataList, Map<String, String> otherMessage, Map<String, Integer> clothesClass) {
        openPort(ipAddress, 9100);
        Vector<Byte> receipt = getReceipt(dataList, otherMessage, clothesClass);
        try {
            byte[] handleMessage = convertVectorByteToBytes(receipt);
            sout.write(handleMessage);
            sout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closePort();
        }
    }

    protected byte[] convertVectorByteToBytes(Vector<Byte> data) {
        byte[] sendData = new byte[data.size()];
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); ++i) {
                sendData[i] = data.get(i);
            }
        }
        return sendData;
    }

    @SuppressLint("SimpleDateFormat")
    public static Vector<Byte> getReceipt(List<Map<String, String>> dataList, Map<String, String> otherMessage, Map<String, Integer> clothesClass) {
        EscCommand esc = new EscCommand();
        //初始化打印机
        esc.addInitializePrinter();
        //打印走纸多少个单位
        //esc.addPrintAndFeedLines((byte) 2);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

        //打印条码
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        // 设置条码可识别字符位置在条码下方
        // 设置条码高度为60点
        esc.addSetBarcodeHeight((byte) 100);
        // 设置条码宽窄比为2
        esc.addSetBarcodeWidth((byte) 2);
        // 打印Code128码
        esc.addCODE128(esc.genCodeB(otherMessage.get("piciNO")));
        esc.addPrintAndLineFeed();
        //字体放大
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_2);
        // 打印文字
        esc.addText(otherMessage.get("storeName") + "\n");
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_2, EscCommand.HEIGHT_ZOOM.MUL_2);
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        esc.addText(otherMessage.get("dianhao") + "店\t\t\t");
        esc.addText(otherMessage.get("gong") + "件\n");
        esc.addText("--------------------\n");
        //取消字体放大
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_1);
        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        // 打印文字
        esc.addText("(衣物出厂单)\n");
        // 打印文字
        esc.addText("批号:" + otherMessage.get("piciNO") + "\n");
        esc.addText("打印时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
        esc.addText("配送员:" + otherMessage.get("gonghao") + "\n");
        // 设置打印左对齐
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("=========================================\n");
        esc.addText("姓名");
        esc.addSetAbsolutePrintPosition((short) 110);
        esc.addText("衣物名");
        esc.addSetAbsolutePrintPosition((short) 250);
        esc.addText("颜色");
        esc.addSetAbsolutePrintPosition((short) 380);
        esc.addText("编号\n");
        int count = 0;
        for (Map<String, String> m : dataList) {
            //返厂信息
            String clothesReturn = m.get("clothesReturn").trim();
            if (StringUtils.isNotBlank(clothesReturn)) {
                int i = Integer.parseInt(clothesReturn);
                if (i > 0) {
                    clothesReturn = "(返)";
                    count++;
                } else {
                    clothesReturn = "";
                }
            }
            String customerName = m.get("customerName");
            esc.addText(customerName + clothesReturn);

            String clothesName = m.get("clothesName");
            esc.addSetAbsolutePrintPosition((short) 110);
            if (clothesName.length() > 6) {
                esc.addText(clothesName.substring(0, 6));
            } else {
                esc.addText(clothesName);
            }

            String clothesColor = m.get("clothesColor");
            if (clothesColor.length() > 6) {
                clothesColor = clothesColor.substring(0, 6);
            }
            esc.addSetAbsolutePrintPosition((short) 250);
            esc.addText(clothesColor);

            String clothesLabel = m.get("clothesLabel");
            String clothesID = m.get("clothesID");
            if (StringUtils.isNotBlank(clothesLabel)) {
                esc.addSetAbsolutePrintPosition((short) 380);
                esc.addText(clothesLabel + "\n");
            } else {
                esc.addSetAbsolutePrintPosition((short) 380);
                esc.addText(clothesID + "\n");
            }
            //附件
            String clothesEnclosure = m.get("clothesEnclosure").trim();

            //如果附件不是空的另起一行显示附件
            if (StringUtils.isNotBlank(clothesEnclosure)) {
                esc.addSetAbsolutePrintPosition((short) 110);
                boolean b = StringUtils.isNumeric(clothesEnclosure.trim());
                if (b) {
                    esc.addText("所属衣物:" + clothesEnclosure + "\n");
                } else {
                    esc.addText("附件:" + clothesEnclosure + "\n");
                }
            }
        }
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("=============合计汇总====================\n");
        esc.addText("上衣:" + clothesClass.get("上衣") + "件\t", "GBK");
        esc.addText("下衣:" + clothesClass.get("下衣") + "件\n", "GBK");
        esc.addText("套装:" + clothesClass.get("套装") + "件\t", "GBK");
        esc.addText("鞋包:" + clothesClass.get("鞋包") + "件\n", "GBK");
        esc.addText("家纺:" + clothesClass.get("家纺") + "件\t", "GBK");
        esc.addText("坐垫:" + clothesClass.get("坐垫") + "件\n", "GBK");
        esc.addText("奢品:" + clothesClass.get("奢品") + "件\t", "GBK");
        esc.addText("皮草:" + clothesClass.get("皮草") + "件\n", "GBK");
        esc.addText("其他:" + clothesClass.get("其他") + "件\n", "GBK");
        esc.addText("自定义:" + clothesClass.get("自定义") + "件\n", "GBK");
        //字体放大
        esc.addSetCharcterSize(EscCommand.WIDTH_ZOOM.MUL_1, EscCommand.HEIGHT_ZOOM.MUL_2);
        esc.addText("衣物总数:" + otherMessage.get("gong") + " 件\t", "GBK");
        esc.addText("返工件数:" + count + " 件\n", "GBK");
        esc.addPrintAndLineFeed();
        esc.addText("工厂出厂签名:\n", "GBK");
        esc.addPrintAndLineFeed();
        esc.addText("店面接收签名:\n", "GBK");

        //打印走纸n个单位
        esc.addPrintAndFeedLines((byte) 10);
        // 开钱箱
        //esc.addGeneratePlus(LabelCommand.FOOT.F2, (byte) 255, (byte) 255);
        //开启切刀
        esc.addCutPaper();
        //添加缓冲区打印完成查询
        byte[] bytes = {0x1D, 0x72, 0x49};
        //添加用户指令
        esc.addUserCommand(bytes);
        return esc.getCommand();
    }

    //开始链接
    public void openPort(String ipAddress, int port) {
        //StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder()).detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        //StrictMode.setVmPolicy((new android.os.StrictMode.VmPolicy.Builder()).detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        try {
            this.socket = new Socket(ipAddress, port);
            this.sout = this.socket.getOutputStream();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    //关闭资源链接
    public void closePort() {
        try {
            if (sout != null) {
                this.sout.close();
                this.sout = null;
            }
            if (socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
