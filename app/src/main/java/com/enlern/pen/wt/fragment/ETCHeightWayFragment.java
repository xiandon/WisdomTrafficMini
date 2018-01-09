package com.enlern.pen.wt.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.wt.R;
import com.enlern.pen.wt.activity.TrafficWisdomActivity;
import com.enlern.pen.wt.broadcast.BroadcastMain;
import com.enlern.pen.wt.broadcast.RecCallBack;
import com.pen.wind.dialog.EnterDialog;
import com.pen.wind.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.TrafficWisdomAnalysisV2;
import com.xiandon.wsn.serial.SerialPortDownload;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android_serialport_api.SerialPort;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by pen on 2017/11/30.
 */

public class ETCHeightWayFragment extends BaseFragment {
    Unbinder unbinder;
    @BindView(R.id.tv_high_en_switch_status)
    TextView tvHighEnSwitchStatus;
    @BindView(R.id.tv_high_en_switch_num)
    TextView tvHighEnSwitchNum;
    @BindView(R.id.tv_high_en_node_num)
    TextView tvHighEnNodeNum;
    @BindView(R.id.tv_high_en_node_flame)
    TextView tvHighEnNodeFlame;
    @BindView(R.id.btn_high_ex_open)
    Button btnHighExOpen;
    @BindView(R.id.btn_high_ex_close)
    Button btnHighExClose;
    @BindView(R.id.tv_high_ex_switch_status)
    TextView tvHighExSwitchStatus;
    @BindView(R.id.tv_high_ex_switch_num)
    TextView tvHighExSwitchNum;
    @BindView(R.id.tv_high_ex_node_num)
    TextView tvHighExNodeNum;
    @BindView(R.id.tv_high_ex_node_flame)
    TextView tvHighExNodeFlame;
    @BindView(R.id.tv_way_node_mess)
    TextView tvWayNodeMess;
    private Context context;

    private View view;
    private String TAG = "ETCHeightWayFragment";
    private BroadcastMain main;

    private TrafficWisdomAnalysisV2 analysis;

    private SerialPortDownload download;
    private SerialPort mSerialPort;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_etc_height_way, container, false);
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
        if (nodeInfo.getNode_num().equals("0022")) {
            String[] nodes = nodeInfo.getData_analysis().split("--");
            if (nodes.length == 5) {
                if (nodes[0].equals("00")) {
                    tvHighEnSwitchNum.setText("--");
                    tvWayNodeMess.setText("--");
                } else {
                    tvHighEnSwitchNum.setText(nodes[0]);
                    if (Integer.parseInt(nodes[3], 16) > Integer.parseInt(nodes[2], 16)) {
                        tvWayNodeMess.setText("成功扣款");
                    } else {
                        tvWayNodeMess.setText("扣款失败，余额不足");
                    }
                }
                tvHighEnNodeNum.setText(checkData(nodes[2]));
                tvHighEnNodeFlame.setText(checkData(nodes[3]));
                tvHighEnSwitchStatus.setText(nodes[4]);
            }
        }
    }

    private String checkData(String data) {
        if (data.equals("00")) {
            return "--";
        } else {
            return Integer.parseInt(data, 16) + "";
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

    @OnClick({R.id.btn_high_en_open, R.id.btn_high_en_close, R.id.btn_high_ex_open, R.id.btn_high_ex_close, R.id.tv_high_en_node_num})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_high_en_open:
                String wsn = agreementFinishing((String) SPUtils.get(context, "SAVE_TRAFFIC0022", ""), "01");
                download.DownData(wsn);
                break;
            case R.id.btn_high_en_close:
                String wsn1 = agreementFinishing((String) SPUtils.get(context, "SAVE_TRAFFIC0022", ""), "02");
                download.DownData(wsn1);
                break;
            case R.id.btn_high_ex_open:
                break;
            case R.id.btn_high_ex_close:
                break;
            case R.id.tv_high_en_node_num:
                new EnterDialog(context, R.style.PromptBoxDialog, "请输入此次扣除费用金额（0-9的整数）", new EnterDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm, String editText) {
                        if (confirm) {
                            if (isNumeric(editText)) {
                                String down = "";
                                if (Integer.parseInt(editText) < 10 && Integer.parseInt(editText) >= 0) {
                                    down = "0" + Integer.toHexString(Integer.parseInt(editText));
                                    String wsn4 = agreementFinishing22((String) SPUtils.get(context, "SAVE_TRAFFIC0022", ""), down);
                                    download.DownData(wsn4);
                                } else {
                                    Toast.makeText(context, "请输入0-9的整数", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "请输入整数", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setTitle("温馨提醒").show();
                break;
        }
    }

    private static String agreementFinishing(String wsn, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, wsn.length() - 6) + status + wsn.substring(wsn.length() - 4, wsn.length());
        }
        return wsn;
    }

    private static String agreementFinishing22(String wsn, String status) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, wsn.length() - 10) + status + wsn.substring(wsn.length() - 8, wsn.length());
        }
        return wsn;
    }

    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}
