package cn.diviniti.toarunolibris.RecyclerModel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.diviniti.toarunolibris.R;

/**
 * Created by Administrator on 2015/9/23.
 */
public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ListViewHolder> {

    private LayoutInflater inflater;
    List<DrawerList> item = Collections.emptyList();

    public DrawerListAdapter(Context context, List<DrawerList> item) {
        inflater = LayoutInflater.from(context);
        this.item = item;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_drawer_list, parent, false);   //  view为列表子项
        ListViewHolder holder = new ListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        DrawerList currentItem = item.get(position);

        holder.title.setText(currentItem.title);
        holder.icon.setImageResource(currentItem.iconId);
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public ListViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.drawer_item_icon);
            title = (TextView) itemView.findViewById(R.id.drawer_item_title);
        }
    }
}
