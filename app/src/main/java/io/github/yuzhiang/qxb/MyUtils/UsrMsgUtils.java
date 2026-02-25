package io.github.yuzhiang.qxb.MyUtils;

import static com.blankj.utilcode.util.ConvertUtils.bytes2HexString;
import static io.github.yuzhiang.qxb.common.Constant.Constant.REGEX_LZU_XH;
import static io.github.yuzhiang.qxb.common.Constant.Constant.SWITCH_COURSE_WEEK_START_DAY_DEFAULT;
import static io.github.yuzhiang.qxb.common.Constant.Constant.USER_VERSION_XYK;
import static io.github.yuzhiang.qxb.common.Constant.Constant.ldr_user_file_key;
import static io.github.yuzhiang.qxb.common.Constant.Constant.noCollege;
import static io.github.yuzhiang.qxb.common.Constant.Constant.noGrade;
import static io.github.yuzhiang.qxb.common.Constant.Constant.noMood;
import static io.github.yuzhiang.qxb.common.Constant.Constant.noNickName;
import static io.github.yuzhiang.qxb.common.Constant.Constant.userType0;
import static io.github.yuzhiang.qxb.common.LdrConfig.spPath;

import android.util.Base64;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.Gson;

import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.common.Constant.Constant;
import io.github.yuzhiang.qxb.common.Constant.ThemeConstant;
import io.github.yuzhiang.qxb.common.LdrConfig;
import io.github.yuzhiang.qxb.model.User;

public class UsrMsgUtils {

    public static String fileName = "UsrMsg";

    public static String encryptUserFile(String s) {
        return encrypt(s, ldr_user_file_key);
    }

    private static String encrypt(String s, String key) {
        byte[] s_ = Base64.encode((s == null ? "" : s).getBytes(), Base64.DEFAULT);
        byte[] ss = EncryptUtils.encryptAES(s_, key.getBytes(), "AES/ECB/PKCS5Padding", null);
        return bytes2HexString(ss);
    }

    public static String decryptUserFile(String s) {
        return decrypt(s, ldr_user_file_key);
    }

    private static String decrypt(String s, String key) {
        try {
            if (s == null || s.length() == 0) {
                return "";
            }
            byte[] ss__ = ConvertUtils.hexString2Bytes(s);
            byte[] sss = EncryptUtils.decryptAES(ss__, key.getBytes(), "AES/ECB/PKCS5Padding", null);
            return new String(Base64.decode(sss, Base64.DEFAULT));
        } catch (Exception e) {
            LogUtils.e(e.toString() + "\n" + s);
            return s;
        }
    }

    public static String getECardId() {
        if (checkXykOk()) {
            return getUserModel().getXh();
        }
        return "";
    }

    public static boolean checkXykOk() {
        return checkXykOk(getUserModel());
    }

    public static boolean checkXykOk(User user) {
        try {
            return user != null
                    && user.getVersion() >= USER_VERSION_XYK
                    && !StringUtils.isEmpty(user.getCollege())
                    && !StringUtils.isEmpty(user.getGrade())
                    && isLzuId(user.getXh());
        } catch (Exception e) {
            LogUtils.e(e);
            return false;
        }
    }

    public static int getSexPic(User user) {
        if (user == null || user.getId() == null || !checkXykOk(user)) {
            return R.drawable.ic_sex_no;
        }
        if (user.getAuthorities() != null && !user.getAuthorities().isEmpty()) {
            return xh2Sex(user.getXh()) == 1 ? R.drawable.ic_vip_boy : R.drawable.ic_vip_girl;
        }
        return getSexPic(xh2Sex(user.getXh()) == 1);
    }

    public static int getSexColor(User user) {
        if (user == null || user.getXh() == null || !checkXykOk(user)) {
            return R.color.gray;
        }
        if (user.getAuthorities() != null && !user.getAuthorities().isEmpty()) {
            return R.color.vip80;
        }
        return getSexColor(xh2Sex(user.getXh()) == 1);
    }

    public static int getSexColor(boolean sex) {
        return getSexColor(sex ? 1 : 0);
    }

    public static int getSexColor(int sex) {
        if (sex == 1) {
            return R.color.boy;
        } else if (sex == 0) {
            return R.color.girl;
        }
        return R.color.gray;
    }

    public static int getSexPic(boolean sex) {
        return getSexPic(sex ? 1 : 0);
    }

    public static int getSexPic(int sex) {
        if (sex == 1) {
            return R.drawable.ic_boy;
        } else if (sex == 0) {
            return R.drawable.ic_girl;
        }
        return R.drawable.ic_sex_no;
    }

