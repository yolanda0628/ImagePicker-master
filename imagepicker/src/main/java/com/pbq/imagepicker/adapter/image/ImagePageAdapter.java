package com.pbq.imagepicker.adapter.image;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.pbq.imagepicker.ImagePicker;
import com.pbq.imagepicker.Utils;
import com.pbq.imagepicker.bean.ImageItem;
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
public class ImagePageAdapter extends PagerAdapter {

    private int screenWidth;
    private int screenHeight;
    private ImagePicker imagePicker;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private Activity mActivity;
    public PhotoViewClickListener listener;

    public ImagePageAdapter(Activity activity, ArrayList<ImageItem> images) {
        this.mActivity = activity;
        this.images = images;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        imagePicker = ImagePicker.getInstance();
    }

    public void setData(ArrayList<ImageItem> images) {
        this.images = images;
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
//        PhotoView photoView = new PhotoView(mActivity);
//        ImageItem imageItem = images.get(position);
//        imagePicker.getImageLoader().displayImage(mActivity, imageItem.path, photoView, screenWidth, screenHeight);
//        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
//            @Override
//            public void onPhotoTap(View view, float x, float y) {
//                if (listener != null) listener.OnPhotoTapListener(view, x, y);
//            }
//        });
//        container.addView(photoView);
//        return photoView;

        ImageView photoView = new ImageView(mActivity);
        ImageItem imageItem = images.get(position);
        imagePicker.getImageLoader().displayImage(mActivity, imageItem.path, photoView, screenWidth, screenHeight);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.OnPhotoTapListener(view);
            }
        });
        container.addView(photoView);
        return photoView;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

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
