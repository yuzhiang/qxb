package io.github.yuzhiang.qxb.common;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;

import io.github.yuzhiang.qxb.BuildConfig;

public class LdrConfig {

    public static final int ForumCommentNum = 5;

    public static final int maxBlock = 20;
    public static final int maxStar = 80;


    public static final String ncHi = "hi";//留言
    public static final String ncReport = "report";//举报
    public static final String ncTeacher = "teacher";//老师的消息
    public static final String ncPost = "social";//论坛
    public static final String ncLost = "lost";//老师的消息
    public static final String NOTICE_CHANNEL_ADMIN = "admin";//老师的消息
    public static final String ncNewLZUer = "newLZUer";//新生审核信息
    public static final String NOTICE_CHANNEL_CHECK_XYK = "check_xyk";//校园卡审核信息
    public static final String NOTICE_CHANNEL_ADMIN_SH = "admin_sh";

    public static final String FORUM_POSTS = "posts";//新生审核信息
    public static final String FORUM_COMMENTS = "comments";//新生审核信息
    public static final String FORUM_REPLIES = "replies";//新生审核信息


    public static final String head1 = "Transfer-Encrypt";
    public static final String head2 = "Appid";
    public static final String head3 = "User-Agent:";
    public static final String head4 = "Host";
    public static final String head5 = "Connection";
    public static final String head6 = "Accept-Encoding";
    public static final String head7 = "Content-Type";
    public static final String ldrKey = "QSfg26hg433BV42a";


    public static final String head1V = "true";
    //    public static final String head2V = "Appid";
//    public static final String head3V = getUsrAgent();
    public static final String head4V = "gateway.lzu.edu.cn:9000";
    public static final String head5V = "Keep-Alive";
    public static final String head6V = "gzip";
    public static final String head7V = "application/x-www-form-urlencoded";


    public static final String rc_ldr_admin = "ldr_admin";//管理员频道，接收管理员的消息
    public static final String noticeNum = "noticeNum";//管理员频道，接收管理员的消息

    public static final int getUpStart = 5;
    public static final int getUpEnd = 9;

    public static final int mainCourse = 9;
    public static final int mainSchoolBus = 10;
    public static final int mainMail = 11;
    public static final int mainNews = 13;
    public static final int mainFee = 14;
    public static final int mainMyMsg = 15;
    public static final int mainGetupAvatar = 16;
    public static final int mainExam = 17;

    public static final int noticeSize = 20;
    public static final int noticeSizeMin = 10;


    public static final int simForum = 2;

    public static final String spPath = BuildConfig.APPLICATION_ID + "_preferences";


    public static final int calLzuNew = 0;
    public static final int calLzuOld = 1;
    public static final int calInter = 2;


    public static final int onTime = 11;
    //随机点名，选中了，也到了
    public static final int onTimeAuto = 12;
    //随机点名，没选中这个同学，认为他到了
    public static final int offTime = 10;
    //随机点名，选中了，但是缺课
    public static final int noSelect = 13;
    //当时这个人还没选课，所以没被点名


    public static final int jumpMain = 0;
    //点击桌面小部件跳转到课表界面


    public static String ldr_user_agent = "";

    public static String getLdrUsrAgent() {
        if (ldr_user_agent.length() == 0) {
            ldr_user_agent = "ldr/" + AppUtils.getAppVersionName() + "." + AppUtils.getAppVersionCode() +
                    " (Linux; Android " + DeviceUtils.getSDKVersionName() + "; " + DeviceUtils.getManufacturer() + " " + DeviceUtils.getModel() + ")";
        }
        return ldr_user_agent;
    }

    public static final String[] mail_type = {"INBOX", "Junk E-mail", "Sent Items"};

    public static final String lnmLogsTitle = "长按清除日志\n可以将被错误拦截的app加入白名单，或者在 首页 - 我的 - 关于我们 - 找到我们，联系开发者适配该机型\n\n";

    public static String error_empty_ = "java.lang.NullPointerException: Null is not a valid element";
    public static String error_empty_2 = "No class com.forgqi.resourcebaseserver.entity.studymode.StudyMode entity with id";
    public static String error_empty_3 = "SON forbids NaN and infinities";
    public static String error_empty = "查无结果！";
    public static String error_net_timeout_ = "java.net.SocketTimeoutException: timeout";
    public static String error_net_timeout_lzu2ldr_ = "java.net.SocketTimeoutException: failed to connect to api.ldr.cool/49.233.202.129";
    public static String error_net_timeout = "网络连接超时！";
    public static String error_net_timeout_lzu2ldr = "校园网屏蔽了轻学伴！试试断开wifi，使用手机流量？";
    public static String error_net_no_ = "Unable to resolve host";
    public static String error_net_no = "网络连接错误，请检查网络连接！";
    public static String error_lzu_no_ldr_ = "java.net.UnknownHostException: Unable to resolve host \"api.ldr.cool\"";
    public static String error_lzu_no_ldr = "可能是轻学伴被兰大网络屏蔽。登录轻学伴或刷新首页等，请不要使用兰大WIFI";
    public static String error_local_time_ = "javax.net.ssl.SSLHandshakeException: Chain validation failed";
    public static String error_local_time = "手机时间有误！";
    public static String error_usr = "您没有该权限！";
    public static String error_no_user_ = "No value present";
    public static String error_ldr_token_ = "HTTP 401";
    public static String error_loading = "retrofit2.adapter.rxjava3.HttpException: HTTP 500";
    public static String error_loading_msg = "后台数据更新中，请稍后再试";