    public static int getSexPicVIP(boolean sex) {
        return getSexPicVIP(sex ? 1 : 0);
    }

    public static int getSexPicVIP(int sex) {
        if (sex == 1) {
            return R.drawable.ic_vip_boy;
        } else if (sex == 0) {
            return R.drawable.ic_vip_girl;
        }
        return R.drawable.ic_sex_no;
    }

    public static boolean saveOtherOpenId(String openId, String type) {
        SPUtils.getInstance(fileName).put("otherOpenId", encryptUserFile(openId));
        SPUtils.getInstance(fileName).put("otherLoginType", type);
        return true;
    }

    public static String getOtherOpenId() {
        String s = SPUtils.getInstance(fileName).getString("otherOpenId");
        if (StringUtils.isEmpty(s)) {
            return decryptUserFile(SPUtils.getInstance(fileName).getString("qqOpenId"));
        }
        return decryptUserFile(s);
    }

    public static String getOtherLoginType() {
        return SPUtils.getInstance(fileName).getString("otherLoginType", Constant.Login_Type_QQ);
    }

    public static String getMyUser() {
        return getWebUser(LdrConfig.LzuWebDataKeyMy);
    }

    private static boolean saveWebUser(String key, String user) {
        SPUtils.getInstance(fileName).put(key + "Usr", encryptUserFile(user));
        return true;
    }

    private static boolean saveWebPw(String key, String pw) {
        SPUtils.getInstance(fileName).put(key + "Pw", encryptUserFile(pw));
        return true;
    }

    private static String getWebUser(String key) {
        return decryptUserFile(SPUtils.getInstance(fileName).getString(key + "Usr"));
    }

    private static String getWebPw(String key) {
        return decryptUserFile(SPUtils.getInstance(fileName).getString(key + "Pw"));
    }

    private static boolean saveLdrUser(String user) {
        SPUtils.getInstance(fileName).put("ldrUsr", encryptUserFile(user), true);
        return true;
    }

    private static boolean saveLdrPw(String pw) {
        SPUtils.getInstance(fileName).put("ldrPw", encryptUserFile(pw), true);
        return true;
    }

    public static boolean saveLdrUserPw(String user, String pw) {
        return saveLdrUser(user) && saveLdrPw(pw);
    }

    public static String getLdrUser() {
        return decryptUserFile(SPUtils.getInstance(fileName).getString("ldrUsr"));
    }

    public static String getLdrPw() {
        return decryptUserFile(SPUtils.getInstance(fileName).getString("ldrPw"));
    }

    public static String getNickName() {
        String nickName = noNickName;
        try {
            User user = getUserModel();
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                nickName = user.getNickName();
            }
        } catch (Exception ignored) {
        }
        return nickName;
    }

    public static String getSignature() {
        String signature = noMood;
        try {
            User user = getUserModel();
            if (user.getSignature() != null && user.getSignature().length() > 0) {
                signature = user.getSignature();
            }
        } catch (Exception ignored) {
        }
        return signature;
    }

    public static int getThemeId() {
        return SPUtils.getInstance(fileName).getInt("themeId", 0);
    }

    public static int getThemeColor() {
        return ThemeConstant.themeColorArray[getThemeId()];
    }

    public static int getAccentThemeColor() {
        return ThemeConstant.themeAccentColorArray[getThemeId()];
    }

    public static void clearAll() {
        SPUtils.getInstance(fileName).clear();
    }

    public static User getUserModel() {
        return getUserModel(getUserString());
    }

    public static User getUserModel(String s) {
        User user = new Gson().fromJson(s, User.class);
        if (user == null) {
            user = new User(-1L, LdrConfig.ldr_no_usr_name, "", noNickName, noMood,
                    noGrade, noCollege, userType0, "2021-05-08T00:00:00Z");
        }
        return user;
    }

    private static String getUserString() {
        return decryptUserFile(SPUtils.getInstance(fileName).getString("UsrModel"));
    }

    private static boolean isLzuId(String xh) {
        return RegexUtils.isMatch(REGEX_LZU_XH, xh);
    }

    public static int xh2Sex(String xh) {
        if (isLzuId(xh)) {
            if (xh.endsWith("0")) {
                return 0;
            } else if (xh.endsWith("1")) {
                return 1;
            }
        }
        return 2;
    }

    public static boolean getWeekStartDay() {
        return SPUtils.getInstance(spPath).getBoolean("switch_course_week_start_day", SWITCH_COURSE_WEEK_START_DAY_DEFAULT);
    }
}
