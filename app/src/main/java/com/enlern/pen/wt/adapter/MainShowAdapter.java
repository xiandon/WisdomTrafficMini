package com.enlern.pen.wt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enlern.pen.wt.R;
import com.pen.wind.log.DonLogger;
import com.xiandon.wsn.node.NodeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pen on 2017/11/30.
 */

public class MainShowAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<NodeInfo> infoList = new ArrayList<>();
    private LayoutInflater inflater;

    public MainShowAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_main_show_item, parent, false);
        RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) {
        };
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView tv_show_num = holder.itemView.findViewById(R.id.tv_main_show_num);
        TextView tv_show_name = holder.itemView.findViewById(R.id.tv_main_show_cn_name);
        TextView tv_show_data_analysis = holder.itemView.findViewById(R.id.tv_main_show_data_analysis);
        TextView tv_show_data = holder.itemView.findViewById(R.id.tv_main_show_data);
        TextView tv_show_wsn = holder.itemView.findViewById(R.id.tv_main_show_wsn);


        tv_show_num.setText(infoList.get(position).getNode_num());
        tv_show_name.setText(infoList.get(position).getNode_name());
        tv_show_data_analysis.setText(infoList.get(position).getData_analysis());
        tv_show_data.setText(infoList.get(position).getNode_data());
        tv_show_wsn.setText(infoList.get(position).getWsn());
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public void addData(int count, NodeInfo nodeInfo) {
        infoList.add(count, nodeInfo);
        notifyItemInserted(count);
        notifyDataSetChanged();

        if (infoList.size() > 100) {
            clearData();
        }
    }

    public void clearData() {
        infoList.clear();
        notifyDataSetChanged();
    }

}
