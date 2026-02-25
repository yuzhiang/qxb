package io.github.yuzhiang.qxb.db.room.dbUtils;

import io.github.yuzhiang.qxb.base.MyApplication;
import io.github.yuzhiang.qxb.db.room.AppDatabase;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dao.LnmDao;

import java.util.Date;
import java.util.List;

public class lnmDBUtils {

    private final static LnmDao lnmDao;

    static {
        lnmDao = AppDatabase.getInstance(MyApplication.getInstances()).lnmDao();
    }

    public static boolean delete(List<Lnm> fees) {
        lnmDao.delete(fees);
        return true;
    }

    public static boolean delete(Lnm fee) {
        lnmDao.delete(fee);
        return true;
    }

    public static boolean deleteById(int id) {
        lnmDao.deleteById(id);
        return true;
    }

    public static boolean deletePendingAll() {
        lnmDao.deletePendingAll();
        return true;
    }

    public static boolean deleteAll() {
        delete(findAll());
        return true;
    }

    public static List<Lnm> findAll() {
        return lnmDao.getAll();
    }

    public static long count() {
        return lnmDao.count();
    }

    public static long countById(int id) {
        return lnmDao.countById(id);
    }

    public static long countPendingById(int id) {
        return lnmDao.countPendingById(id);
    }

    public static long countPending() {
        return lnmDao.countPending();
    }

    public static List<Lnm> findAll2() {
        return lnmDao.getAll2();
    }

    public static boolean update(List<Lnm> fees) {
        lnmDao.update(fees);
        return true;
    }

    public static boolean update(Lnm fee) {
        lnmDao.update(fee);
        return true;
    }

    public static boolean insert(List<Lnm> fees) {
        lnmDao.insertAll(fees);
        return true;
    }

    public static boolean insert(Lnm lnm) {
        lnmDao.insert(lnm);
        return true;
    }

    public static List<Lnm> findByTimeAsc() {
        return lnmDao.findByTimeAsc();
    }

    public static List<Lnm> findBetween(Date start, Date end) {

        return lnmDao.findByDate(start, end);
    }


}
