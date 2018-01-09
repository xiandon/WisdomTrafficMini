package com.enlern.pen.wt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enlern.pen.wt.R;
import com.pen.wind.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.serial.SerialPortDownload;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android_serialport_api.SerialPort;

import static android.content.ContentValues.TAG;

/**
 * Created by pen on 2017/12/1.
 */

public class BusStopCarAdapter extends RecyclerView.Adapter {

    private Context context;
    private LayoutInflater inflater;

    private List<NodeInfo> infoList = new ArrayList<>();

    private SerialPortDownload download;
    private SerialPort mSerialPort;


    public BusStopCarAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        download = new SerialPortDownload();
        String PATH = (String) SPUtils.get(context, "PATH", "/dev/ttyUSB0");
        String RATE = (String) SPUtils.get(context, "RATE", "9600");
        try {
            mSerialPort = download.open(PATH, RATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_bus_stop_recycler, parent, false);
        RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {
        };
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TextView tv_bus_stop_show_where = holder.itemView.findViewById(R.id.tv_bus_stop_show_where);
        TextView tv_bus_stop_show_num = holder.itemView.findViewById(R.id.tv_bus_stop_show_num);
        TextView tv_bus_stop_show_plate = holder.itemView.findViewById(R.id.tv_bus_stop_show_plate);
        TextView tv_bus_stop_show_type = holder.itemView.findViewById(R.id.tv_bus_stop_show_type);
        TextView tv_bus_stop_show_time = holder.itemView.findViewById(R.id.tv_bus_stop_show_time);

        String[] nodes = infoList.get(position).getData_analysis().split("--");
        if (nodes.length == 3) {
            tv_bus_stop_show_plate.setText(nodes[0]);
            tv_bus_stop_show_type.setText(nodes[1]);
            tv_bus_stop_show_where.setText(nodes[2]);
            tv_bus_stop_show_time.setText(getNow());
            if (nodes[2] == "" || nodes == null) {
                tv_bus_stop_show_num.setText("--");
            } else {
                tv_bus_stop_show_num.setText("已到站");
                String wsn = busStop((String) SPUtils.get(context, "SAVE_TRAFFIC00dd", ""), nodes[0]);
                download.DownData(wsn);


            }
        }
    }

    private String busStop(String wsn, String s) {
        if (wsn.length() > 30) {
            wsn = "36" + wsn.substring(2, 28) + stringToHexString(s) + wsn.substring(32, wsn.length());
        }
        return wsn;
    }

    private String getNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str;
    }


    @Override
    public int getItemCount() {
        return infoList.size();
    }


    public void addData(int count, NodeInfo nodeInfo) {
        String data = nodeInfo.getNode_data();
        boolean b = data.substring(data.length() - 8, data.length()).equals("00000000");
        if (!b) {
            infoList.add(count, nodeInfo);
            notifyItemInserted(count);
            notifyDataSetChanged();
        }
        if (infoList.size() > 15) {
            clearData();
        }

    }

    public void clearData() {
        infoList.clear();
        notifyDataSetChanged();
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
}
