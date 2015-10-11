package cn.diviniti.toarunolibris.RecyclerModel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import cn.diviniti.toarunolibris.R;

/**
 * Created by Administrator on 2015/9/24.
 */
public class HotTagAdapter extends RecyclerView.Adapter<HotTagAdapter.HotTagHolder> {

    private LayoutInflater inflater;
    List<HotTag> hotTags = Collections.emptyList();

    public HotTagAdapter(Context context, List<HotTag> hotTags) {
        inflater = LayoutInflater.from(context);
        this.hotTags = hotTags;
    }

    @Override
    public HotTagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_hot_tag, parent, false);
        HotTagHolder holder = new HotTagHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HotTagHolder holder, int position) {
        HotTag currentHotTag = hotTags.get(position);

        holder.hotTag.setText(currentHotTag.tagName);
    }

    @Override
    public int getItemCount() {
        return hotTags.size();
    }

    class HotTagHolder extends RecyclerView.ViewHolder {
        TextView hotTag;

        public HotTagHolder(View itemView) {
            super(itemView);
            hotTag = (TextView) itemView.findViewById(R.id.hot_tag);
        }
    }
}
