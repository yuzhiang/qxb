package io.github.yuzhiang.qxb.common.Constant;

import com.blankj.utilcode.util.PathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constant {


    public static final String Login_Type_QQ = "qq";
    public static final String Login_Type_WX = "wx";

    public static final String registerWay_QQ = Login_Type_QQ;
    public static final String registerWay_WX = Login_Type_WX;
    public static final String registerWay_Mail = "mail";

    public static final String Login_Wx_Activity_Login = "loginActivity";
    public static final String Login_Wx_Activity_Bind = "MangeUserActivity";


    public static final String WeiXinApi = "https://api.weixin.qq.com/";
    public static final String WeiXinAppID = "wxe2d0795e7b4d5db7";
    public static final String WeiXinAppSecret = "c59e8d7a61977c29fdd222a5b2dc641c";

    //    电信的小程序
    public static final String WeiXinAppGhIdDx = "gh_51be84094e77";
    public static final String WeiXinAppGhIdTest = "gh_a9547906945e";


    public static final String AppIDGoMore = "5262265";
    public static final String AdSplashGoMore = "887734987";
    public static final String AdSplashNormal = "887674453";
    public static final String AdSplashReward = "102088521";

    public static final String TopOnAppID = "a61dbcecd64716";
    public static final String TopOnAppKey = "743d3489b39975a7b376c48011773929";
    public static final String TopOnSplashTopOnPlacementID = "b61dbcf6912770";
    public static final String rewardTopOnPlacementID = "b61e1060c1ebf6";


    public static final String NOTICE_NUM = "noticeNum";

    public static final String MAIL_LZU = "@lzu.edu.cn";


    public static final int USER_VERSION_INIT = 0;
    public static final int USER_VERSION_RESET_PW = 1;
    public static final int USER_VERSION_XYK = 2;
    public static final int USER_VERSION_XYK_AUTO = 3;

    public static final String REGEX_LDR_USER_NAME = "[a-z][a-z0-9]{4,20}$";
    public static final String REGEX_LZU_XH = "[83210](19|20)\\d{2}\\d{6}[01]";
    //    1900  -  2099年：
    public static final String REGEX_GRADLE = "(19|20)\\d{2}级(.*?)";

    //密码
    //数字
    public static final String REG_NUMBER = ".*\\d+.*";
    //小写字母
    public static final String REG_UPPERCASE = ".*[A-Z]+.*";
    //大写字母
    public static final String REG_LOWERCASE = ".*[a-z]+.*";
    //特殊符号
    public static final String REG_SYMBOL = ".*[~!@#$%^&*()_+|<>,.?/:;'\\[\\]{}\"]+.*";

    public static final String ldr_course_key = "Vw3jSh5qExcO4zmu";
    public static final String ldr_user_key = "0208022402300419";
    public static final String ldr_other_key = "8wGDCjG62D408C6A";
    public static final String ldr_user_file_key = "1234020802240230";


    public static final String lnmBg = PathUtils.getInternalAppDataPath() + "/image/lnm_bg.jpg";

    public static final String upError = "请勿重复点赞或踩";

    public static final String intentMsgType = "intentMsgType";

    public static final int typeNickname = 3;

    public static final int codeMy2Pj = 123397;
    public static final int codeMy2OA = 123398;
    public static final int codeMy2Job = 123399;

    public static final int codeMy = 123400;

    public static final int codeMy2Jwk = 123401;
    public static final int codeMy2Gms = 123402;

    public static final int codeMy2Zhxg = 123403;
    public static final int codeMy2Yjsxg = 123404;

    public static final int codeMy2ECard = 123405;
    public static final int codeMail = 123406;
    public static final int codeMy2LibSeat = 32;
    public static final int codeMy2A = 33;
    public static final int codeLzuLib = 1234017;

    public static final int codeYx = 123407;

    public static final int codeJwkCookie = 123408;

    public static final int codeStart = 123409;
    public static final int codeEnd = 123410;
    public static final int codeSelectPic = 123411;
    public static final int codeRecoverPic = 123412;
    public static final int codeSuccess = 123413;
    public static final int codeError = 123414;
    public static final int codeMsg = 123415;
    public static final int codeUpdate = 123416;

    public static final int codeShareWeb = 123424;

    public static final int jwkWeb2ScorePost = 123425;
    public static final int jwkWeb2CoursePost = 123426;
    public static final int jwkWeb2Plan = 123427;
    public static final int eCardWeb2Bill = 123428;
    public static final int myWeb2St = 123429;
    public static final int yjsJwWeb2CourseTerm = 123430;


    public static final int eCardWeb2Fee = 123430;

    public static final int jwkWeb2Exam = 123431;
    public static final int jwkWeb2other = 123432;
    public static final int myWeb2Login = 123433;
    public static final int jwkWeb2SpareRoom = 123434;

    public static final int myWeb2Other = 123435;
    public static final int myWeb2LibSeat = 123436;

    public static final int myWeb2Jwk = 123437;
    public static final int myWeb2Gms = 123438;

    public static final int myWeb2ZhXg = 123439;
    public static final int myWeb2YjsXg = 123440;

    public static final int jwkWeb2LdrCode = 123441;
    public static final int codeErrorLdrCode = 123442;

    public static final int myWeb2eCard = 123443;
    public static final int myWeb2A = 123444;

    public static final int myWeb2Mail = 123445;

    public static final int eCardWeb2BillDetail = 123446;
    public static final int eCardWeb2FeeDetail = 123447;

    public static final int codeOther = 1234;

    public static final int codeBlockPost = 123448;
    public static final int codeBlockUser = 123449;
    public static final int codeBlockStar = 123450;

    public static final int cache6H = 123451;
    public static final int cache2D = 123452;
    public static final int eCardWeb2MyMsg = 123453;
    public static final int jwkGmsWeb2CheckXyk = 123454;
    public static final int yjsJwWeb2CoursePost = 123455;
    public static final int myWeb2MailPC = 123456;
    public static final int ssoWeb2Lib = 123457;
    public static final int ssoWeb2LibJy = 123458;
    public static final int yjsJwWeb2ScorePost = 123459;
    public static final int myWeb2KxDzPz = 123460;
    public static final int wenBanWeb2Login = 123461;

    public static final List<Integer> codeMy2Others = new ArrayList<>(Arrays.asList(
            codeMy2Jwk, codeMy2Gms, codeMy2Zhxg, codeMy2Yjsxg,
            codeMy2ECard, codeMy2LibSeat, codeMy2A, codeLzuLib,
            codeMy2OA, codeMy2Pj, codeMy2Job
    ));


    public static List<Integer> getCodeMy2Others() {
        return codeMy2Others;
    }

    public static final int autoExam = 4002;
    public static final int noAuto = 20;
    public static final int autoFindRoom = 4001;


    public static final String[] chineseNum = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};


    //    所有管理员必须有这个属性
    public static final String admin = "ROLE_ADMIN";


    public static final String noNickName = "无昵称";
    public static final String noMood = "每天都要好好的，加油！";
    public static final String noGrade = "年级未认证";
    public static final String noCollege = "学院未认证";
    public static final String noName = "姓名未认证";


    public static final String typeStartStudent = "3";
    public static final String typeStartGraduate1 = "1";
    public static final String typeStartGraduate2 = "2";
    public static final String typeStartTeacher = "8";
    public static final String jwkHost = "http://jwk.lzu.edu.cn";


    public static final String mainCurrentItem = "mainCurrentItem";
    public static final String userType0 = "STUDENT";
    public static final String userType1 = "GRADUATE";
    public static final String userType2 = "TEACHER";
    public static final String userType3 = "FRESH";

    public static final String jwkNotice = "本科生教务系统相关内容，有时必须使用校园网！";


    public final static String lnmState = "lnmState";
    public final static int lnmFinish = 520731;
    public final static int lnmStart = 520730;
    public final static int lnmCancel = 520732;
    public final static int lnmRecover = 520733;
    public final static int lnmDetectUnbind = 520734;


    //轻学伴功能
    public static final int funLdrNews = 22145;//"轻学伴公告新闻",//52
    public static final int funLdrHomeWork = 22146;//"作业",//47
    public static final int funLdrGoodMorning = 22149;//"早起打卡",//50
    public static final int funWebLdrHhSh = 22151;//"好好说话",//51
    public static final int funCalculatorLzu = 22105;//"物理计算",//5
    public static final int funWebLzuFinance = 22106;//"发票税号", //6
    public static final int funLzuMail = 22109;//"邮件查看",//9
    public static final int funWebLzuXl = 22110;//"兰大校历",//10
    public static final int funWebLzuBusTime = 22111;//"校车时刻",//11
    public static final int funLNM = 22113;//"兰朵模式",//15
    public static final int funLdrAI = 22114;//"玩转AI",//16
    public static final int funLdrQR = 22123;//"二 维 码",//29
    public static final int funWebLdrMap = 22127;//"兰朵地图",//34
    public static final int funLdrAllFc = 22128;//"兰朵功能",//35

    //    单纯网页
    public static final int funWebLzuPhones = 22119;//"常用电话",//25
    public static final int funWebYbYc = 22108;//"易班优课",//8
    public static final int funWebLzuSafeCourse = 22118;//"安全微课",//23

    //    界面优化，类似超级课程表，学生在webview手动登录，然后在学生手机上处理数据，重新布局