    public static final String ldr_usr_head = "ldr_";
    public static final Long ldr_public_usr_id = 123456789012L;
    public static final String ldr_public_usr = ldr_usr_head + ldr_public_usr_id;
    public static final String ldr_public_pw = "BF8F42F4D31358DC26BC9E0E2D8EBBE41D7D3B6175D25F1BEDEB19C8B0160C58";

    public static final Long ldr_anonymous_usr_id = 123456789011L;
    public static final String ldr_anonymous_usr = ldr_usr_head + ldr_anonymous_usr_id;
    public static final String ldr_anonymous_pw = "3E9778924821C3E762A2D4F80ED34AD76EBA34FFB9A5E465F98C14E3904466C0";

    public static final String ldr_qq_app_id = "1108022424";
    public static final String ldr_qq_app_key = "IQtIIpoFkK0FJ5sA";

    public static final Long ldr_no_usr_id = 320210526009L;
    public static final String ldr_no_usr_name = "ldr";
    public static final String ldr_no_name = "未认证";
    public static final String ldr_no_nickname = "未认证";
    public static final String ldr_no_other = "未认证";


    public static final String ldr_new_lzu_grade = "级准新生";

    public static final int nickname_max_length = 10;
    public static final int nickname_min_length = 2;

    public static final int mood_max_length = 20;
    public static final int mood_min_length = 2;

    public static final int comment_min_length = 2;
    public static final int comment_max_length = 248;

    /**
     * 准新生账号审核：考号长度
     */
    public static final int check_new_kh_length = 12;
    public static final int check_new_pw_length = 6;
    public static final int check_new_max_length = 50;


    public static final int change_anonymous_max_num = 3;


    public static final float errorCardDisActive = -2F;
    public static final float errorCard = -1F;

    public static final float errorFee = -1F;
    public static final float errorFeeNoUsrData = -2F;


    public static final String BugLyID = "82749d0000";
    public static final Long yuh = 220200838111L;//百度
    public static final int mainNewsNum = 15;//百度

    public static final String getLZUWebError = "120.lzu.edu.cn";//查询成绩，官网出错
    public static final String getScoreWebError = "官网成绩查询有问题：";//查询成绩，官网出错
    public static final String getScoreNoXhError = "未认证学生信息：";//查询成绩，官网出错

    public static final String TEST_COOKIE_FLAG_USER_ZH_NAME = "userZhName";//用户名作为flag
    public static final String TEST_COOKIE_FLAG_USER_ID = "userId";//用户名作为flag

    public static final String LzuWebDataKeyMy = "my";//用户名作为flag
    public static final String LzuWebDataKeyJwk = "jwk";//用户名作为flag
    public static final String LzuWebDataKeyYjsJw = "yjsjw";//用户名作为flag
    public static final String LzuWebDataKeyZhXg = "zhxg";//用户名作为flag
    public static final String LzuWebDataKeyYjsXg = "yjsxg";//用户名作为flag
    public static final String LzuWebDataKeyECard = "ecard";//用户名作为flag
    public static final String LzuWebDataKeySeat = "seat";//用户名作为flag
    public static final String LzuWebDataKeyA = "a";//用户名作为flag
    public static final String LzuWebDataKeySSO = "sso";//用户名作为flag
    public static final String LzuWebDataKeyLib = "lib";//用户名作为flag
    public static final String LzuWebDataKeyWeiBan = "weiban";//用户名作为flag

    public static final String LzuWebDataKeyYx = "yx";//用户名作为flag
    public static final String LzuWebDataKeyVpnX = "vpnx";//用户名作为flag


    public static final String errorNoLZUer = "使用兰大邮箱或者学号登录，才能使用该功能!!";
    public static final String myCookieNo = "个人工作台需要重新登录！";
    public static final String errorMy2Other = "信息门户跳转其他网站失败！";
    public static final String errorMy2OtherNote = "暂时无法跳转登录，您可以暂时使用网页导入";
    public static final String errorImport = "请点击右上角“官网版”，检查官网是否免登录访问，" +
            "如果可以访问，请在“我的 - 关于我们 - 找到我们”，联系开发者解决，谢谢";

    public static final String PROFILE_ANONYMOUS_VERSION = "anonymous_version";
    public static final String PROFILE_ANONYMOUS_SEX = "sex";
    public static final String PROFILE_ANONYMOUS_NICKNAME = "nickname";
    public static final String PROFILE_ANONYMOUS_MD5_ID = "md5_id";

    public static final String PROFILE_TO_ANONYMOUS_MD5_ID = "to_md5_id";
    public static final String PROFILE_TO_ANONYMOUS_SEX = "to_sex";
    public static final String PROFILE_TO_ANONYMOUS_NICKNAME = "to_nickname";

    public static final String my2OtherCookieHead = "iPlanetDirectoryPro";
    public static final String my2OtherCookieHead1 = "CASTGC";
    public static final String my2OtherCookieHead2 = "kaptchaCookie";
    public static final String my2OtherCookieHead3 = "eusp_token";
    public static final String yjsAdminToken = "LZYZ-ADMIN-TOEKN";
    public static final String stCookieHead = "ldr_ticket";
    public static final String gmsBearerHeadStart = "Bearer ";


    public static final String urlLdrHhSh = "https://ldr.cool/nbnhhsh/index.html";//好好说话

    public static final String msgErrorLzuApp = "轻学伴不再自动登录学校系统，请同学们自己输入账号密码，获取相关数据，注意：账号密码等信息仅临时保留在手机中。";


    public static final String ACTION_REFRESH_AUTO = "io.github.yuzhiang.qxb.action.APPWIDGET_REFRESH_AUTO";


    public static final String FORUM_AUTO_SAVE_IMAGE_URLS = "_image_urls";
    public static final String POINT_TYPE_QD = "qd";
    public static final String POINT_TYPE_GET_UP = "get_up";


}
