package io.github.yuzhiang.qxb.common;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.SPUtils;

import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;

public class LzuUrl {

    public static String my2jwk = "https://sso.lzu.edu.cn/login?service=http://jwk.lzu.edu.cn/academic/login/lzu/loginIds6Valid.jsp";
    public static String my2yjs = "https://sso.lzu.edu.cn/login?service=http://yjs.lzu.edu.cn/lzuyjs/";

    public static final String WebUrlTitle = "WebUrlTitle";//账单
    public static final String WebUrl = "WebUrl";//账单
    public static final String WebQaUrl = "WebQaUrl";//账单
    public static final String WebFunCode = "WebFunCode";//账单
    public static final String WebFunName = "WebFunName";//账单
    public static final String WebUrl2 = "WebUrl2";//账单
    public static final String WebUrl2Data = "WebUrl2Data";//账单
    public static final String WebOnlyRef = "WebOnlyRef";//账单


    public static final String urlLzuMyHost = "my.lzu.edu.cn";//账单

    public static final String ybYc = "https://www.yooc.me/mobile/yooc_courses";//易班优课
    public static final String ybSignUp = "https://www.yiban.cn/user/reg/index?type=baseinfo";//易班优课

    //public static final String urlXl = "https://support.qq.com/embed/phone/55054/faqs/52334";//兰大校历
    public static final String urlXl = "https://api.ldr.cool/ui/list/20";//兰大校历
    //public static final String urlLzuBus = "https://support.qq.com/embed/phone/55054/faqs/52606";//校车信息
    public static final String urlLzuBus = "https://api.ldr.cool/ui/list/19";//校车信息
    //public static final String adTest = "https://support.qq.com/products/55054/faqs/59913";//广告测试
    public static final String adTest = "https://api.ldr.cool/ui/list/31";//广告测试
    //public static final String fee = "https://support.qq.com/embed/phone/55054/faqs/49427";//电费
    public static final String fee = "https://api.ldr.cool/ui/list/21";//电费
    //public static final String libAccount = "https://support.qq.com/embed/phone/55054/faqs/49631";//图书馆账号说明
    public static final String libAccount = "https://api.ldr.cool/ui/list/22";//图书馆账号说明
    //public static final String lnmMsg = "https://support.qq.com/embed/phone/55054/faqs/49630";//轻学伴模式说明
    public static final String lnmMsg = "https://api.ldr.cool/ui/list/23";//轻学伴模式说明
    //public static final String msgScore = "https://support.qq.com/embed/phone/55054/faqs/49740";//绩点计算说明
    public static final String msgScore = "https://api.ldr.cool/ui/list/24";//绩点计算说明
    public static final String ldrUrlParseCourse = "https://api.ldr.cool/ui/list/142";//课表解析说明
    public static final String ldrUrlParseScore = "https://api.ldr.cool/ui/list/147";//成绩解析说明
    //public static final String msgLogin = "https://support.qq.com/embed/phone/55054/faqs/48281";//登录问题
    public static final String msgLogin = "https://api.ldr.cool/ui/list/25";//登录问题
    //public static final String ysXy = "https://support.qq.com/embed/phone/55054/faqs/56983";//隐私协议
    public static final String ysXy = "https://api.ldr.cool/ui/list/26";//隐私协议
    //public static final String fwTk = "https://support.qq.com/embed/phone/55054/faqs/57320";//服务条款
    public static final String fwTk = "https://api.ldr.cool/ui/list/27";//服务条款
    //public static final String msg_card_ali = "https://support.qq.com/embed/phone/55054/faqs/63279";//校园卡支付宝充值说明
    public static final String msg_card_ali = "https://api.ldr.cool/ui/list/28";//校园卡支付宝充值说明
    //public static final String msg_card_all = "https://support.qq.com/embed/phone/55054/faqs/47998";//校园卡充值说明
    public static final String msg_card_all = "https://api.ldr.cool/ui/list/29";//校园卡充值说明
    //public static final String msg_search_book = "https://support.qq.com/embed/phone/55054/faqs/49741";//馆藏查询说明
    public static final String msg_search_book = "https://api.ldr.cool/ui/list/30";//馆藏查询说明
    public static final String urlForumGF = "https://api.ldr.cool/ui/list/42";//社区规范
    public static final String urlLdrQsJs = "https://api.ldr.cool/ui/list/17";//前世今生
    public static final String urlLdrSySm = "https://api.ldr.cool/ui/list/46";//使用说明
    public static final String urlLdrQQ = "https://api.ldr.cool/ui/list/108";//联系轻学伴
    public static final String urlConfigBak = "https://gitee.com/yuhldr/LdrLearn/raw/master/config.json";//联系轻学伴