//    教务系统界面优化
    public static final int funFindSpareRoom = 22102;//"空闲教室",//2
    public static final int funCalScore = 22103;//"成绩绩点", //3
    public static final int funExam = 22104;//"考试安排", //4
    public static final int funLdrJwkPlan = 22122;//"教学计划",//28
    //    智慧一卡通界面优化
    public static final int funLzuBill = 22116;//"兰朵账单",//18
    //    图书馆界面优化
    public static final int funLibBook = 22100;//"借阅信息",//0
    public static final int funSearchBook = 22101;//"馆藏查找", //1

    //    功能取消
    public static final int funWebLzuOrderLib = 22125;//"图书馆预约",//32
    //    public static final int funLzuCourseBooks = 22117;//"专业课本",//20
    public static final int funWebLzuFee = 22115;//"电费充值",//17
    //    public static final int funWebLZUCards = 22112;//"校 园 卡",//14
    public static final int funWebLzuPj = 22121;//"教学评价",//27
    //    public static final int funWebLzuZhXgDjDj = 22129;//"到家登记",//36
    public static final int funWebLzuQj = 22130;//"请假",//37
    //    public static final int funWebLzuZhXgJqDj = 22131;//"假期登记",//38
//    public static final int funWebLzuZhXgSsQd = 22132;//"宿舍签到",//39
    public static final int funWebLzuJwkXk = 22137;//"选课",//44
    public static final int funWebLzuJwkKbSc = 22138;//"课表输出",//45
