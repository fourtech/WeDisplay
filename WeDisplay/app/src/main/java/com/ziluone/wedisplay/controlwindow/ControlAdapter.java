package com.ziluone.wedisplay.controlwindow;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ziluone.wedisplay.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControlAdapter extends RecyclerView.Adapter<ControlAdapter.ControlHolder> {

    List<ControlBoardWindow.Twotxt> txtlist = new ArrayList<>();
    List<ControlBoardWindow.Twotxt> topTxts = new ArrayList<>();

    public ControlAdapter(List<ControlBoardWindow.Twotxt> txtlist) {
        this.txtlist = txtlist;
    }

    public void showTop(Map<String,String> topTxts) {
        this.topTxts.clear();
        for (Map.Entry<String, String> entry : topTxts.entrySet()) {
            this.topTxts.add(new ControlBoardWindow.Twotxt(entry.getKey(), entry.getValue()));
        }
        notifyDataSetChanged();
    }

    @Override
    public ControlHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ControlHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.control_itemview, parent, false));
    }

    @Override
    public void onBindViewHolder(ControlHolder holder, int position) {
        // Log.e("ControlBoardWindow","item set");
        if (txtlist.size() > position) {
            holder.txt1.setText(txtlist.get(position).getTxt1());
            holder.txt2.setText(txtlist.get(position).getTxt2());
        } else {
            holder.txt1.setText(topTxts.get(position - txtlist.size()).getTxt1());
            holder.txt2.setText(topTxts.get(position - txtlist.size()).getTxt2());
        }

    }

    @Override
    public int getItemCount() {
        return txtlist.size() + topTxts.size();
    }

    public static class ControlHolder extends RecyclerView.ViewHolder {
        public TextView txt1;
        public TextView txt2;

        public ControlHolder(View v) {
            super(v);
            txt1 = v.findViewById(R.id.txt1);
            txt2 = v.findViewById(R.id.txt2);
        }
    }
}
