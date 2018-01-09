package com.enlern.pen.wt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enlern.pen.wt.R;
import com.enlern.pen.wt.activity.TrafficWisdomActivity;
import com.enlern.pen.wt.broadcast.BroadcastMain;
import com.enlern.pen.wt.broadcast.RecCallBack;
import com.pen.wind.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.TrafficWisdomAnalysisV2;
import com.xiandon.wsn.serial.SerialPortDownload;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import android_serialport_api.SerialPort;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by pen on 2017/11/30.
 */

public class StreetLightsFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.tv_street_light_num)
    TextView tvStreetLightNum;
    @BindView(R.id.tv_street_light_frame)
    TextView tvStreetLightFrame;
    @BindView(R.id.tv_street_light_mode)
    TextView tvStreetLightMode;
    private Context context;

    private View view;
    private String TAG = "StreetLightsFragment";
    private BroadcastMain main;

    private TrafficWisdomAnalysisV2 analysis;

    private SerialPortDownload download;
    private SerialPort mSerialPort;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_street_lights, container, false);
        context = getActivity();
        initViews();
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initViews() {
        analysis = new TrafficWisdomAnalysisV2(context);

        download = new SerialPortDownload();
        String PATH = (String) SPUtils.get(getActivity(), "PATH", "/dev/ttyUSB0");
        String RATE = (String) SPUtils.get(getActivity(), "RATE", "9600");
        try {
            mSerialPort = download.open(PATH, RATE);
        } catch (IOException e) {
            e.printStackTrace();
        }


        main = new BroadcastMain();
        main.setCallBack(new RecCallBack() {
            @Override
            public void nodeRec(String rec) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = rec;
                handler.sendMessage(message);
            }
        });
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initViews();
                    break;
                case 1:
                    String wsn = (String) msg.obj;
                    try {
                        NodeInfo nodeInfo = analysis.analysis(wsn);
                        if (nodeInfo != null && TrafficWisdomActivity.bTraffic) {
                            flashUi(nodeInfo);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    });

    private void flashUi(NodeInfo nodeInfo) {
        if (nodeInfo.getNode_num().equals("00ee")) {
            tvStreetLightNum.setText(nodeInfo.getNode_num());
            tvStreetLightFrame.setText(nodeInfo.getFrame_num());
            tvStreetLightMode.setText(nodeInfo.getData_analysis());
        }
    }

    @Override
    protected void lazyLoad() {
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_street_light_standard, R.id.tv_street_light_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_street_light_standard:
                String wsn = agreementFinishingStreet((String) SPUtils.get(context, "SAVE_TRAFFIC00ee", ""), "02");
                download.DownData(wsn);
                break;
            case R.id.tv_street_light_save:
                String wsn1 = agreementFinishingStreet((String) SPUtils.get(context, "SAVE_TRAFFIC00ee", ""), "01");
                download.DownData(wsn1);
                break;
        }
    }

    private static String agreementFinishingStreet(String wsn, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 30) + status + wsn.substring(32, wsn.length());
        }
        return wsn;
    }
}
