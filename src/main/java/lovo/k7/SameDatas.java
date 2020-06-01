package lovo.k7;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * 调用webService接口
 */
public class SameDatas {
    public String getData(String[] loginData) {
        SoapObject request = new SoapObject("http://tempuri.org/", "BillService___IFfun");
        request.addProperty("BillAct", loginData[0]);
        request.addProperty("InPutParam", loginData[1]);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        try {
            HttpTransportSE ht = new HttpTransportSE(loginData[2]);
            ht.call("http://tempuri.org/BillService___IFfun", envelope);
            return envelope.getResponse().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