    public static final String urlLdrQaJwkKbSc = "https://api.ldr.cool/ui/list/48";//课表输出说明

    public static final String urlNewBusTime = "https://api.ldr.cool/notice/32";//馆藏查询说明
    public static final String urlLdrNetConfig = "https://api.ldr.cool/notice/81";//云控设置

    public static final String urlWebJs = "https://api.ldr.cool/notice/80";//云控设置
    public static final String urlErrorMsg = "https://api.ldr.cool/ui/list/98";//错误说明


    public static final String urlCourseWidGetHelp = "https://api.ldr.cool/ui/list/124";//桌面小部件
    public static final String urlPmHelp = "https://api.ldr.cool/ui/list/125";//权限设置


    public static final String urlWebWeekStart = "https://api.ldr.cool/ui/list/87";//周一开始新一周与周日开始的区别

    public static final String urlBanners = "https://api.ldr.cool/notice/34";//馆藏查询说明
    public static final String urlForumMsg = "https://api.ldr.cool/notice/36";//馆藏查询说明

    public static final String urlTestUsr = "https://api.ldr.cool/notice/41";//获取测试用户

    public static final String urlInviteUser = "https://api.ldr.cool/ui/list/114";//电费
    public static final String urlVipNoAD = "https://api.ldr.cool/ui/list/115";//vip免广告

    public static final String urlCheckNewLZUer = "https://api.ldr.cool/ui/list/45";//准新生注册审核说明
    public static final String urlRegister = "https://api.ldr.cool/ui/list/101";//注册审核说明
    public static final String urlCheckXyk = "https://api.ldr.cool/ui/list/102";//认证审核说明
    public static final String urlLdrPoint = "https://api.ldr.cool/ui/list/103";//积分
    public static final String urlLdrEnvelope = "https://api.ldr.cool/ui/list/104";//时光信使
    public static final String urlLdrEvaluateCourse = "https://api.ldr.cool/ui/list/104";//时光信使
    public static final String urlLdrArticle = "https://api.ldr.cool/ui/list/105";//分享会
    public static final String urlLdrPointRule = "https://api.ldr.cool/ui/list/106";//积分规则

    public static final String urlLzuMap = "https://ldr.cool/ldr-lzu-map.html";//轻学伴地图
    public static final String urlLdrHhSh = "https://ldr.cool/hhsh.html";//好好说话
    public static final String urLdrLzuClassCourse = "https://yuh.ldr.cool/lzu/2021";//新生班级课表


    public static final String urlNetDetails = "http://login.lzu.edu.cn:8800/home";//网费充值
    public static final String urlNetDetails0 = "http://login.lzu.edu.cn/";//网费充值

    public static final String urlPayNetMain = "http://a.lzu.edu.cn/lzu_selfpay/web/pay/html/home.html";//网费充值
    public static final String urlErrorPayNet = "http://a.lzu.edu.cn/lzu_selfpay/web/pay/login.html";//网费充值，登陆界面

    public static final String urlLzuYx = "http://yx.lzu.edu.cn/lzuzsb/stuweb/index/toStuMain";//迎新系统
    public static final String urlLzuYxApp = "http://yx.lzu.edu.cn/lzuzsb/stuapp/stuphone/toStuPhoneMain?ksh=";//迎新系统
    public static final String urlLzuYxMain = "http://yx.lzu.edu.cn";//迎新系统

    public static final String urlLibPersonalCenter = "https://findlzu.libsp.cn/#/personalCenter";//借阅信息

    public static final String urlLdrCoolApk = "https://www.coolapk.com/apk/io.github.yuzhiang.qxb/";//内测
//            + UsrMsgUtils.getYxUsr()
    ;//校车信息
    public static final String appPhoneUrl = "http://application.lzu.edu.cn/appPhone/";

    //TODO这个网址可能不对
    public static final String urlVPNx = "https://vpnx.lzu.edu.cn/";// vpnx
    public static final String urlJwkXk = "http://jwk.lzu.edu.cn/academic/student/selectcoursedb/jumppage.jsp";//选课
    public static final String urlJwkKbSc = "http://jwk.lzu.edu.cn/academic/teacher/timetable/classTimetable.do";//课表输出

    public static final String urlYjsXgQj = "http://yjsxg.lzu.edu.cn/lzuygb/qxj/wdqj.jsp";//请假

    public static final String urlZhXgQj = "http://zhxg.lzu.edu.cn/lzuyz/qxj/wdqj.jsp";//请假
    public static final String urlZhXgSsQd = "http://zhxg.lzu.edu.cn/lzuyz/xsxxwh/ssqd.jsp";//宿舍签到
    public static final String urlZhXgJqDj = "http://zhxg.lzu.edu.cn/lzuyz/qxj/qxjJqdj.jsp";//假期登记
    public static final String urlZhXgDjDj = "http://zhxg.lzu.edu.cn/lzuyz/qxj/qxjDjdjEdit.jsp";//到家登记


