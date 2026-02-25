package io.github.yuzhiang.qxb.MyUtils;

import static io.github.yuzhiang.qxb.common.LdrConfig.error_empty;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_empty_;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_empty_2;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_empty_3;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_local_time;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_local_time_;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_lzu_no_ldr;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_lzu_no_ldr_;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_net_timeout;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_net_timeout_;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_net_timeout_lzu2ldr;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_net_timeout_lzu2ldr_;
import static io.github.yuzhiang.qxb.common.LdrConfig.error_no_user_;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Objects;

import retrofit2.HttpException;

final class LnmErrorUtils {

    private LnmErrorUtils() {
    }

    static String getErrorMsg(Throwable e) {
        String s = e == null ? "" : e.toString();
        try {
            String es = s;
            if (es.contains(error_lzu_no_ldr_)) {
                s = error_lzu_no_ldr;
            } else if (es.contains(error_net_timeout_)) {
                s = error_net_timeout;
            } else if (es.contains(error_net_timeout_lzu2ldr_)) {
                s = error_net_timeout_lzu2ldr;
            } else if (es.contains(error_empty_) || es.contains(error_empty_2)) {
                s = error_empty;
            } else if (es.contains(error_local_time_)) {
                s = error_local_time;
            } else if (es.contains(error_empty_3)) {
                s = "无数据！";
            } else if (e instanceof HttpException httpException) {
                try {
                    String body = Objects.requireNonNull(Objects.requireNonNull(httpException.response()).errorBody()).string();
                    Map<?, ?> map = new Gson().fromJson(body, Map.class);
                    Object msg = map == null ? null : map.get("message");
                    if (msg != null) {
                        s = msg.toString();
                        if (s.contains(error_no_user_)) {
                            s = "查无此人！";
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {
        }
        if ((s == null || s.trim().isEmpty()) && e != null) {
            s = e.getMessage();
        }
        return s == null ? "" : s;
    }
}
