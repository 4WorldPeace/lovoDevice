package lovo.k7;

import android.annotation.SuppressLint;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : 617
 * @date 2020年05月09日 16:34
 **/
public class BaseData {
    /**
     * 数据源
     */
    public static String[] dataResource = new String[]{"storeName", "cName", "cColor", "cPrice", "cLabel", "cOrder", "cFlaw", "cEnclosure", "cAddService", "cAddPrice", "cCollectDate", "cTakeDate", "cBackReason", "cStockStatus", "cIsTake", "cSortingDate", "cWashStatus", "cIroningStatus", "cQualityStatus", "cOutputDate", "cTotalNumber", "cCustomerName"};
    /**
     * 数据值
     */
    public static int[] dataValue = new int[]{R.id.storeName, R.id.cName, R.id.cColor, R.id.cPrice, R.id.cLabel, R.id.cOrder, R.id.cFlaw, R.id.cEnclosure, R.id.cAddService, R.id.cAddPrice, R.id.cCollectDate, R.id.cTakeDate, R.id.cBackReason, R.id.cStockStatus, R.id.cIsTake, R.id.cSortingDate, R.id.cWashStatus, R.id.cIroningStatus, R.id.cQualityStatus, R.id.cOutputDate, R.id.cTotalNumber, R.id.cCustomerName};

    public static List<Map<String, String>> addList(Object obj, Map<String, String> other) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy年MM月dd日 HH:mm:ss");
        Map<String, String> m = new HashMap<>();
        JSONObject clothesJson = JSONObject.parseObject(obj.toString());
        m.put("storeName", "店名：" + clothesJson.getString("sdeptname"));
        m.put("cName", "名称：" + clothesJson.getString("sPriceName"));
        m.put("cColor", "颜色：" + clothesJson.getString("sColor"));
        m.put("cPrice", "价格：" + clothesJson.getString("sprice"));
        m.put("cLabel", "标签：" + clothesJson.getString("sbqbh"));
        m.put("cOrder", "单号：" + clothesJson.getString("sbillid"));
        m.put("cFlaw", "瑕疵：" + clothesJson.getString("sState"));
        if (StringUtils.isNotBlank(clothesJson.getString("sfitting"))) {
            m.put("cEnclosure", "附件：" + clothesJson.getString("sfitting"));
        } else {
            m.put("cEnclosure", "附件：");
        }
        m.put("cAddService", "附加服务：" + clothesJson.getString("sAgnomen"));
        m.put("cAddPrice", "附加费：" + clothesJson.getString("sAgnomenprice"));
        if (StringUtils.isNotBlank(clothesJson.getString("sInputDate"))) {
            m.put("cCollectDate", "收衣时间：" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("sInputDate"))));
        } else {
            m.put("cCollectDate", "收衣时间：");
        }
        if (StringUtils.isNotBlank(clothesJson.getString("sOutputDate"))) {
            m.put("cTakeDate", "取衣时间：" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("sOutputDate"))));
        } else {
            m.put("cTakeDate", "取衣时间：");
        }
        m.put("cBackReason", "反洗：" + other.get("cBackReason").toString().trim());
        m.put("cStockStatus", "库存状态：" + clothesJson.getString("kcstate"));
        if (Boolean.parseBoolean(clothesJson.getString("sTakeAway"))) {
            m.put("cIsTake", "取走：" + "是");
        } else {
            m.put("cIsTake", "取走：" + "否");
        }
        if (StringUtils.isNotBlank(clothesJson.getString("rctime"))) {
            if (StringUtils.isNotBlank(clothesJson.getString("fjy"))) {
                m.put("cSortingDate", "分拣:" + "(" + clothesJson.getString("fjy") + ")" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("rctime"))));
            } else {
                m.put("cSortingDate", "分拣:" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("rctime"))));
            }
        } else {
            m.put("cSortingDate", "分拣:");
        }
        if (StringUtils.isNotBlank(clothesJson.getString("xdtime"))) {
            if (StringUtils.isNotBlank(clothesJson.getString("qxy"))) {
                m.put("cWashStatus", "洗涤:" + "(" + clothesJson.getString("qxy") + ")" + clothesJson.getString("xdstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("xdtime"))));
            } else {
                m.put("cWashStatus", "洗涤:" + clothesJson.getString("xdstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("xdtime"))));
            }
        } else {
            m.put("cWashStatus", "洗涤:" + clothesJson.getString("xdstate"));
        }
        if (StringUtils.isNotBlank(clothesJson.getString("yttime"))) {
            if (StringUtils.isNotBlank(clothesJson.getString("yty"))) {
                m.put("cIroningStatus", "熨烫:" + "(" + clothesJson.getString("yty") + ")" + clothesJson.getString("ytstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("yttime"))));
            } else {
                m.put("cIroningStatus", "熨烫:" + clothesJson.getString("ytstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("yttime"))));
            }
        } else {
            m.put("cIroningStatus", "熨烫:" + clothesJson.getString("ytstate"));
        }
        if (StringUtils.isNotBlank(clothesJson.getString("zjtime"))) {
            if (StringUtils.isNotBlank(clothesJson.getString("zjy"))) {
                m.put("cQualityStatus", "质检:" + "(" + clothesJson.getString("zjy") + ")" + clothesJson.getString("zjstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("zjtime"))));
            } else {
                m.put("cQualityStatus", "质检:" + clothesJson.getString("zjstate") + "/" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("zjtime"))));
            }
        } else {
            m.put("cQualityStatus", "质检:" + clothesJson.getString("zjstate"));
        }
        if (StringUtils.isNotBlank(clothesJson.getString("cctime"))) {
            if (StringUtils.isNotBlank(clothesJson.getString("psy"))) {
                m.put("cOutputDate", "出厂:" + "(" + clothesJson.getString("psy") + ")" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("cctime"))));
            } else {
                m.put("cOutputDate", "出厂:" + simpleDateFormat.format(Long.valueOf(clothesJson.getString("cctime"))));
            }
        } else {
            m.put("cOutputDate", "出厂:");
        }
        m.put("cTotalNumber", "总件数：" + other.get("cTotalNumber").toString().trim());
        m.put("cCustomerName", "顾客姓名：" + clothesJson.getString("sBuyerName") + "/" + clothesJson.getString("sTele"));
        List<Map<String, String>> list = new ArrayList<>();
        list.add(m);
        return list;
    }

}