    public static final String webExamUrl = "http://application.lzu.edu.cn/appStuExam/#/?PersonID=";
//    public static final String webFindRoomUrl = "http://self.lzu.edu.cn/classroomQuery/#/";
//    public static final String webFindRoomUrl = "https://appservice.lzu.edu.cn/lzu-web-classroomquery/#/?st={ST}&PersonID={PersonId}";

    public static final String financeUrl = "http://finance.lzu.edu.cn/weixinMain/html/account.html";
    //
    public static final String urlSafeCourseBase = "http://weiban.mycourse.cn/index.html";//安全微课
//    public static final String urlSafeCourse = "https://weiban.mycourse.cn/index.html#/fill?openid={openid}&state=1";//安全微课
//    public static final String urlSafeCourse = "https://weiban.mycourse.cn/#/login?tenantCode=73000001&displayPop=2&userName={xh}&tenantName=兰州大学&userNamePrompt=请输入学号&passwordPrompt=请输入学号&popPrompt=各位新同学：2021级新生安全教育课程已开放，本科生用户名是考生号，密码是身份证后六位，研究生用户名和密码都是学号，请勿输错！";//安全微课
//    public static final String urlSafeCourse = "https://weiban.mycourse.cn/index.html#/fill?openid=oeNCVuDu-eqkmZE7wojawpz0EAdc&state=1";//安全微课

    public static final String urlBookSeatMain = "http://seat.lib.lzu.edu.cn/home/book/index/type/4";
    public static final String urlMailMobile = "https://mail.lzu.edu.cn/coremail/xphone/main.jsp#module=folder";
    public static final String urlMailPC = "https://mail.lzu.edu.cn/coremail/XT5/index.jsp";
    public static final String urlMainMy = "http://" + urlLzuMyHost + "/main";//信息门户

    public static final String urlYjsXgZyJtSq = "http://yjsxg.lzu.edu.cn/lzuygb/zyjt/stu/zyjtsq.jsp";//助研津贴申请
    public static final String urlYjsXgZyJtSqXq = "http://yjsxg.lzu.edu.cn/lzuygb/zyjt/stu/sqjgxq.jsp";//助研津贴申请结果详情


    public static final String urlLoginMailPhone = "https://mail.lzu.edu.cn/coremail/xphone";//邮箱
    public static final String urlLoginMail = "https://mail.lzu.edu.cn";//邮箱

    public static final String urlMainECard = "https://ecard.lzu.edu.cn/lzulogin";//智慧一卡通
    public static final String urlECardPay = "https://ecard.lzu.edu.cn/payFee";//智慧一卡通生活缴费
    public static final String urlKxDzPz = "https://sso.lzu.edu.cn/login?service=https://ca.lzu.edu.cn/student/";//智慧一卡通生活缴费


    public static final String urlLibraryAppointment = "http://" + urlLzuMyHost + ":8080/login?service=http%3A%2F%2Fseat.lib.lzu.edu.cn%2Fcas%2Findex.php%3Fcallback%3Dhttp%3A%2F%2Fseat.lib.lzu.edu.cn%2Fhome%2Fbook%2Findex%2Ftype%2F4";//图书馆预约

    public static final String urlScoreJwk = "http://jwk.lzu.edu.cn/academic/manager/score/studentOwnScore.do?groupId=&moduleId=2020";//成绩
    public static final String urlScoreJwkWeb = "http://jwk.lzu.edu.cn/academic/manager/score/studentOwnScore.do";//成绩
    public static final String urlScoreGmsWeb = "http://yjsjw.lzu.edu.cn/xs/kcgl/kccjcx";//成绩
    //    public static final String urlScoreGms = "http://yjsjw.lzu.edu.cn:30003/api/xs/queryStugrCj?page=0&size=100&sort=xm&jh=0&xykh=";//成绩
    public static final String urlScoreGms = "http://yjsjw.lzu.edu.cn:30003/api/xs/queryStugrCj?page=0&size=1000&sort=xm&xykh=";//成绩


    public static final String kurlUsrInfoGms = "http://yjs.lzu.edu.cn/adminApi/auth/info";//个人信息
    public static final String urlUsrInfoJwk = "http://jwk.lzu.edu.cn/academic/showPersonalInfo.do";//个人信息


    public static final String urlGmsLeft = "http://gms.lzu.edu.cn/graduate/listLeft.do?";//成绩
    public static final String urlJwkLeft = "http://jwk.lzu.edu.cn/academic/listLeft.do?";//成绩

    public static final String urlExamJwk = "http://jwk.lzu.edu.cn/academic/student/exam/index.jsdo";//考试安排

