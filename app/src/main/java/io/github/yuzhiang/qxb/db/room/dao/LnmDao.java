package io.github.yuzhiang.qxb.db.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import io.github.yuzhiang.qxb.db.room.bean.Lnm;

import java.util.Date;
import java.util.List;

@Dao
public interface LnmDao {

    @Query("SELECT * FROM StudyMode")
    List<Lnm> getAll();

    @Query("SELECT * FROM StudyMode WHERE stopTime-startTime > 30000")
    List<Lnm> getAll2();

    @Query("SELECT COUNT(id)  FROM StudyMode")
    long count();

    @Query("SELECT COUNT(id)  FROM StudyMode WHERE stopTime-startTime > 30000")
    long count2();

    @Query("SELECT COUNT(id) FROM StudyMode WHERE id = :id")
    long countById(int id);

    @Query("SELECT COUNT(id) FROM StudyMode WHERE id = :id AND stopTime = startTime")
    long countPendingById(int id);

    @Query("SELECT COUNT(id) FROM StudyMode WHERE stopTime = startTime")
    long countPending();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Lnm lnm);

    @Insert
    void insertAll(List<Lnm> lnms);

    @Delete
    void delete(Lnm lnm);


    @Delete
    void delete(List<Lnm> lnms);

    @Query("DELETE FROM StudyMode WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM StudyMode WHERE stopTime = startTime")
    void deletePendingAll();

    @Update
    void update(Lnm lnm);

    @Update
    void update(List<Lnm> lnms);


    @Query("SELECT * FROM StudyMode WHERE stopTime-startTime > 30000  ORDER BY startTime ASC")
    List<Lnm> findByTimeAsc();


    @Query("SELECT * FROM StudyMode WHERE startTime BETWEEN :From AND :To AND stopTime-startTime > 30000 ORDER BY id ASC")
    List<Lnm> findByDate(Date From, Date To);


//    @Query("SELECT * FROM StudyMode WHERE year(now(),interval 6 month) and now() ORDER BY id ASC")
//    List<Lnm> findByDate(Date From);


}
