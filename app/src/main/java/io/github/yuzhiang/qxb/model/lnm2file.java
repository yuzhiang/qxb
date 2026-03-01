package io.github.yuzhiang.qxb.model;

import static io.github.yuzhiang.qxb.common.FileConfig.LNM_LOGS;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.yuzhiang.qxb.model.StudyProjectRecord;

public class lnm2file {
    public static String file_name = "lnm";
    private static String plan = "plan";
    private static String plan_span = "plan_span";
    private static String start = "start";
    private static String end = "end";
    private static String lnmId = "thisId";
    private static String manualCancelAt = "manualCancelAt";
    private static String studyProjectsKey = "study_projects";
    private static String studyProjectSelectedKey = "study_project_selected";
    private static String studyProjectLogsKey = "study_project_logs";

    // Keep a process-local cache to avoid SharedPreferences async write timing issues.
    private static volatile Long cachePlan = null;
    private static volatile Long cachePlanSpan = null;
    private static volatile Long cacheStart = null;
    private static volatile Long cacheEnd = null;
    private static volatile Long cacheLnmId = null;
    private static volatile Long cacheManualCancelAt = null;

    public static void saveLnmTime(Date start_time, Date plan_time, Date end_time) {

        saveStartTime(start_time);
        savePlanTime(plan_time);
        saveNowTime(end_time);
    }

    public static void finishLearn() {
        cacheStart = -1L;
        cachePlan = -1L;
        cacheEnd = -1L;
        cachePlanSpan = -1L;
        cacheLnmId = -1L;
        SPUtils.getInstance(file_name).put(start, -1L, true);
        SPUtils.getInstance(file_name).put(plan, -1L, true);
        SPUtils.getInstance(file_name).put(end, -1L, true);
        SPUtils.getInstance(file_name).put(plan_span, -1L, true);
        SPUtils.getInstance(file_name).put(lnmId, -1L, true);

        LogUtils.i("学习结束===删除");

    }

    public static void savePlanTime(Date date) {
        cachePlan = date.getTime();
        SPUtils.getInstance(file_name).put(plan, date.getTime(), true);
    }

    public static void savePlanSpan(long span) {
        cachePlanSpan = span;
        SPUtils.getInstance(file_name).put(plan_span, span, true);
    }

    public static Long getPlanSpan() {
        if (cachePlanSpan != null) return cachePlanSpan;
        cachePlanSpan = SPUtils.getInstance(file_name).getLong(plan_span);
        return cachePlanSpan;
    }

    public static long okSpan = 5000;//网络时间与本地时间差，相差5s以内可接受   5000ms == 5s
    public static long errorSpan = okSpan * 60;//网络时间与本地时间差，相差5s以内可接受   5000ms == 5s


//    public static void saveLearning(boolean b){
//        SPUtils.getInstance(file_name).put(sab, b);
//    }

    public static boolean getLearning() {
        long n = getSpanTime();
//        LogUtils.i(n + "====" + (-okSpan));
        return n > -okSpan;

    }

    public static long getSpanTime() {
        Date pt = getPlanTime();
        if (pt == null) {
            return -okSpan * 10;
        } else {
            return pt.getTime() - System.currentTimeMillis();
        }

    }

    public static void saveStartTime(Date date) {
        cacheStart = date.getTime();
        SPUtils.getInstance(file_name).put(start, date.getTime(), true);
    }

    public static void saveNowTime(Date date) {
        cacheEnd = date.getTime();
        SPUtils.getInstance(file_name).put(end, date.getTime(), true);
    }


