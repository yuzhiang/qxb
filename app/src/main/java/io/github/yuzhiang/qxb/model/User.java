package io.github.yuzhiang.qxb.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//生成get、set方法
@Data
//生成空参构造方法
@NoArgsConstructor
//生成全参构造方法
@AllArgsConstructor
public class User implements Parcelable {
//create不能null，密码要加密

    public Long id = -1L;//": 用户id,
    public Long userPublicId = -1L;//": 用户id，和上面的一样,
    public String userName = "";//": "yuh2016",
    public String name = "";//": "yuh2016",

    public String signature = "每天都要好好的，加油！";//": "yuh2016",
    public String nickName = "无昵称";//": "yuh",
    public String anonymousNickname = "无昵称";//": "匿名昵称",
    public String avatar = "";//": "",
    public String mail = "";//": "邮箱",

    public String college = "";//": "物理科学与技术学院",
    public String grade = "";//": "2016级",
    public String xh = ""; //"学号",
    public int sex = 2; //"1男  0女  2未知",

    public int version = 0;//": 1修改密码，2手动认证，3网页认证,


    public String vipDate = "";//": "免广告时间",
    public String createdDate = "";//": null,
    public String lastModifiedDate = "";//": null,
    public String type = "STUDENT";//": STUDENT,
    public List<Map<String, String>> authorities = new ArrayList<>();//": [],

    public boolean bindQQ = false;//": 绑定QQ,
    public boolean bindWX = false;//": 绑定wx,

    //谁邀请的我
    private long inviteUserId = -1L;
    //    我的邀请码是多少
    private String inviteMyCode = "首页下滑刷新";
    //    积分数
    public int point = 0;

    //    经验值，确定等级，额外权限
    private int jyz = 0;
    //    早起天数
    public int getUpDay = 0;

    public User(Long id) {
        super(); //step 1
        this.id = id;

    }

    public User(long id, String userName, String name, String college) {
        super(); //step 1
        //step 3

        this.id = id;
        this.userName = userName;
        this.college = college;

    }

    public User(Long id, String userName, String name, String nickName, String signature,
                String grade, String college, String type, String createdDate) {
        super(); //step 1

        this.id = id;
        this.userName = userName;
        this.createdDate = createdDate;
        this.nickName = nickName;
        this.signature = signature;
        this.college = college;
        this.grade = grade;
        this.type = type;

    }


    public User(Long id, String xh, String avatar) {
        this.id = id;
        this.xh = xh;
        this.avatar = avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.xh);
        dest.writeString(this.userName);
        dest.writeString(this.signature);
        dest.writeString(this.nickName);
        dest.writeString(this.avatar);
        dest.writeString(this.mail);
        dest.writeString(this.college);
        dest.writeString(this.grade);
        dest.writeString(this.createdDate);
        dest.writeString(this.type);
        dest.writeList(this.authorities);
    }

    protected User(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.xh = (String) in.readValue(Long.class.getClassLoader());
        this.userName = in.readString();
        this.signature = in.readString();
        this.nickName = in.readString();
        this.avatar = in.readString();
        this.mail = in.readString();
        this.college = in.readString();
        this.grade = in.readString();
        this.createdDate = in.readString();
        this.type = in.readString();
        this.authorities = new ArrayList<>();
        in.readList(this.authorities, Map.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

}

