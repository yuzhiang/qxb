package io.github.yuzhiang.qxb.MyUtils;

import static io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils.decryptUserFile;
import static io.github.yuzhiang.qxb.common.LdrConfig.getLdrUsrAgent;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LnmRetrofitUtils {

    private static final String HOST = "https://api.ldr.cool/";
    private static final long TIMEOUT_SEC = 2L;
    private static final Retrofit RETROFIT = build();

    private LnmRetrofitUtils() {
    }

    public static Retrofit getInstance() {
        return RETROFIT;
    }

    public static <T> ObservableTransformer<T, T> schedulersTransformer() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static Retrofit build() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request.Builder builder = chain.request().newBuilder()
                            .addHeader("user-agent", getLdrUsrAgent());
                    String token = getBearerToken();
                    if (!StringUtils.isEmpty(token)) {
                        builder.addHeader("Authorization", token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .baseUrl(HOST)
                .build();
    }

    private static String getBearerToken() {
        String encrypted = SPUtils.getInstance().getString("access_token", "");
        if (StringUtils.isEmpty(encrypted)) {
            return "";
        }
        String token = decryptUserFile(encrypted);
        if (StringUtils.isEmpty(token)) {
            return "";
        }
        return token.startsWith("bearer ") ? token : "bearer " + token;
    }
}
