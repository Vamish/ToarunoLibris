package cn.diviniti.toarunolibris.RecyclerModel;

/**
 * Created by vango on 15/11/17.
 */
public class BorrowedBook {
    public String bookID;
    public String bookName;
    public String returnTime;
    //注意，这个generalInfo 在 超期 里面存储馆藏地，在 已借 里面存储作者
    public String generalInfo;
}
