package io.github.yuzhiang.qxb.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ResourceUtils;

import io.github.yuzhiang.qxb.MyUtils.LnmDateUtils;
import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.adapter.LnmRecyclerViewAdapter;
import io.github.yuzhiang.qxb.adapter.SpinnerAdapterToolbar;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentAllBinding;
import io.github.yuzhiang.qxb.model.LnmTime;
import io.github.yuzhiang.qxb.model.eventbus.MeLnmShowChart;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import io.github.yuzhiang.qxb.view.tastytoast.TastyToast;

import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LnmAllFragment extends LazyFragment {


    private boolean isActive = true;
    private int p = 0;
    private int all = 0;

    private LnmRecyclerViewAdapter lnmAdapter;

    public static LnmAllFragment newInstance() {
        return new LnmAllFragment();
    }

    private Context mContext;

    private int bang;

    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private List<String> query = new ArrayList<>();
    private List<Integer> lastFewDays = new ArrayList<>();

    private LnmFragmentAllBinding binding;


    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_all;
    }


    /**
     * 初始化视图
     *
     * @param view
     */
    @Override
    protected void initView(View view) {
        super.initView(view);

        LogUtils.i("lnmAll======initView");

        mContext = getContext();

        binding = LnmFragmentAllBinding.bind(view);
        StatusBarUtil.setPaddingSmart(mContext, binding.ablLfa);

        isActive = true;

        int xqDay = 0;
        int n = LnmDateUtils.getNowDays();
        if (n > 0) {
            xqDay = n;
        }

        lastFewDays.addAll(new ArrayList<>(Arrays.asList(-1, -1, xqDay, 10 * 365)));
        query.addAll(new ArrayList<>(Arrays.asList("thisWeek", "thisMonth", "", "")));

        binding.SwipeRefreshLayoutLnmAll.setOnRefreshListener(r -> largeEqual());


        linearLayoutManager = new LinearLayoutManager(mContext);
        binding.rvLnm.setLayoutManager(linearLayoutManager);
        lnmAdapter = new LnmRecyclerViewAdapter(new ArrayList<>());

        lnmAdapter.setOnItemClickListener((adapter, view1, position) -> {
            // LNM-only build: disable jump to global user profile page.
        });
        lnmAdapter.addOnItemChildClickListener(R.id.ll_lnm_up, (adapter, view1, position) -> {
            LnmTime lnmTime = (LnmTime) adapter.getItems().get(position);
            LogUtils.i(lnmTime);
            up(lnmTime, position);
        });

        View empty = LayoutInflater.from(mContext).inflate(R.layout.rv_empty_view, binding.rvLnm, false);
        TextView tv = empty.findViewById(R.id.tv_empty_msg);
        tv.setText("好好学习，加油！");
        lnmAdapter.setStateView(empty);
        lnmAdapter.setStateViewEnable(true);

        binding.rvLnm.setAdapter(lnmAdapter);

        binding.lnmName.setText(UsrMsgUtils.getNickName());
        binding.lnmName.setOnClickListener(v -> {

            binding.rvLnm.scrollToPosition(p);
            if (linearLayoutManager != null)
                linearLayoutManager.scrollToPositionWithOffset(p, 0);

        });
        ((AppCompatActivity) mContext).setSupportActionBar(binding.toolbarLnm);

        ((AppCompatActivity) mContext).getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        super.initData();
        LogUtils.i("lnmAll======initData");
        EventBus.getDefault().register(this);

        // Setup spinner
        binding.spinner.setAdapter(new SpinnerAdapterToolbar(
                binding.toolbarLnm.getContext(),
                new String[]{
                        " 周榜",
                        " 月榜",
                        " 学期榜（" + lastFewDays.get(2) + "天）",
                        " 总榜"
                }));

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                bang = position;

                binding.SwipeRefreshLayoutLnmAll.autoRefresh();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    private void largeEqual() {
        try {
            String json = ResourceUtils.readAssets2String("lnmRankDemo");
            List<LnmTime> source = GsonUtils.fromJson(json, new TypeToken<List<LnmTime>>() {
            }.getType());
            if (source == null || source.isEmpty()) {
                source = buildDemoFallback();
            }
            renderLocalDemoRank(source);
        } catch (Exception e) {
            LogUtils.e(e);
            if (isActive) {
                renderLocalDemoRank(buildDemoFallback());
                TastyToast.makeText(mContext, "已切换为本地演示榜单", TastyToast.LENGTH_LONG, TastyToast.INFO);
            }
        } finally {
            if (binding != null) binding.SwipeRefreshLayoutLnmAll.finishRefresh();
        }

    }

    private void up(LnmTime lnmTime, int position) {
        if (lnm2file.getStudyModeUps().contains(lnmTime.getId())) {
            lnmAdapter.notifyItemChanged(position);
            SimToast.toastEe("演示模式：每周对一个人只能点赞一次");
            return;
        }

        lnm2file.saveStudyModeUps(lnmTime.getId());
        int oldLike = lnmTime.getLike() == null ? 0 : lnmTime.getLike();
        lnmTime.setLike(oldLike + 1);
        lnmAdapter.notifyItemChanged(position);
        SimToast.toastSe("点赞成功！（本地演示）");

    }

    private void renderLocalDemoRank(List<LnmTime> source) {
        if (!isActive) return;

        List<LnmTime> result = new ArrayList<>();
        for (LnmTime item : source) {
            if (item == null) continue;
            if (item.getId() == null) item.setId(System.currentTimeMillis());
            if (item.getSum() == null) item.setSum(0);
            item.setUpShow(bang < 2);
            result.add(item);
        }

        Collections.sort(result, (o1, o2) -> Integer.compare(o2.getSum(), o1.getSum()));
        lnmAdapter.submitList(result);

        all = result.size();
        p = 0;
        String name = UsrMsgUtils.getNickName() + "（本地演示榜单）";
        long myId = -1L;
        try {
            myId = UsrMsgUtils.getUserModel().getId();
        } catch (Exception ignored) {
        }
        for (int i = 0; i < all; i++) {
            if (result.get(i).getId() != null && result.get(i).getId().equals(myId)) {
                p = i;
                name = UsrMsgUtils.getNickName() + "：第" + (p + 1) + "名（演示）";
                break;
            }
        }
        binding.lnmName.setText(name);
    }

    private List<LnmTime> buildDemoFallback() {
        List<LnmTime> list = new ArrayList<>();
        list.add(mockLnmTime(10001L, "自习室A", "专注就是复利", 1560, 12));
        list.add(mockLnmTime(10002L, "图书馆B", "先学45分钟", 1320, 8));
        list.add(mockLnmTime(10003L, "晚课C", "今天也别摸鱼", 1180, 6));
        list.add(mockLnmTime(10004L, "清晨D", "一点点推进", 980, 5));
        return list;
    }

    private LnmTime mockLnmTime(Long id, String nickName, String signature, int sum, int like) {
        LnmTime item = new LnmTime();
        item.setId(id);
        item.setNickName(nickName);
        item.setSignature(signature);
        item.setSum(sum);
        item.setLike(like);
        item.setAvatar("");
        item.setXh(id);
        return item;
    }


    /**
     * onDestroyView中进行解绑操作
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        isActive = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MeLnmShowChart meLnmShowChart) {
        if (meLnmShowChart.isLearnFinish())
            binding.SwipeRefreshLayoutLnmAll.autoRefresh();
    }


}
