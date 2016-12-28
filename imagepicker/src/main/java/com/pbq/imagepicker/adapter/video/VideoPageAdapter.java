package com.pbq.imagepicker.adapter.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pbq.imagepicker.R;
import com.pbq.imagepicker.Utils;
import com.pbq.imagepicker.VideoPicker;
import com.pbq.imagepicker.bean.VideoItem;

import java.util.ArrayList;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class VideoPageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private VideoPicker videoPicker;
    private ArrayList<VideoItem> videos = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    /**
     * 构造函数
     * @param activity
     * @param videos 视频数据源
     */
    public VideoPageAdapter(Activity activity, ArrayList<VideoItem> videos) {
        this.mActivity = activity;
        this.videos = videos;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        videoPicker = VideoPicker.getInstance();
    }

    public void setData(ArrayList<VideoItem> videos) {
        this.videos = videos;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    /**
     * 实例化一个页卡
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view=mActivity.getLayoutInflater().inflate(com.pbq.imagepicker.R.layout.viewpager_video_item,null);
        ImageView imageview = (ImageView) view.findViewById(com.pbq.imagepicker.R.id.imageview);
        ImageButton btnPlay = (ImageButton) view.findViewById(com.pbq.imagepicker.R.id.btn_play);
        final VideoItem videoItem = videos.get(position);
        Glide.with(mActivity)
                .load(videoItem.path)
                .placeholder(R.mipmap.default_image)
                .into(imageview);
        /**
         * 点击播放播放视频
         */
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(videoItem.path);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                mActivity.startActivity(intent);
            }
        });
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.OnPhotoTapListener(view);
            }
        });
        container.addView(view);
        return view;
    }

    /**
     * 返回页卡的数量
     * @return
     */
    @Override
    public int getCount() {
        return videos.size();
    }

    /**
     * view是否来自于对象
     * @param view
     * @param object
     * @return
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * 销毁一个页卡
     * @param container
     * @param position
     * @param object
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view);
    }
}