//    public static final int funWebLzuFxDj = 22140;//"返校登记",//47
//    public static final int funWebLzuZhYgZyJtSq = 22147;//"助研津贴申请",//48
//    public static final int funWebLzuZhYgZyJtSqXq = 22148;//"助研津贴申请详情",//49

    //    跳转浏览器打开
    public static final int funWebLzuPayILzu = 22126;//"网费充值",//33
    public static final int funWebLzuZhXg = 22133;//"智慧学工",//40
    public static final int funWebLzuJwk = 22134;//"教务系统  包括研究生教务系统和本科生教务系统，根据userType自动判断",//41
    public static final int funWebLzuMy = 22135;//"个人工作台",//42
    public static final int funWebLzuECard = 22136;//"智慧一卡通",//43
    public static final int funWebLzuYx = 22139;//"迎新系统",//46
    public static final int funWebLzuVPNx = 22150;//"vpnx",//51


    public static final int funLdrAbout = 22141;//"关于",//48
    public static final int funLdrLogin = 22142;//"登录",//49
    public static final int funLdrCheckXyk = 22152;//"认证校园卡",//50
    public static final int funLdrChangePw = 22153;//"修改密码校园卡",//51
    public static final int funLdrLzuClassCourse = 22154;//"新生班级课表",//52
    public static final int funLdrAccount = 22155;//"账户管理与自动记住密码",//53
    public static final int funLdrEnvelope = 22158;//"时光信使",//56

    public static final int funLdrOpenWeb = 22160;//"打开新网页",//58
    public static final int funLdrShareCourse = 22162;//"蹭课",//59
    public static final int funLdrInviteUser = 22163;//"邀请新用户",//60

    public static final int funWebLzuFindSpareRoom = 22165;//"空闲教室WebLZU",//2
    public static final int funWebLzuExam = 22166;//"考试安排WebLZU",//2
    public static final int funWebLzuScore = 22167;//"成绩查询WebLZU",//2
    //    https://sso.lzu.edu.cn/login?service=https%3A%2F%2Fca.lzu.edu.cn%2Fstudent%2F
    public static final int funWebLzuKxDzPz = 22168;//"可信电子凭证WebLZU",//2
    public static final int funWebLzuYjs = 22169;//"研究生系统",//2
    public static final int funLdrLanZhou = 22170;//"兰州旅游",//2


    //             三种都有
    final public static int MenuCopy = 0;
    final public static int MenuReport = 1;

    //            三种都有，但是仅限于管理员和发帖员
    final public static int MenuDelete = 2;

    //            三种都有，但是仅限于管理员
    final public static int MenuForBid = 3;

    //            post
    final public static int MenuStar = 4;
    final public static int MenuBlockUser = 5;

    //            post且仅限于发帖人
    final public static int MenuEdit = 6;

    //            post仅限于管理员
    final public static int MenuHighlight = 7;
    final public static int MenuUnHighlight = 8;
    final public static int MenuSticky = 9;
    final public static int MenuUnSticky = 10;
    final public static int MenuChangeSubject = 11;
    final public static int MenuChangeSort = 13;

    final public static String bookLoan = "待还";
    final public static String bookReturn = "已还";

    //    不看这个帖子
    final public static int MenuBlockPost = 12;

    //    默认周日开始新一周
    final public static boolean SWITCH_COURSE_WEEK_START_DAY_DEFAULT = true;

    public static List<String> chineseWeek = Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日");


}