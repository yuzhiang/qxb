package io.github.yuzhiang.qxb.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;

import io.github.yuzhiang.qxb.fragment.PlaceholderLnmRecordFragment;
import io.github.yuzhiang.qxb.adapter.Viewpager2Adapter;
import io.github.yuzhiang.qxb.base.BaseActivity;
import io.github.yuzhiang.qxb.databinding.ActivityLnmRecordBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LnmRecordActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLnmRecordBinding binding = ActivityLnmRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setImmersiveView(binding.ablLr);

        List<String> TabTitle = Arrays.asList("周", "月", "年", "总");

        for (int i = 0; i < TabTitle.size(); i++)
            binding.tabsLnmRecord.addTab(binding.tabsLnmRecord.newTab().setText(TabTitle.get(i)), true);//设置默认选中

        List<Fragment> mFragments = new ArrayList<>();
        for (int i = 0; i < TabTitle.size(); i++) {
            mFragments.add(PlaceholderLnmRecordFragment.newInstance(i));
        }
        binding.vp2LnmRecord.setAdapter(new Viewpager2Adapter(this, mFragments));
        binding.vp2LnmRecord.setCurrentItem(0);
        binding.vp2LnmRecord.setOffscreenPageLimit(mFragments.size());
//        binding.vp2LnmRecord.setUserInputEnabled(false);

        new TabLayoutMediator(binding.tabsLnmRecord, binding.vp2LnmRecord, true, (tab, position) -> {
            //这里需要根据position修改tab的样式和文字等
            tab.setText(TabTitle.get(position));
        }).attach();

        binding.tvLnmRecordTitle.setOnClickListener(v -> finish());
        binding.ivShareLnm.setOnClickListener(v -> {
            toastEe("你自己截图吧，我不想做这个功能了……");
        });

    }
}
