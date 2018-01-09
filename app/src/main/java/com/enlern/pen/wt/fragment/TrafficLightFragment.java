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
import android.widget.ImageView;
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

public class TrafficLightFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.tv_traffic_light_group_id)
    TextView tvTrafficLightGroupId;
    @BindView(R.id.tv_traffic_light_node_num)
    TextView tvTrafficLightNodeNum;
    @BindView(R.id.tv_traffic_light_flame)
    TextView tvTrafficLightFlame;
    @BindView(R.id.tv_traffic_light_up_red)
    ImageView tvTrafficLightUpRed;
    @BindView(R.id.tv_traffic_light_up_yellow)
    ImageView tvTrafficLightUpYellow;
    @BindView(R.id.tv_traffic_light_up_green)
    ImageView tvTrafficLightUpGreen;
    @BindView(R.id.tv_traffic_light_left_red)
    ImageView tvTrafficLightLeftRed;
    @BindView(R.id.tv_traffic_light_left_yellow)
    ImageView tvTrafficLightLeftYellow;
    @BindView(R.id.tv_traffic_light_left_green)
    ImageView tvTrafficLightLeftGreen;
    @BindView(R.id.tv_traffic_light_right_red)
    ImageView tvTrafficLightRightRed;
    @BindView(R.id.tv_traffic_light_right_yellow)
    ImageView tvTrafficLightRightYellow;
    @BindView(R.id.tv_traffic_light_right_green)
    ImageView tvTrafficLightRightGreen;
    @BindView(R.id.tv_traffic_light_below_red)
    ImageView tvTrafficLightBelowRed;
    @BindView(R.id.tv_traffic_light_below_yellow)
    ImageView tvTrafficLightBelowYellow;
    @BindView(R.id.tv_traffic_light_below_green)
    ImageView tvTrafficLightBelowGreen;
    private Context context;

    private View view;
    private String TAG = "TrafficLightFragment";
    private BroadcastMain main;

    private TrafficWisdomAnalysisV2 analysis;

    private SerialPortDownload download;
    private SerialPort mSerialPort;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_traffic_light, container, false);
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
        if (nodeInfo.getNode_num().equals("0033")) {
            tvTrafficLightFlame.setText(nodeInfo.getFrame_num());
            String[] nodes = nodeInfo.getData_analysis().split("--");
            tvTrafficLightGroupId.setText(nodes[1]);
            tvTrafficLightNodeNum.setText(nodes[0]);
            if (nodes.length == 3 && nodes[2] != null) {
                if (nodes[0].equals("0001") && nodes[1].equals("01")) {
                    setLightColor(nodes[2]);
                }
            }
        }
    }

    boolean bFirstUp = false;
    boolean bFirstLeft = false;

    private void setLightColor(String node) {
        if (bRed(node)) {
            if (bFirstUp) {
                tvTrafficLightLeftRed.setVisibility(View.GONE);
                tvTrafficLightLeftYellow.setVisibility(View.VISIBLE);
                tvTrafficLightLeftGreen.setVisibility(View.GONE);

                tvTrafficLightRightRed.setVisibility(View.GONE);
                tvTrafficLightRightYellow.setVisibility(View.VISIBLE);
                tvTrafficLightRightGreen.setVisibility(View.GONE);
                bFirstUp = false;

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            tvTrafficLightUpYellow.setVisibility(View.GONE);
            tvTrafficLightUpGreen.setVisibility(View.GONE);
            tvTrafficLightUpRed.setVisibility(View.VISIBLE);

            tvTrafficLightBelowYellow.setVisibility(View.GONE);
            tvTrafficLightBelowGreen.setVisibility(View.GONE);
            tvTrafficLightBelowRed.setVisibility(View.VISIBLE);

            tvTrafficLightLeftRed.setVisibility(View.GONE);
            tvTrafficLightLeftYellow.setVisibility(View.GONE);
            tvTrafficLightLeftGreen.setVisibility(View.VISIBLE);

            tvTrafficLightRightRed.setVisibility(View.GONE);
            tvTrafficLightRightYellow.setVisibility(View.GONE);
            tvTrafficLightRightGreen.setVisibility(View.VISIBLE);
            bFirstLeft = true;

        } else {

            if (bFirstLeft) {
                tvTrafficLightUpRed.setVisibility(View.GONE);
                tvTrafficLightUpYellow.setVisibility(View.VISIBLE);
                tvTrafficLightUpGreen.setVisibility(View.GONE);

                tvTrafficLightBelowRed.setVisibility(View.GONE);
                tvTrafficLightBelowYellow.setVisibility(View.VISIBLE);
                tvTrafficLightBelowGreen.setVisibility(View.GONE);

                bFirstLeft = false;


                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            tvTrafficLightUpRed.setVisibility(View.GONE);
            tvTrafficLightUpYellow.setVisibility(View.GONE);
            tvTrafficLightUpGreen.setVisibility(View.VISIBLE);

            tvTrafficLightBelowRed.setVisibility(View.GONE);
            tvTrafficLightBelowYellow.setVisibility(View.GONE);
            tvTrafficLightBelowGreen.setVisibility(View.VISIBLE);

            tvTrafficLightLeftRed.setVisibility(View.VISIBLE);
            tvTrafficLightLeftYellow.setVisibility(View.GONE);
            tvTrafficLightLeftGreen.setVisibility(View.GONE);

            tvTrafficLightRightRed.setVisibility(View.VISIBLE);
            tvTrafficLightRightYellow.setVisibility(View.GONE);
            tvTrafficLightRightGreen.setVisibility(View.GONE);

            bFirstUp = true;
        }
    }

    private boolean bRed(String node) {
        return node.equals("红");
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

    @OnClick({R.id.tv_traffic_light_up_red, R.id.tv_traffic_light_up_yellow, R.id.tv_traffic_light_up_green, R.id.tv_traffic_light_left_red, R.id.tv_traffic_light_left_yellow, R.id.tv_traffic_light_left_green, R.id.tv_traffic_light_right_red, R.id.tv_traffic_light_right_yellow, R.id.tv_traffic_light_right_green, R.id.tv_traffic_light_below_red, R.id.tv_traffic_light_below_yellow, R.id.tv_traffic_light_below_green})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_traffic_light_up_red:
                break;
            case R.id.tv_traffic_light_up_yellow:
                break;
            case R.id.tv_traffic_light_up_green:
                break;
            case R.id.tv_traffic_light_left_red:
                break;
            case R.id.tv_traffic_light_left_yellow:
                break;
            case R.id.tv_traffic_light_left_green:
                break;
            case R.id.tv_traffic_light_right_red:
                break;
            case R.id.tv_traffic_light_right_yellow:
                break;
            case R.id.tv_traffic_light_right_green:
                break;
            case R.id.tv_traffic_light_below_red:
                break;
            case R.id.tv_traffic_light_below_yellow:
                break;
            case R.id.tv_traffic_light_below_green:
                break;
        }
    }
}
