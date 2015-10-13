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

public class BookSummaryAdapter extends RecyclerView.Adapter<BookSummaryAdapter.BooksViewHolder> {

    private LayoutInflater inflater;
    List<BookSummary> book = Collections.emptyList();

    public BookSummaryAdapter(Context context, List<BookSummary> book) {
        inflater = LayoutInflater.from(context);
        this.book = book;
    }


    @Override
    public BooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_books_item, parent, false);
        BooksViewHolder holder = new BooksViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BooksViewHolder holder, int position) {
        BookSummary currentBook = book.get(position);

        holder.bookID.setText(currentBook.bookID);
        holder.bookName.setText(currentBook.bookName);
        holder.bookCallNumber.setText(currentBook.bookCallNumber);
        holder.bookAuthor.setText(currentBook.bookAuthor);
        holder.bookPublisher.setText(currentBook.bookPublisher);
        holder.bookAvailable.setText("可借: " + currentBook.bookAvailable);
    }

    @Override
    public int getItemCount() {
        return book.size();
    }

    class BooksViewHolder extends RecyclerView.ViewHolder {
        TextView bookID;
        TextView bookName;
        TextView bookCallNumber;
        TextView bookAuthor;
        TextView bookPublisher;
        TextView bookAvailable;

        public BooksViewHolder(View itemView) {
            super(itemView);
            bookID = (TextView) itemView.findViewById(R.id.book_id);
            bookName = (TextView) itemView.findViewById(R.id.book_name);
            bookCallNumber = (TextView) itemView.findViewById(R.id.book_call_number);
            bookAuthor = (TextView) itemView.findViewById(R.id.book_author);
            bookPublisher = (TextView) itemView.findViewById(R.id.book_publisher);
            bookAvailable = (TextView) itemView.findViewById(R.id.book_available);
        }
    }
}
