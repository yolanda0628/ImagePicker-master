package com.pbq.imagepicker.adapter.video;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.pbq.imagepicker.R;
import com.pbq.imagepicker.Utils;
import com.pbq.imagepicker.VideoPicker;
import com.pbq.imagepicker.bean.VideoItem;
import com.pbq.imagepicker.ui.image.ImageBaseActivity;
import com.pbq.imagepicker.ui.video.VideoGridActivity;
import com.pbq.imagepicker.view.SuperCheckBox;

import java.util.ArrayList;

/**
 * 加载视频的GridView
 */
public class VideoGridAdapter extends BaseAdapter {

    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机

    private VideoPicker videoPicker;
    private Activity mActivity;
    private ArrayList<VideoItem> videos;       //当前需要显示的所有的视频数据
    private ArrayList<VideoItem> mSelectedVideos; //全局保存的已经选中的视频数据
    private boolean isShowCamera;         //是否显示录像按钮
    private int mVideoSize;               //每个条目的大小
    private OnVideoItemClickListener listener;   //视频被点击的监听

    public VideoGridAdapter(Activity activity, ArrayList<VideoItem> videos) {
        this.mActivity = activity;
        if (videos == null || videos.size() == 0) this.videos = new ArrayList<>();
        else this.videos = videos;

        mVideoSize = Utils.getImageItemWidth(mActivity);
        videoPicker = VideoPicker.getInstance();
        isShowCamera = videoPicker.isShowCamera();
        mSelectedVideos = videoPicker.getSelectedVideos();
    }

    public void refreshData(ArrayList<VideoItem> videos) {
        if (videos == null || videos.size() == 0) this.videos = new ArrayList<>();
        else this.videos = videos;
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return isShowCamera ? videos.size() + 1 : videos.size();
    }

    /**
     * 根据是否显示相机来判断list位置
     * @param position
     * @return
     */
    @Override
    public VideoItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return videos.get(position - 1);
        } else {
            return videos.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        //相机录像
        if (itemViewType == ITEM_TYPE_CAMERA) {
            convertView = LayoutInflater.from(mActivity).inflate(com.pbq.imagepicker.R.layout.adapter_record_item, parent, false);
            convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mVideoSize)); //让视频是个正方形
            convertView.setTag(null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((ImageBaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, VideoGridActivity.REQUEST_PERMISSION_CAMERA);
                    } else {
                        //录像
                        videoPicker.takeRecord(mActivity, VideoPicker.REQUEST_VIDEO_TAKE);
                    }
                }
            });
        } else {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mActivity).inflate(com.pbq.imagepicker.R.layout.adapter_video_item, parent, false);
                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mVideoSize)); //让视频是个正方形
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final VideoItem videoItem = getItem(position);
            /**
             * 点击图片的监听
             */
            holder.ivThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onVideoItemClick(holder.rootView, videoItem, position);
                }
            });

            //显示时长 转化成分秒
            holder.tv_timelong.setText(videoPicker.timeParse(videoItem.timeLong));

            //视频的选择
            holder.cbCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectLimit = videoPicker.getSelectLimit();
                    if (holder.cbCheck.isChecked() && mSelectedVideos.size() >= selectLimit) {
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(com.pbq.imagepicker.R.string.select_video_limit, selectLimit), Toast.LENGTH_SHORT).show();
                        holder.cbCheck.setChecked(false);
                        holder.mask.setVisibility(View.GONE);
                    } else {
                        videoPicker.addSelectedVideoItem(position, videoItem, holder.cbCheck.isChecked());
                        holder.mask.setVisibility(View.VISIBLE);
                    }
                }
            });
            //根据是否多选，显示或隐藏checkbox
            if (videoPicker.isMultiMode()) {
                holder.cbCheck.setVisibility(View.VISIBLE);
                boolean checked = mSelectedVideos.contains(videoItem);
                /**
                 * 判断该项视频是否选中,如果选中,将背景显示出来,将多选框选中状态
                 * 否则隐藏背景,多选框未选中状态
                 */
                if (checked) {
                    holder.mask.setVisibility(View.VISIBLE);
                    holder.cbCheck.setChecked(true);
                } else {
                    holder.mask.setVisibility(View.GONE);
                    holder.cbCheck.setChecked(false);
                }
            } else {
                //单选的话直接隐藏多选框
                holder.cbCheck.setVisibility(View.GONE);
            }

            if(videoPicker.isSelect(videoItem)){
                holder.mask.setVisibility(View.VISIBLE);
                holder.cbCheck.setChecked(true);
            }
            Glide.with(mActivity).load(videoItem.path).placeholder(R.mipmap.default_image).into(holder.ivThumb);
//            videoPicker.getVideoLoader().displayImage(mActivity, videoItem.path, holder.ivThumb, mVideoSize, mVideoSize); //显示视频
        }
        return convertView;
    }

    private class ViewHolder {
        public View rootView;
        public ImageView ivThumb;
        public View mask;
        public SuperCheckBox cbCheck;
        public TextView tv_timelong;

        public ViewHolder(View view) {
            rootView = view;
            ivThumb = (ImageView) view.findViewById(com.pbq.imagepicker.R.id.iv_thumb);
            mask = view.findViewById(com.pbq.imagepicker.R.id.mask);
            cbCheck = (SuperCheckBox) view.findViewById(com.pbq.imagepicker.R.id.cb_check);
            tv_timelong = (TextView) view.findViewById(com.pbq.imagepicker.R.id.tv_timelong);
        }
    }

    public void setOnVideoItemClickListener(OnVideoItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 定义一个监听视频被点击的接口
     */
    public interface OnVideoItemClickListener {
        void onVideoItemClick(View view, VideoItem videoItem, int position);
    }
}