package com.pbq.imagepicker.ui.video;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.pbq.imagepicker.R;
import com.pbq.imagepicker.VideoDataSource;
import com.pbq.imagepicker.VideoPicker;
import com.pbq.imagepicker.adapter.video.VideoFolderAdapter;
import com.pbq.imagepicker.adapter.video.VideoGridAdapter;
import com.pbq.imagepicker.bean.VideoFolder;
import com.pbq.imagepicker.bean.VideoItem;
import com.pbq.imagepicker.ui.image.ImageBaseActivity;
import com.pbq.imagepicker.view.FolderPopUpWindow;
import java.util.List;

/**
 * 视频的加载界面   注意 视频选择不需要剪裁
 */
public class VideoGridActivity extends ImageBaseActivity implements VideoDataSource.OnVideosLoadedListener, VideoGridAdapter.OnVideoItemClickListener, VideoPicker.OnVideoSelectedListener, View.OnClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

    private VideoPicker videoPicker;

    private boolean isOrigin = false;  //是否选中原图
    private GridView mGridView;  //视频展示控件
    private View mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private Button mBtnDir;      //文件夹切换按钮
    private Button mBtnPre;      //预览按钮
    private VideoFolderAdapter mVideoFolderAdapter;    //视频文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //VideoSet的PopupWindow
    private List<VideoFolder> mVideoFolders;   //所有的视频文件夹
    private VideoGridAdapter mVideoGridAdapter;  //视频九宫格展示的适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_grid);

        videoPicker = VideoPicker.getInstance();
        videoPicker.clear();
        //视频加载完成是回调该接口
        videoPicker.addOnVideoSelectedListener(this);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnDir = (Button) findViewById(R.id.btn_dir);
        mBtnDir.setOnClickListener(this);
        mBtnPre = (Button) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mFooterBar = findViewById(R.id.footer_bar);
        if (videoPicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

        mVideoGridAdapter = new VideoGridAdapter(this, null);
        mVideoFolderAdapter = new VideoFolderAdapter(this, null);

        onVideoSelected(0, null, false);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new VideoDataSource(this, null, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        }
    }

    /**
     * 6.0以上权限检查
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new VideoDataSource(this, null, this);
            } else {
                showToast("权限被禁止，无法选择本地视频");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //录像
                videoPicker.takeRecord(this, VideoPicker.REQUEST_VIDEO_TAKE);
            } else {
                showToast("权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onDestroy() {
        videoPicker.removeOnVideoSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        //点击完成
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(VideoPicker.EXTRA_RESULT_VIDEO_ITEMS, videoPicker.getSelectedVideos());
            setResult(VideoPicker.RESULT_VIDEO_ITEMS, intent);  //返回数据
            finish();
        } else if (id == R.id.btn_dir) {
            //点击全部视频
            if (mVideoFolders == null) {
                Toast.makeText(VideoGridActivity.this,"您的手机没有视频",Toast.LENGTH_SHORT).show();
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mVideoFolderAdapter.refreshData(mVideoFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mVideoFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            /**
             * 点击预览的事件处理  在onActivityResult中回调处理
             */
            Intent intent = new Intent(VideoGridActivity.this, VideoPreviewActivity.class);
            intent.putExtra(VideoPicker.EXTRA_SELECTED_VIDEO_POSITION, 0);
            intent.putExtra(VideoPicker.EXTRA_VIDEO_ITEMS, videoPicker.getSelectedVideos());
            intent.putExtra(VideoPreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, VideoPicker.REQUEST_VIDEO_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish();
        }
    }

    /** 创建弹出的ListView */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mVideoFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mVideoFolderAdapter.setSelectIndex(position);
                videoPicker.setCurrentVideoFolderPosition(position);
                mFolderPopupWindow.dismiss();
                VideoFolder videoFolder = (VideoFolder) adapterView.getAdapter().getItem(position);
                if (null != videoFolder) {
                    mVideoGridAdapter.refreshData(videoFolder.videos);
                    mBtnDir.setText(videoFolder.name);
                }
                mGridView.smoothScrollToPosition(0);//滑动到顶部
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onVideosLoaded(List<VideoFolder> videoFolders) {
        this.mVideoFolders = videoFolders;
        videoPicker.setVideoFolders(videoFolders);
        if (videoFolders.size() == 0) mVideoGridAdapter.refreshData(null);
        else mVideoGridAdapter.refreshData(videoFolders.get(0).videos);
        //视频点击的监听事件
        mVideoGridAdapter.setOnVideoItemClickListener(this);
        mGridView.setAdapter(mVideoGridAdapter);
        mVideoFolderAdapter.refreshData(videoFolders);
    }

    /**
     * 点击grid每一项的事件回调
     * @param view
     * @param videoItem
     * @param position
     */
    @Override
    public void onVideoItemClick(View view, VideoItem videoItem, int position) {
        //根据是否有相机按钮确定位置
        position = videoPicker.isShowCamera() ? position - 1 : position;
        //多选视屏
        if (videoPicker.isMultiMode()) {
            Intent intent = new Intent(VideoGridActivity.this, VideoPreviewActivity.class);
            intent.putExtra(VideoPicker.EXTRA_SELECTED_VIDEO_POSITION, position);
            intent.putExtra(VideoPicker.EXTRA_VIDEO_ITEMS, videoPicker.getCurrentVideoFolderItems());
            intent.putExtra(VideoPreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, VideoPicker.REQUEST_VIDEO_PREVIEW);  //如果是多选，点击视频进入预览界面
        } else {
            //单选视频 直接返回选中的视频
            videoPicker.clearSelectedVideos();
            videoPicker.addSelectedVideoItem(position, videoPicker.getCurrentVideoFolderItems().get(position), true);
            Intent intent = new Intent();
            intent.putExtra(VideoPicker.EXTRA_RESULT_VIDEO_ITEMS, videoPicker.getSelectedVideos());
            setResult(VideoPicker.RESULT_VIDEO_ITEMS, intent);
            finish();
        }
    }

    /**
     * 视频选中的监听
     * 视频添加成功后，修改当前视频的选中数量
     * 当调用 addSelectedVideoItem 或 deleteSelectedVideoItem 都会触发当前回调
     * @param position
     * @param item
     * @param isAdd
     */
    @Override
    public void onVideoSelected(int position, VideoItem item, boolean isAdd) {
        if (videoPicker.getSelectVideoCount() > 0) {
            mBtnOk.setText(getString(R.string.select_complete, videoPicker.getSelectVideoCount(), videoPicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
        } else {
            mBtnOk.setText(getString(R.string.complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
        }
        mBtnPre.setText(getResources().getString(R.string.preview_count, videoPicker.getSelectVideoCount()));
        mVideoGridAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            //如果从预览界面返回  判断是否在预览界面选择了原图
            if (resultCode == VideoPicker.RESULT_VIDEO_BACK) {
                //从预览界面接收是否选择原图
                isOrigin = data.getBooleanExtra(VideoPreviewActivity.ISORIGIN, false);
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_ITEMS) == null) {
                    //什么都不做
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以  应该是从录像界面返回的视频数据  还未作处理
                    setResult(VideoPicker.RESULT_VIDEO_ITEMS, data);
                    finish();
                }
            }
        } else {
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if (resultCode == RESULT_OK && requestCode == VideoPicker.REQUEST_VIDEO_TAKE) {
                //发送广播通知视频增加了
                VideoPicker.galleryAddPic(this, videoPicker.getTakeVideoFile());
                VideoItem videoItem = new VideoItem();
                videoItem.path = videoPicker.getTakeVideoFile().getAbsolutePath();
                videoPicker.clearSelectedVideos();
                videoPicker.addSelectedVideoItem(0, videoItem, true);
                Intent intent = new Intent();
                intent.putExtra(VideoPicker.EXTRA_RESULT_VIDEO_ITEMS, videoPicker.getSelectedVideos());
                setResult(VideoPicker.RESULT_VIDEO_ITEMS, intent);   //单选不需要裁剪，返回数据
                finish();
            }
        }
    }
}