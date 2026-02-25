package io.github.yuzhiang.qxb.common;

import com.blankj.utilcode.util.PathUtils;

public class FileConfig {
    public static final String NEWS_SELECTED = "newsSelected_v2";
    public static String path_image = PathUtils.getInternalAppDataPath() + "/image";

    public static final String COURSE_BG = path_image + "/";//课表背景
    public static final String HEAD_IMG = path_image + "/head.jpg";//课表背景

    public static String path_student = PathUtils.getInternalAppFilesPath() + "/students";//老师的学生保存在本地文件夹，每个文件名字为课程名
    public static String path_student_note = PathUtils.getInternalAppFilesPath() + "/student_note";//老师的学生保存在本地文件夹，每个文件名字为课程名

    public static String flag = "flag";//一些标记放在本地在SPUtil里
    public static String flag_main_msg_ids = "flag_main_msg_ids";//提示图书馆预约
    public static String flag_main_msg_show_date = "flag_main_msg_show_date";//这次显示的时间，方便计算间隔
    public static String flag_update_ldr = "flag_update_ldr";//更新软件
    public static String flag_update_ldr_show_time = "flag_update_ldr_show_time";//更新软件
    public static String flag_forum_gf = "ldr_forum_gf";//论坛规范
    public static final String FLAG_COURSE_BG_FONT = "CourseBgFont";//课表字体颜色(背景颜色过深，字体改为白色)是否修改过

    public static final String PM_TC = "pm_tc";//后台界面弹出权限

    public static final String LNM_LOGS = PathUtils.getInternalAppCachePath() + "/LnmLogs.yuh";//拦截日志


    public static final String DIR_LDR_LOGS = PathUtils.getInternalAppFilesPath() + "/log";//拦截日志
    public static final String DIR_LIST = PathUtils.getInternalAppFilesPath() + "/list";
    public static final String DIR_JSON = PathUtils.getInternalAppFilesPath() + "/json";
    public static final String DIR_JSON_AD = DIR_JSON + "/saveLdrAD";
    public static final String dir_mail = PathUtils.getExternalAppFilesPath() + "/LzuMail/";
    public static final String dir_web = PathUtils.getExternalAppDownloadPath() + "/web/";


    public static final String sp_key_main_fun = "DiyMainFun";


    public static final String dir_bak_ = PathUtils.getExternalAppCachePath() + "/ldr_bak/";


}
