package com.maning.gankmm.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ldoublem.thumbUplib.ThumbUpView;
import com.maning.gankmm.R;
import com.maning.gankmm.bean.GankEntity;
import com.maning.gankmm.db.CollectDao;
import com.maning.gankmm.utils.MySnackbar;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by maning on 16/5/17.
 */
public class RecyclePublicAdapter extends RecyclerView.Adapter<RecyclePublicAdapter.MyViewHolder> {

    private Context context;
    private List<GankEntity> commonDataResults;
    private LayoutInflater layoutInflater;

    public RecyclePublicAdapter(Context context, List<GankEntity> commonDataResults) {
        this.context = context;
        this.commonDataResults = commonDataResults;
        layoutInflater = LayoutInflater.from(this.context);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void updateDatas(List<GankEntity> commonDataResults) {
        this.commonDataResults = commonDataResults;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflate = layoutInflater.inflate(R.layout.item_common, parent, false);

        return new MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, final int position) {

        final GankEntity resultsEntity = commonDataResults.get(position);

        viewHolder.tvShowWho.setText(resultsEntity.getWho());
        viewHolder.tvShowTitle.setText(resultsEntity.getDesc());
        viewHolder.tvShowTime.setText(resultsEntity.getPublishedAt().split("T")[0]);

        //图片展示
        String imageUrl = "";
        List<String> images = resultsEntity.getImages();
        if (images != null && images.size() > 0) {
            imageUrl = images.get(0);
        }
        if (TextUtils.isEmpty(imageUrl)) {
            viewHolder.ivShow.setVisibility(View.GONE);
        } else {
            viewHolder.ivShow.setVisibility(View.VISIBLE);
            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.placeholder(R.drawable.pic_gray_bg);
            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(viewHolder.ivShow);
        }

        //查询是否存在收藏
        boolean isCollect = new CollectDao().queryOneCollectByID(resultsEntity.get_id());
        if (isCollect) {
            viewHolder.btnCollect.setLike();
        } else {
            viewHolder.btnCollect.setUnlike();
        }

        viewHolder.btnCollect.setOnThumbUp(new ThumbUpView.OnThumbUp() {
            @Override
            public void like(boolean like) {
                if (like) {
                    boolean insertResult = new CollectDao().insertOneCollect(resultsEntity);
                    if (insertResult) {
                        MySnackbar.makeSnackBarBlack(viewHolder.tvShowTime, "收藏成功");
                    } else {
                        viewHolder.btnCollect.setUnlike();
                        MySnackbar.makeSnackBarRed(viewHolder.tvShowTime, "收藏失败");
                    }
                } else {
                    boolean deleteResult = new CollectDao().deleteOneCollect(resultsEntity.get_id());
                    if (deleteResult) {
                        MySnackbar.makeSnackBarBlack(viewHolder.tvShowTime, "取消收藏成功");
                    } else {
                        viewHolder.btnCollect.setLike();
                        MySnackbar.makeSnackBarRed(viewHolder.tvShowTime, "取消收藏失败");
                    }
                }
            }
        });

        //如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return commonDataResults.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tvShowWho)
        TextView tvShowWho;
        @Bind(R.id.tvShowTitle)
        TextView tvShowTitle;
        @Bind(R.id.tvShowTime)
        TextView tvShowTime;
        @Bind(R.id.btn_collect)
        ThumbUpView btnCollect;
        @Bind(R.id.iv_show)
        ImageView ivShow;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }
}
