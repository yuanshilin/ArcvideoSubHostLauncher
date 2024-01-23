package com.arcvideo.arcvideosubhostlauncher;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppRecycleViewAdapter extends RecyclerView.Adapter<AppRecycleViewAdapter.ViewHolder> {
    private List<AppInfo> list;

    public AppRecycleViewAdapter(List<AppInfo> list) {
        this.list = list;
    }

    private OnItemClickListener mOnItemClickListener;

    //添加一个OnItemClickListener接口，并且定义两个方法
    public interface OnItemClickListener{
        void onClick(int position);
    }

    //然后定义一个监听的方法，便于主类调用
    public void setOnItemListener(OnItemClickListener onItemListener){
        this.mOnItemClickListener = onItemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.app_item, null);
        // 该方案会导致各个元素间隙过大，操作不紧凑
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item2, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = list.get(position);
        holder.icon.setImageDrawable(appInfo.getIcon());
        holder.label.setText(appInfo.getmLable());
        final int temp_position = position;

        //在此方法中来添加监听，给 icon 控件添加监听并设置上面自定义接口中的方法，在这只是获取位置。
        if(mOnItemClickListener!=null){
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(temp_position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView label;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            label = itemView.findViewById(R.id.label);
        }
    }
}
