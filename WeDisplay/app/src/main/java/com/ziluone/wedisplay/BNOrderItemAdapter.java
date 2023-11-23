package com.ziluone.wedisplay;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class BNOrderItemAdapter
        extends RecyclerView.Adapter<BNOrderItemAdapter.BNItemHolder> {

    private List<String> orderInfos;
    private Context mContext;
    private ItemClickListener listener;

    public BNOrderItemAdapter(Context context, List<String> orderInfos, ItemClickListener listener) {
        this.mContext = context;
        this.orderInfos = orderInfos;
        this.listener = listener;
    }

    public void setOrderInfos(List<String> orderInfos) {
        this.orderInfos = orderInfos;
    }

    @Override
    public BNItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(mContext).inflate(R.layout.onsdk_ordercenter_item_item, parent, false);
        return new BNItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BNItemHolder holder, int position) {
        holder.update(orderInfos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return orderInfos == null ? 0 : orderInfos.size();
    }

    public class BNItemHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private View lineView;
        private String info;
        private int position;

        public BNItemHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.item_txt);
            lineView = itemView.findViewById(R.id.line_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.click(info, position);
                }
            });
        }

        public void update(String info, int position) {
            this.info = info;
            this.position = position;
            mTextView.setText(info);
            if (position == getItemCount() - 1) {
                lineView.setVisibility(View.GONE);
            } else {
                lineView.setVisibility(View.VISIBLE);
            }
        }
    }

    public static interface ItemClickListener {

        public void click(String info, int position);

    }
}