    public static Date getPlanTime() {

        long l = cachePlan != null ? cachePlan : -1L;
        try {
            if (cachePlan == null) {
                l = SPUtils.getInstance(file_name).getLong(plan);
                cachePlan = l;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (l < 0) {
            return null;
        } else {
            return new Date(l);
        }
    }

    public static Date getStartTime() {
        long l = cacheStart != null ? cacheStart : -1L;
        try {
            if (cacheStart == null) {
                l = SPUtils.getInstance(file_name).getLong(start);
                cacheStart = l;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (l < 0) {
            return null;
        } else {
            return new Date(l);
        }

    }

    public static Date getLastTime() {
        long l = cacheEnd != null ? cacheEnd : -1L;
        try {
            if (cacheEnd == null) {
                l = SPUtils.getInstance(file_name).getLong(end);
                cacheEnd = l;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (l < 0) {
            return null;
        } else {
            return new Date(l);
        }
    }

    public static void saveEnableApp(List<String> apps) {

        SPUtils.getInstance(file_name).put("enableApp", new HashSet<>(apps));
    }

    public static List<String> getEnableApp() {

        Set<String> s = SPUtils.getInstance(file_name).getStringSet("enableApp");

        return new ArrayList<>(s);
    }

    public static void saveStudyProjects(List<String> projects) {
        if (projects == null) projects = new ArrayList<>();
        SPUtils.getInstance(file_name).put(studyProjectsKey, new Gson().toJson(projects), true);
    }

    public static List<String> getStudyProjects() {
        String json = SPUtils.getInstance(file_name).getString(studyProjectsKey, "");
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<String> list = new Gson().fromJson(json, new TypeToken<List<String>>() {
            }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void saveSelectedStudyProject(String name) {
        SPUtils.getInstance(file_name).put(studyProjectSelectedKey, name == null ? "" : name, true);
    }

    public static String getSelectedStudyProject() {
        return SPUtils.getInstance(file_name).getString(studyProjectSelectedKey, "");
    }

    public static void addStudyProjectRecord(StudyProjectRecord record) {
        if (record == null) return;
        List<StudyProjectRecord> records = getStudyProjectRecords();
        records.add(record);
        SPUtils.getInstance(file_name).put(studyProjectLogsKey, new Gson().toJson(records), true);
    }

    public static List<StudyProjectRecord> getStudyProjectRecords() {
        String json = SPUtils.getInstance(file_name).getString(studyProjectLogsKey, "");
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<StudyProjectRecord> list = new Gson().fromJson(json, new TypeToken<List<StudyProjectRecord>>() {
            }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    public static void saveTime(int hourOfDay, int minute) {

        SPUtils.getInstance(file_name).put("hourOfDay", hourOfDay);
        SPUtils.getInstance(file_name).put("minute", minute);

    }

    public static int getHourOfDay() {
        return SPUtils.getInstance(file_name).getInt("hourOfDay", 0);

    }

    public static int getMinute() {
        return SPUtils.getInstance(file_name).getInt("minute", 45);

    }


    public static long getThisId() {
        if (cacheLnmId != null) return cacheLnmId;
        cacheLnmId = SPUtils.getInstance(file_name).getLong(lnmId);
        return cacheLnmId;
    }

    public static void saveThisId(long id) {
        LogUtils.i("保存id：" + id);
        cacheLnmId = id;
        SPUtils.getInstance(file_name).put(lnmId, id, true);
    }

    public static void markManualCancelNow() {
        cacheManualCancelAt = System.currentTimeMillis();
        SPUtils.getInstance(file_name).put(manualCancelAt, cacheManualCancelAt, true);
    }

    public static long getManualCancelAt() {
        if (cacheManualCancelAt != null) return cacheManualCancelAt;
        cacheManualCancelAt = SPUtils.getInstance(file_name).getLong(manualCancelAt, -1L);
        return cacheManualCancelAt;
    }

    public static void clearManualCancelMark() {
        cacheManualCancelAt = -1L;
        SPUtils.getInstance(file_name).put(manualCancelAt, -1L, true);
    }

    public static boolean saveLnmApp(String name, String des) {

        List<LnmApp> lnmApps = getLnmApp();
        lnmApps.add(new LnmApp(name, des));

        return true;
    }

    public static boolean saveLnmLogs(String content) {

        if (FileUtils.getFileLength(LNM_LOGS) > 50000) {
            FileUtils.delete(LNM_LOGS);
        }

        FileIOUtils.writeFileFromString(LNM_LOGS, content + "\n\n" + getLnmLogs());


        return true;
    }

    public static String getLnmLogs() {
        String logs = FileIOUtils.readFile2String(LNM_LOGS);
        if (logs == null) logs = "";
        return logs;
    }

    public static boolean clearLnmLogs() {
        FileUtils.delete(LNM_LOGS);
        return true;
    }

    public static List<LnmApp> getLnmApp() {

        String s = SPUtils.getInstance(file_name).getString("recordLnmApp");
        List<LnmApp> lnmApps = new ArrayList<>();
        try {
            lnmApps = new Gson().fromJson(s, new TypeToken<List<LnmApp>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (lnmApps == null) lnmApps = new ArrayList<>();

        return lnmApps;

    }


    public static List<Long> getStudyModeUps() {
        Calendar calendar = Calendar.getInstance();
        int WEEK_OF_YEAR = calendar.get(Calendar.WEEK_OF_YEAR);
        int YEAR = calendar.get(Calendar.YEAR);

        String s = SPUtils.getInstance(file_name).getString(YEAR + "_" + WEEK_OF_YEAR + "_StudyModeUps");

        List<Long> longs = new Gson().fromJson(s, new TypeToken<List<Long>>() {
        }.getType());

        if (longs == null) longs = new ArrayList<>();

        return longs;

    }

    public static boolean saveStudyModeUps(Long upId) {

        Calendar calendar = Calendar.getInstance();
        int WEEK_OF_YEAR = calendar.get(Calendar.WEEK_OF_YEAR);
        int YEAR = calendar.get(Calendar.YEAR);

        List<Long> longs = getStudyModeUps();
        if (!longs.contains(upId)) {
            longs.add(upId);
            SPUtils.getInstance(file_name).put(YEAR + "_" + WEEK_OF_YEAR + "_StudyModeUps", new Gson().toJson(longs));
        }
        return true;
    }


}
