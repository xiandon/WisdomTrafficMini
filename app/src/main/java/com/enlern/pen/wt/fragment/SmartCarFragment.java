package com.enlern.pen.wt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.wt.R;
import com.enlern.pen.wt.activity.TrafficWisdomActivity;
import com.enlern.pen.wt.broadcast.BroadcastMain;
import com.enlern.pen.wt.broadcast.RecCallBack;
import com.pen.wind.spinner.nice.NiceSpinner;
import com.pen.wind.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.TrafficWisdomAnalysis;
import com.xiandon.wsn.node.TrafficWisdomAnalysisV2;
import com.xiandon.wsn.serial.SerialPortDownload;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android_serialport_api.SerialPort;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by pen on 2017/11/30.
 */

public class SmartCarFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.tv_car_num)
    TextView tvCarNum;
    @BindView(R.id.tv_car_inner_num)
    TextView tvCarInnerNum;
    @BindView(R.id.tv_car_flame)
    TextView tvCarFlame;
    private Context context;

    private NiceSpinner spCarSmartCars;
    List<String> dataset;

    public static String car_drive = "0002";// 小车前行
    public static String car_waitting = "0080";// 小车等待

    private View view;
    private String TAG = "SmartCarFragment";
    private BroadcastMain main;

    private TrafficWisdomAnalysisV2 analysis;

    private SerialPortDownload download;
    private SerialPort mSerialPort;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_smart_car, container, false);
        context = getActivity();
        initViews();
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initViews() {
        analysis = new TrafficWisdomAnalysisV2(context);
        spCarSmartCars = view.findViewById(R.id.sp_car_smart_cars);
        dataset = new LinkedList<>(Arrays.asList("请选择车辆", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9"));
        spCarSmartCars.attachDataSource(dataset);

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
        if (nodeInfo.getNode_num().equals("0044")) {
            tvCarNum.setText(nodeInfo.getNode_num());
            String[] nodes = nodeInfo.getData_analysis().split("--");
            tvCarInnerNum.setText(nodes[0]);
            tvCarFlame.setText(nodeInfo.getFrame_num());
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

    @OnClick({R.id.tv_car_go_head, R.id.tv_car_stop})
    public void onViewClicked(View view) {
        int i = spCarSmartCars.getSelectedIndex();
        String car = dataset.get(i);
        switch (view.getId()) {
            case R.id.tv_car_go_head:
                String wsn1 = main.carFinishing((String) SPUtils.get(context, "SAVE_TRAFFIC0044", ""), main.stringToHexString(car), car_drive);
                download.DownData(wsn1);
                break;
            case R.id.tv_car_stop:
                String wsn2 = main.carFinishing((String) SPUtils.get(context, "SAVE_TRAFFIC0044", ""), main.stringToHexString(car), car_waitting);
                download.DownData(wsn2);
                break;
        }
    }
}
