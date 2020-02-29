package com.example.diaryup.bean;

import org.litepal.crud.DataSupport;

public class Diary extends DataSupport {

    private int id; //主键自增id
    private String title; //日记标题
    private String context; //日记内容
    private String[] images; //存储照片
    private String time; //日记记录时间
    private boolean dataType; //是否设置了到时提醒0代表未提醒
    private String dataTime; //提醒时间
    private boolean lockType; //是否添加了密码锁0代表未添加
    private String lock; //密码锁密码

    public Diary() {
    }

    public Diary(String title, String context, String[] images, String time, boolean dataType, String dataTime, boolean lockType, String lock) {
        this.title = title;
        this.context = context;
        this.images = images;
        this.time = time;
        this.dataType = dataType;
        this.dataTime = dataTime;
        this.lockType = lockType;
        this.lock = lock;
    }

    public Diary(int id, String title, String context, String[] images, String time, boolean dataType, String dataTime, boolean lockType, String lock) {
        this.id = id;
        this.title = title;
        this.context = context;
        this.images = images;
        this.time = time;
        this.dataType = dataType;
        this.dataTime = dataTime;
        this.lockType = lockType;
        this.lock = lock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDataType() {
        return dataType;
    }

    public void setDataType(boolean dataType) {
        this.dataType = dataType;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public boolean isLockType() {
        return lockType;
    }

    public void setLockType(boolean lockType) {
        this.lockType = lockType;
    }

    public String getLock() {
        return lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }
}
