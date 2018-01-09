package com.enlern.pen.wt.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.enlern.pen.wt.activity.TrafficWisdomActivity;


/**
 * Created by pen on 2017/12/1.
 */

public class BroadcastMain extends BroadcastReceiver {
    private String rec;

    public static RecCallBack callBack;
    private String TAG = "BroadcastMain";

    public static void setCallBack(RecCallBack callBack) {
        BroadcastMain.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        rec = intent.getStringExtra("REC_NODE_TRAFFIC_DATA");
        if (callBack != null && TrafficWisdomActivity.bTraffic) {
            callBack.nodeRec(rec);
        }
    }

    /**
     * ETC更改状态
     *
     * @param wsn
     * @param status
     * @return
     */
    public String agreementFinishing(String wsn, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 34) + status + wsn.substring(38, wsn.length());
        }
        return wsn;
    }

    public String carFinishing(String wsn, String carName, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 28) + carName + wsn.substring(32, 34) + status + wsn.substring(38, wsn.length());
        }
        return wsn;
    }


    /**
     * 路灯系统更改状态
     *
     * @param wsn
     * @param status
     * @return
     */
    public String agreementFinishingStreet(String wsn, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 34) + status + wsn.substring(36, wsn.length());
        }
        return wsn;
    }

    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

}
