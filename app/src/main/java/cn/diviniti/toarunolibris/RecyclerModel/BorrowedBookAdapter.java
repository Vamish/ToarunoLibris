package cn.diviniti.toarunolibris.RecyclerModel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import cn.diviniti.toarunolibris.R;

public class BorrowedBookAdapter extends RecyclerView.Adapter<BorrowedBookAdapter.BorrowedBookHolder> {

    private LayoutInflater inflater;
    List<BorrowedBook> books = Collections.emptyList();

    public BorrowedBookAdapter(Context context, List<BorrowedBook> books) {
        inflater = LayoutInflater.from(context);
        this.books = books;
    }

    @Override
    public BorrowedBookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_books_item, parent, false);
        BorrowedBookHolder holder = new BorrowedBookHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BorrowedBookHolder holder, int position) {
        BorrowedBook currentBook = books.get(position);

        holder.bookId.setText(currentBook.bookID);
        holder.bookName.setText(currentBook.bookName);
        holder.returnTime.setText("应还日期:" + currentBook.returnTime);
        holder.generalInfo.setText(currentBook.generalInfo);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BorrowedBookHolder extends RecyclerView.ViewHolder {
        TextView bookId;
        TextView bookName;
        TextView returnTime;
        TextView generalInfo;

        public BorrowedBookHolder(View itemView) {
            super(itemView);
            bookId = (TextView) itemView.findViewById(R.id.book_id);
            bookName = (TextView) itemView.findViewById(R.id.book_name);
            returnTime = (TextView) itemView.findViewById(R.id.book_available);
            generalInfo = (TextView) itemView.findViewById(R.id.book_call_number);
        }
    }

}
