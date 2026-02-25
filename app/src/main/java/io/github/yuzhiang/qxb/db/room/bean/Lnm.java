package io.github.yuzhiang.qxb.db.room.bean;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "StudyMode")
public class Lnm {

    @PrimaryKey()
    public int id;

    @ColumnInfo(name = "startTime")
    public Date createdDate; //开始学习的时间

    @ColumnInfo(name = "planTime")
    public Date schedule; //计划结束学习的时间

    @ColumnInfo(name = "stopTime")
    public Date endTime; //实际结束学习的时间

    @ColumnInfo(name = "successLearn")
    public boolean finish; //是否成功完成计划


}
