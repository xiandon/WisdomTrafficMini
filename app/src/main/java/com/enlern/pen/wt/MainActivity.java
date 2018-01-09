package com.enlern.pen.wt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.enlern.pen.wt.activity.MonitorActivity;
import com.enlern.pen.wt.activity.TrafficWisdomActivity;
import com.enlern.pen.wt.adapter.MainShowAdapter;
import com.enlern.pen.wt.broadcast.BroadcastMain;
import com.pen.wind.log.AndroidLogAdapter;
import com.pen.wind.log.DonLogger;
import com.pen.wind.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.TrafficWisdomAnalysisV2;
import com.xiandon.wsn.serial.SerialPortDownload;
import com.xiandon.wsn.serial.SerialPortForWsn;
import com.xiandon.wsn.serial.SerialProtocolV2;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Hashtable;

import android_serialport_api.SerialPort;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.sp_serial)
    Spinner spSerial;
    @BindView(R.id.sp_bit)
    Spinner spBit;
    @BindView(R.id.btn_openSerial)
    Button btnOpenSerial;
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    @BindView(R.id.recyclerView_main_show)
    RecyclerView recyclerViewMainShow;
    @BindView(R.id.iv_main_traffic)
    ImageView ivMainTraffic;
    @BindView(R.id.iv_main_monitor)
    ImageView ivMainMonitor;
    private Context context;
    private String strSerialSel = "";
    private String[] deviceEntries = null;
    private Hashtable<String, String> htSerialToPath = null;
    private String[] serialRates = null;
    private String strSerialPath = "";
    private String strSerialRateSel = "";
    private boolean bSerialIsOpen = false;

    public static ArrayList<byte[]> alFrames;
    final public static int iRcvBufMaxLen = 2048;
    public static int iRcvBufStart = 0;
    public static int iRcvBufLen = 0;
    public static byte[] baRcvBuf = new byte[iRcvBufMaxLen];
    boolean broadCastFlag = false;
    public static Handler mHandler;
    public static SerialPortForWsn mSerialport;
    private String TAG = "MainActivity";

    private TrafficWisdomAnalysisV2 analysis;
    private MainShowAdapter adapter;
    private SerialPortDownload download;
    private SerialPort mSerialPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        ButterKnife.bind(this);
        initView();

    }


    private void initView() {
        setSerialPort();
        boardCast();
        DonLogger.addLogAdapter(new AndroidLogAdapter());
        analysis = new TrafficWisdomAnalysisV2(context);
        tvPublicTitle.setText("上海因仑智慧交通系统");

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewMainShow.setLayoutManager(layoutManager);
        adapter = new MainShowAdapter(context);
        recyclerViewMainShow.setAdapter(adapter);
        startRotate();


    }

    /**
     * 广播
     */
        /*广播*/
    private LocalBroadcastManager broadcastManager;
    private IntentFilter filter;

    private void boardCast() {
        filter = new IntentFilter("MAIN_REC_TRAFFIC_TAG");
        broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.registerReceiver(new BroadcastMain(), filter);
    }

    private void setBroadCast(String m) {
        if (m.length() < 32) {
            return;
        }
        Intent intent = new Intent("MAIN_REC_TRAFFIC_TAG");
        intent.putExtra("REC_NODE_TRAFFIC_DATA", m);
        broadcastManager.sendBroadcast(intent);
    }

    private void startRotate() {
        Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.anim_main_traffic_image);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);//设置动画匀速运动
        if (operatingAnim != null) {
            ivMainMonitor.startAnimation(operatingAnim);
            ivMainTraffic.startAnimation(operatingAnim);
        }
    }

    private void stopRotate() {
        ivMainTraffic.clearAnimation();
        ivMainMonitor.clearAnimation();
    }


    @OnClick({R.id.btn_openSerial, R.id.tv_main_show_clear, R.id.iv_main_traffic, R.id.iv_main_monitor})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_openSerial:
                if (bSerialIsOpen) {
                    mSerialport.closeSerialPort();
                    bSerialIsOpen = false;
                    btnOpenSerial.setText("打开串口");
                } else {
                    try {
                        mSerialport.open(strSerialPath, strSerialRateSel);
                        SPUtils.put(context, "PATH", strSerialPath);
                        SPUtils.put(context, "RATE", strSerialRateSel);
                        bSerialIsOpen = true;
                        btnOpenSerial.setText("关闭串口");
                        download = new SerialPortDownload();
                        try {
                            mSerialPort = download.open(strSerialPath, strSerialRateSel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } catch (SecurityException e) {

                    } catch (IOException e) {

                    } catch (InvalidParameterException e) {
                    }
                }
                break;
            case R.id.tv_main_show_clear:
                adapter.clearData();
                break;
            case R.id.iv_main_traffic:
                startActivity(new Intent(context, TrafficWisdomActivity.class));
                break;
            case R.id.iv_main_monitor:
                startActivity(new Intent(context, MonitorActivity.class));
                break;

        }
    }


    private void setSerialPort() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int msgWhat = msg.what;
                switch (msgWhat) {
                    case 1:
                        break;
                    case 2:
                        if (MainActivity.alFrames != null && MainActivity.alFrames.size() > 0) {
                            String m = SerialProtocolV2.bytesToHexString(alFrames.get(0));
                            if (m.length() > 100) {
                                m = m.substring(m.length() - 100);
                            }

                            String sLength = m.substring(6, 10);
                            int iLength = Integer.parseInt(sLength, 16);

                            if (iLength * 2 + 10 == m.length()) {
                                try {
                                    NodeInfo nodeInfo = analysis.analysis(m);
                                    if (nodeInfo != null) {
                                        adapter.addData(0, nodeInfo);
                                      /*  boolean bSave = SPUtils.contains(context, "SAVE_TRAFFIC" + nodeInfo.getNode_num());
                                        if (!bSave) {
                                            SPUtils.put(context, "SAVE_TRAFFIC" + nodeInfo.getNode_num(), nodeInfo.getWsn());
                                        }*/
                                        SPUtils.put(context, "SAVE_TRAFFIC" + nodeInfo.getNode_num(), nodeInfo.getWsn());

                                        if (nodeInfo.getNode_num().equals("0044")) {
                                            String[] nodes = nodeInfo.getData_analysis().split("--");
                                            if (nodes.length == 3) {
                                                Log.i(TAG, "handleMessage: " + stringToHexString(nodes[2]));
                                                if (stringToHexString(nodes[2]).equals("0000")) {
                                                    String wsn = busStop((String) SPUtils.get(context, "SAVE_TRAFFIC00dd", ""), "00");
                                                    download.DownData(wsn);
                                                } else {
                                                    String wsn1 = busStop((String) SPUtils.get(context, "SAVE_TRAFFIC00dd", ""), nodes[0]);
                                                    download.DownData(wsn1);
                                                }
                                            }
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                }
                            }
                            setBroadCast(m);
                        }
                        break;
                    case 3:
                        int iLen = msg.arg1;
                        handleSerialData((byte[]) msg.obj, iLen);
                        break;
                    default:
                        break;
                }
                return false;
            }


        });

        mSerialport = new SerialPortForWsn(mHandler);
        deviceEntries = mSerialport.getSerials();
        htSerialToPath = mSerialport.getSerialsToPath();

        ArrayAdapter<String> adaComDevices = new ArrayAdapter<String>(this, R.layout.spinner_item,
                deviceEntries);
        adaComDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSerial.setAdapter(adaComDevices);
        spSerial.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择串口
                strSerialSel = deviceEntries[position];
                // 选择串口路径值
                strSerialPath = htSerialToPath.get(strSerialSel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                strSerialSel = deviceEntries[0];
            }
        });

        int iRate = 9600;
        serialRates = new String[7];
        for (int i = 0; i < 3; i++) {
            serialRates[i] = String.valueOf(iRate);
            iRate *= 2;
        }
        iRate = 57600;
        for (int i = 3; i < 7; i++) {
            serialRates[i] = String.valueOf(iRate);
            iRate *= 2;
        }

        ArrayAdapter<String> adaComRates = new ArrayAdapter<String>(this, R.layout.spinner_item,
                serialRates);
        adaComRates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBit.setAdapter(adaComRates);
        spBit.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择波特率
                strSerialRateSel = serialRates[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认波特率
                strSerialRateSel = serialRates[0];
            }
        });

    }


    /**
     * 判断数据
     *
     * @param buffer
     * @param size
     */

    private void handleSerialData(byte[] buffer, int size) {
        MainActivity.mSerialport.setIsIdle(false);
        int iBufLef = iRcvBufMaxLen - iRcvBufStart - iRcvBufLen;
        if (iBufLef < 0) {
            iRcvBufStart = 0;
            iRcvBufLen = 0;
            iBufLef = iRcvBufMaxLen;
        }
        if (iBufLef < size && iRcvBufStart > 0) {
            for (int i = 0; i < iRcvBufLen; i++) {
                baRcvBuf[i] = baRcvBuf[iRcvBufStart + i];
            }
            iRcvBufStart = 0;
            iBufLef = iRcvBufMaxLen - iRcvBufLen;
        }
        size = (iBufLef < size) ? iBufLef : size;
        int iIdx = iRcvBufStart + iRcvBufLen;
        for (int i = 0; i < size; i++) {
            baRcvBuf[iIdx + i] = buffer[i];
        }
        iRcvBufLen += size;
        SerialProtocolV2.recvDataLen = iRcvBufLen;

        MainActivity.alFrames = SerialProtocolV2.ReceiveToQBA(baRcvBuf, iRcvBufStart);
        iRcvBufLen = iRcvBufStart + iRcvBufLen - SerialProtocolV2.iHandValidIdx;
        iRcvBufStart = SerialProtocolV2.iHandValidIdx;

        if (MainActivity.alFrames != null && MainActivity.alFrames.size() > 0) {
            if (broadCastFlag) {
            } else {
                Message msg = new Message();
                msg.what = 2;
                MainActivity.mHandler.sendMessage(msg);
            }
        }
        MainActivity.mSerialport.setIsIdle(true);
    }

    private String busStop(String wsn, String s) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 28) + stringToHexString(s) + wsn.substring(32, wsn.length());
        }
        return wsn;
    }

    private static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


}


