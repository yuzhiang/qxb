package io.github.yuzhiang.qxb.db.room;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<Integer> fromInteger(String value) {
        Type listType = new TypeToken<List<Integer>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromListToInteger(List<Integer> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromListToString(List<String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }


//        @TypeConverter
//        public static Date fromTimestamp(String  value) {
//            return value == null ? null : TimeUtils.string2Date(value);
//        }
//
//        @TypeConverter
//        public static String dateToTimestamp(Date date) {
//            return date == null ? null : TimeUtils.date2String(date);
//        }

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
