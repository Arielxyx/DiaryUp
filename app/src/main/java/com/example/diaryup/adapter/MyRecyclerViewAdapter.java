package com.example.diaryup.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diaryup.R;
import com.example.diaryup.bean.Diary;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    int position;
    private View view;
    private List<Diary> diaryList;
    private Context context;
    private Diary diary;

    public MyRecyclerViewAdapter(Context context, List<Diary> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if(diaryList==null)
            return 0;
        return diaryList.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        diary = diaryList.get(position);
        if(diary!=null){
            if(diary.isDataType()){//有设置提示
                holder.im_dataTime.setVisibility(View.VISIBLE);
            }else{
                holder.im_dataTime.setVisibility(View.GONE);
            }
            if(diary.isLockType()){ //有锁状态
//                Log.d("ddd","有锁");
                holder.noteTitleText.setVisibility(View.GONE);
                holder.ivImage.setVisibility(View.VISIBLE);
                holder.ivImage.setBackgroundResource(R.drawable.lock_bg);
            }else{
//                Log.d("ddd","没锁");
                holder.ivImage.setVisibility(View.INVISIBLE);
                holder.noteTimeText.setText(diary.getTime());
                holder.noteTitleText.setText(diary.getTitle());
            }
//            holder.photoImage.setImageURI(Uri.parse(club.getPhoto()));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteTimeText;
        TextView noteTitleText;
        ImageView ivImage,im_dataTime;

        public ViewHolder(View view) {
            super(view);

            noteTimeText = (TextView)view.findViewById(R.id.tv_note_time);
            noteTitleText = (TextView)view.findViewById(R.id.tv_note_title);
            ivImage = (ImageView)view.findViewById(R.id.iv_image);
            im_dataTime = (ImageView)view.findViewById(R.id.im_data_time);
//            final ViewHolder holder = new ViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
//                    Club club = mClubList.get(position);
//                    Toast.makeText(v.getContext(),"you clicked view "+club.getClubName(),Toast.LENGTH_LONG).show();
//                    //此处回传点击监听事件
                    if(onItemClickListener!=null){
                        onItemClickListener.OnItemClick(v, diaryList.get(getLayoutPosition()));
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    position = getAdapterPosition();
                    if(onItemClickListener!=null){
                        onItemLongClickListener.OnItemLongClick(v,diaryList.get(getAdapterPosition()));
                    }
                    return true;
                }
            });
        }
    }

    //  删除数据
    public void removeData() {
        diaryList.remove(position);
        //删除动画
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    // 修改数据
    public void changeData(int clubId) {
        diary = DataSupport.find(Diary.class,clubId);
        diaryList.set(position,diary);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    /**
     * 设置点击item的监听事件的接口
     */
    public interface OnItemClickListener {
        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param data 点击得到的数据
         */
        public void OnItemClick(View view, Diary data);
    }
    //需要外部访问，所以需要设置set方法，方便调用
    private OnItemClickListener onItemClickListener;
    /**
     * 设置RecyclerView某个的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 设置长按item的监听事件接口
     */
    public interface OnItemLongClickListener {
        public void OnItemLongClick(View view, Diary data);
    }
    private OnItemLongClickListener onItemLongClickListener;
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
}
