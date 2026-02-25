package io.github.yuzhiang.qxb.model;

import lombok.Data;

@Data
public class LearnProfile {
    public long id;//": 320160936051,
    public int score;//": -922.0,//学习净剩余，实际学习时间 - （计划学习时间-实际学习时间）
    public float totalTime;//": 2166.110940000,//实际学习时间：计划学习时间-实际学习时间
    public int totalNumber;//": 26,学习次数
    public int succeed;//": 21,成功次数
    public int upVote;//": 1,//被点赞次数
    public float achievementRatio;//成功率": 0.8076923076923077

}
