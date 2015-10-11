package cn.diviniti.toarunolibris.RecyclerModel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.diviniti.toarunolibris.R;

/**
 * Created by Administrator on 2015/9/26.
 */
public class BookInfoAdapter extends RecyclerView.Adapter<BookInfoAdapter.BookInfoViewHolder> {

    private LayoutInflater inflater;
    List<BookInfo> bookInfos = Collections.emptyList();

    public BookInfoAdapter(Context context, List<BookInfo> bookInfos) {
        inflater = LayoutInflater.from(context);
        this.bookInfos = bookInfos;
    }

    @Override
    public BookInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_book_info, parent, false);
        BookInfoViewHolder holder = new BookInfoViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BookInfoViewHolder holder, int position) {
        BookInfo currentBookInfo = bookInfos.get(position);

        holder.title.setText(currentBookInfo.title);
        holder.subTitle.setText(currentBookInfo.subTitle);
    }

    @Override
    public int getItemCount() {
        return bookInfos.size();
    }

    class BookInfoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subTitle;

        public BookInfoViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.book_info_item_title);
            subTitle = (TextView) itemView.findViewById(R.id.book_info_item_subtitle);
        }
    }
}
