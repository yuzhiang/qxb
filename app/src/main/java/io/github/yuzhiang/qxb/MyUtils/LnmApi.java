package io.github.yuzhiang.qxb.MyUtils;

import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.model.LearnProfile;
import io.github.yuzhiang.qxb.model.LnmTime;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface LnmApi {

    @POST("/v1/study-modes")
    Observable<Lnm> startLearn(@Query("estimate") String planTime);

    @PUT("/v1/study-modes/{id}")
    Observable<Lnm> finishLearn(@Path("id") long id);

    @DELETE("/v1/study-modes/{id}")
    Observable<ResponseBody> CancelLearn(@Path("id") long id);

    @GET("/v1/study-modes/users")
    Observable<List<Lnm>> getMyLearn(@Query("lastFewDays") int lastFewDays);

    @PUT("/v1/studyMode/{id}/up")
    Observable<LearnProfile> upStudyMode(@Path("id") Long id);

    @GET("/study-modes")
    Observable<Map<String, LnmTime>> getLearn(@Query("query") String query, @Query("lastFewDays") int lastFewDays);
}
