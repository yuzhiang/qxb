package io.github.yuzhiang.qxb.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.LogUtils;

import io.github.yuzhiang.qxb.BuildConfig;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.adapter.BaiMingDanAdapter;
import io.github.yuzhiang.qxb.base.BaseActivity;
import io.github.yuzhiang.qxb.databinding.ActivityBaiMingDanBinding;
import io.github.yuzhiang.qxb.model.LnmApp;
import io.github.yuzhiang.qxb.model.SearchSimilarity;
import io.github.yuzhiang.qxb.model.SearchSimilarity.LcSubsequence;
import io.github.yuzhiang.qxb.model.eventbus.EBLnmBai;
import io.github.yuzhiang.qxb.view.materialsearchview.MaterialSearchView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaiMingDanActivity extends BaseActivity {

    List<LnmApp> appList = new ArrayList<>();//只因为点击变化被选中状态
    BaiMingDanAdapter baiMIngDanAdapter;

    private ActivityBaiMingDanBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBaiMingDanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setImmersiveView(binding.ablBmd);

        setSupportActionBar(binding.toolbarBai);

        binding.rvBaiMingDan.setLayoutManager(new LinearLayoutManager(BaiMingDanActivity.this));
        baiMIngDanAdapter = new BaiMingDanAdapter(new ArrayList<>());

        baiMIngDanAdapter.addOnItemChildClickListener(R.id.cb_app_select, (adapter, view, position) ->
                baiMIngDanAdapter.getItems().get(position)
                        .setAppSelect(!baiMIngDanAdapter.getItems().get(position).isAppSelect()));

        View empty = LayoutInflater.from(this).inflate(R.layout.rv_empty_view, binding.rvBaiMingDan, false);
        TextView tv = empty.findViewById(R.id.tv_empty_msg);
        tv.setText("加载中……");
        baiMIngDanAdapter.setStateView(empty);
        baiMIngDanAdapter.setStateViewEnable(true);
        binding.rvBaiMingDan.setAdapter(baiMIngDanAdapter);


        new Thread() {
            @Override
            public void run() {//不进行异步，卡顿厉害
                initData();
            }
        }.start();

        binding.tvBaiOk.setOnClickListener(v -> {
            List<LnmApp> apps = new ArrayList<>();

            List<String> stringList = new ArrayList<>();

            for (LnmApp app : baiMIngDanAdapter.getItems()) {
                if (app.isAppSelect()) {
                    apps.add(app);
                    stringList.add(app.getAppPackageName());
                }
            }

            LogUtils.i("=========bmd" + apps.toString());


            toastSe("一共" + apps.size() + "个");


            LogUtils.i(stringList.toString());

            lnm2file.saveEnableApp(stringList);

            EventBus.getDefault().post(new EBLnmBai(true));

            finish();


        });


        binding.searchViewBai.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("=======", "======" + newText);

                if (newText.length() > 0) {

                    List<LnmApp> apps = new ArrayList<>();

                    for (LnmApp app : appList) {
                        newText = newText.toLowerCase();
                        String appName0 = app.getAppName().toLowerCase();

                        LcSubsequence subsequence = new LcSubsequence(newText, appName0);//简称匹配

                        SearchSimilarity.LcSubstring substring = new SearchSimilarity.LcSubstring(newText, appName0);//完全匹配
                        int maxString = substring.calculate();

                        float similarity = subsequence.similarity() + substring.similarity() * 2 + maxString;//newText在appName中的个数//newText长度

                        if (app.isAppSelect()) {
                            similarity = similarity + +100000F;
                        }

                        app.setAppSearchSimilarity(similarity);

                        apps.add(app);

                    }

                    Collections.sort(apps, (o1, o2) -> Float.compare(o2.getAppSearchSimilarity(), o1.getAppSearchSimilarity()));

                    baiMIngDanAdapter.submitList(apps);

                } else {
                    baiMIngDanAdapter.submitList(appList);
                }


                return false;

            }
        });

    }


    protected void initData() {

        List<String> stringList = lnm2file.getEnableApp();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        int all = apps.size();

        String last = "";

        LogUtils.i(all);

        for (int i = 0; i < all; i++) {
            try {

                ResolveInfo info = apps.get(i);
                String packageName = info.activityInfo.packageName;
                if (!last.equals(packageName)) {

                    last = packageName;

                    if (!packageName.equals("com.miui.securitycenter") &&
                            !packageName.equals(BuildConfig.APPLICATION_ID) &&
                            !packageName.equals("com.iqoo.secure") &&
                            !packageName.equals("com.coloros.safecenter") &&
                            !packageName.equals("com.huawei.systemmanager")) {


                        String appName = info.activityInfo.loadLabel(pm).toString();
                        Drawable appIcon = info.activityInfo.loadIcon(pm);

                        if (stringList.contains(packageName)) {
                            appList.add(new LnmApp(appName, packageName, appIcon, stringList.contains(packageName), 1000F));

                        } else {
                            appList.add(new LnmApp(appName, packageName, appIcon, false, 0.1F));
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(appList, (o1, o2) -> Float.compare(o2.getAppSearchSimilarity(), o1.getAppSearchSimilarity()));

        runOnUiThread(() -> baiMIngDanAdapter.submitList(appList));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        binding.searchViewBai.setMenuItem(item);

        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public void onBackPressed() {
        if (binding.searchViewBai.isSearchOpen()) {
            binding.searchViewBai.closeSearch();
        } else {
            super.onBackPressed();
        }
    }


}
