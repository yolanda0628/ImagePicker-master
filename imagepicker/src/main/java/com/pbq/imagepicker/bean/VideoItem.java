package com.pbq.imagepicker.bean;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：视频信息
 * 修订历史：
 * ================================================
 */
public class VideoItem implements Serializable {

    public String name;       //视频的名字
    public String path;       //视频的路径
    public long size;         //视频的大小
    public int width;         //视频的宽度
    public int height;        //视频的高度
    public String mimeType;   //视频的类型
    public long addTime;      //视频的创建时间
    public long timeLong;      //视频的时长

    /** 视频的路径和创建时间相同就认为是同一个视频 */
    @Override
    public boolean equals(Object o) {
        try {
            VideoItem other = (VideoItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.addTime == other.addTime;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