    public static final String urlSpareRoomJwk = "http://jwk.lzu.edu.cn/academic/teacher/teachresource/roomschedule_week.jsdo";//空闲教室
    public static final String urlSpareRoomJwkWeb = "http://jwk.lzu.edu.cn/academic/teacher/teachresource/roomschedulequery.jsdo";//空闲教室


    public static final String urlCourseGms = "http://yjsjw.lzu.edu.cn/xs/kcgl/wsxk";//课表
    public static final String urlCourseGmsNew = "http://yjsjw.lzu.edu.cn:30003/api/xs/stuxkjg/NotAuth?page=0&size=100&sort=id,desc&xqcode=";//课表
    public static final String urlCourseJwk = "http://jwk.lzu.edu.cn/academic/student/currcourse/currcourse.jsdo";//课表
    public static final String urlCourseJwkTeacher = "http://jwk.lzu.edu.cn/academic/teacher/teachingtask/ownteachingtask.jsdo";//课表
    public static final String jwkPlanUrl = "http://jwk.lzu.edu.cn/academic/manager/studyschedule/studentSelfSchedule.jsdo";//教学计划

    public static final String myChangePw = "http://" + urlLzuMyHost + "/mylzu/password-one";//邮箱密码修改
    public static final String jwkChangePw = "http://jwk.lzu.edu.cn/academic/sysmgr/user_password.jsdo";//教务系统密码修改
    public static final String gmsChangePw = "http://gms.lzu.edu.cn/graduate/password/passwordList.do";//研究生系统密码修改
    public static final String eCardChangePw = "https://ecard.lzu.edu.cn/securityCenter/firstStep?pwdType=2";//智慧一卡通修改密码
    public static final String urlJumpLib = "https://findlzu.libsp.cn/";//跳转到馆藏查找

    //    public static final String urlJumpLibOld = "http://202.201.7.5/uhtbin/cgisirsi/?ps=zIEez2gcOj/%E5%85%B0%E5%A4%A7%E4%B8%AD%E5%BF%83%E9%A6%86/0/49";//跳转到馆藏查找
    public static final String urlJumpLibOld = "http://tyrzfw.chaoxing.com/login_auth/lzu/init?&refer=https%3A//findlzu.libsp.cn/find/sso/login/lzu/0";//跳转到馆藏查找

    public static final String urlLzuBill = "https://ecard.lzu.edu.cn/bill";//账单


    public static final String urlECardWx0 = "https://jkschool.lsmart.cn/card/queryAcc_queryAccount.shtml?openId=";//微信充值
    public static final String urlECardWxBill0 = "https://jkschool.lsmart.cn/order/order_index.shtml?openId=";//微信交易记录
    public static final String urlECardWxQa0 = "https://statichttp.xiaofubao.com/wxschool/question/question.html?t=12345&openId=";//微信问题解答
    public static final String urlECardWxLost0 = "https://jkschool.lsmart.cn/card/stop_index.shtml?t=12345&openId=";//微信挂失
    public static final String urlECardWxFound0 = "https://jkschool.lsmart.cn/card/unbind_toUnBind.shtml?openId=";//微信解绑


    public static String getWxUrl(int n) {
        String url0 = "";

        switch (n) {
            case 141:
                //交易记录
                url0 = urlECardWxBill0;
                break;
            case 142:
                //问题解答
                url0 = urlECardWxQa0;
                break;
            case 143:
                //挂失
                url0 = urlECardWxLost0;
                break;
            case 144:
                //解绑
                url0 = urlECardWxFound0;
                break;
            default:
                //交易记录
                url0 = urlECardWx0;
                break;
        }

        return url0 + getWxId() + "&wxArea=10730";

    }


    public static String getWxId() {

        String card_openid = SPUtils.getInstance(UsrMsgUtils.fileName).getString("ECardOpenId");
//注意必须是28位！！！！
        if (card_openid.length() != 28) {
            String lzuId = UsrMsgUtils.getECardId();
            if (lzuId.length() == 12) { //登录过，并且学号12位
                String[] a = {"A", "B", "C", "D", "E",
                        "F", "G", "H", "I", "J"};
                for (int i = 0; i < 10; i++) {
                    lzuId = lzuId.replace(i + "", a[i]);
                }
                card_openid = lzuId + lzuId;
            } else {
                String usr = UsrMsgUtils.getMyUser();
//防止出现重复
                card_openid = EncryptUtils.encryptMD5ToString(UsrMsgUtils.getMyUser())
                        .substring(0, 24 - usr.length()) + usr.toUpperCase();
            }

            card_openid = card_openid + "_-MN";
            SPUtils.getInstance(UsrMsgUtils.fileName).put("ECardOpenId", card_openid);
        }

        return card_openid;
    }


}

