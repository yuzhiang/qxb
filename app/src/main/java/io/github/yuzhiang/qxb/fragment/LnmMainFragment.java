package io.github.yuzhiang.qxb.fragment;

import static com.blankj.utilcode.util.ScreenUtils.getScreenHeight;
import static com.blankj.utilcode.util.ScreenUtils.getScreenWidth;
import static io.github.yuzhiang.qxb.model.lnm2file.clearManualCancelMark;
import static io.github.yuzhiang.qxb.model.lnm2file.errorSpan;
import static io.github.yuzhiang.qxb.model.lnm2file.finishLearn;
import static io.github.yuzhiang.qxb.model.lnm2file.getLastTime;
import static io.github.yuzhiang.qxb.model.lnm2file.getManualCancelAt;
import static io.github.yuzhiang.qxb.model.lnm2file.getThisId;
import static io.github.yuzhiang.qxb.model.lnm2file.okSpan;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmBg;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmStart;
import static io.github.yuzhiang.qxb.common.Constant.Constant.lnmState;
import static io.github.yuzhiang.qxb.MyUtils.LnmRetrofitUtils.schedulersTransformer;
import static io.github.yuzhiang.qxb.view.pickpic.PicUtils.picSel;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastEL;
import static io.github.yuzhiang.qxb.view.tastytoast.SimToast.toastSe;
import static autodispose2.AutoDispose.autoDisposable;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.MyUtils.UsrMsgUtils;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.Service.DetectService;
import io.github.yuzhiang.qxb.activity.BaiMingDanActivity;
import io.github.yuzhiang.qxb.activity.PermissionActivity;
import io.github.yuzhiang.qxb.activity.StartLearnActivity;
import io.github.yuzhiang.qxb.activity.LnmRecordActivity;
import io.github.yuzhiang.qxb.adapter.EnableAppAdapter;
import io.github.yuzhiang.qxb.base.BaseDialog;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentMainBinding;
import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dbUtils.lnmDBUtils;
import io.github.yuzhiang.qxb.model.LnmApp;
import io.github.yuzhiang.qxb.model.eventbus.EBLnmBai;
import io.github.yuzhiang.qxb.model.eventbus.MeLnmShowChart;
import io.github.yuzhiang.qxb.model.focus.FocusRulePrefs;
import io.github.yuzhiang.qxb.model.focus.SleepReportStore;

import io.github.yuzhiang.qxb.model.eventbus.TodoImportantChanged;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoPrefs;
import io.github.yuzhiang.qxb.model.todo.TodoTimeUtils;
import io.github.yuzhiang.qxb.model.lnm2file;
import io.github.yuzhiang.qxb.view.dialog.InputDialog;
import io.github.yuzhiang.qxb.view.dialog.MessageDialog;
import io.github.yuzhiang.qxb.view.dialog.SelectDialog;
import io.github.yuzhiang.qxb.view.dialog.UIDialog;
import io.github.yuzhiang.qxb.view.pickpic.ImageCropEngine;
import io.github.yuzhiang.qxb.view.pickpic.ImageFileCompressEngine;
import io.github.yuzhiang.qxb.view.pickpic.PicUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;
import io.github.yuzhiang.qxb.view.tastytoast.TastyToast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class LnmMainFragment extends LazyFragment {
    private static final long RESUME_PROMPT_WINDOW_MS = 30 * 1000L;
    private static final long MANUAL_CANCEL_SUPPRESS_RESUME_MS = 10 * 1000L;

    private Context mContext;

    private LnmFragmentMainBinding binding;
    private EnableAppAdapter enableAppAdapter;
    private long spanTime = 0;

    public static LnmMainFragment newInstance() {
        return new LnmMainFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_main;
    }


    /**
     * 初始化视图
     *
     * @param view
     */
    @Override
    protected void initView(View view) {
        super.initView(view);

        LogUtils.i("lnmMain======initView");

        mContext = getContext();

        binding = LnmFragmentMainBinding.bind(view);
        StatusBarUtil.setPaddingSmart(mContext, binding.lnmTitle);

        updateImportantBanner();
        binding.tvProjectManage.setOnClickListener(v -> showManageProjectsDialog(this::startLearnAfterProjectSelected));
        binding.tvLnmHistory.setOnClickListener(v -> startActivity(new Intent(mContext, LnmRecordActivity.class)));

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.rvLnmMain.setLayoutManager(layoutManager);

        enableAppAdapter = new EnableAppAdapter(new ArrayList<>());
        binding.rvLnmMain.setAdapter(enableAppAdapter);
        //设置监听

    }

    @Override
    protected void initEvent() {
        super.initEvent();


        enableAppAdapter.setOnItemClickListener((adapter, view, position) -> {

            try {
                String appPackageName = enableAppAdapter.getItems().get(position).getAppPackageName();
                LogUtils.i(appPackageName);
                Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appPackageName);
                if (intent != null) {
                    startActivity(intent);
                } else {
                    SimToast.toastEe("打开错误，软件不存在：" + AppUtils.getAppName(appPackageName));
                }
            } catch (Exception e) {
                LogUtils.i(e.toString());
                e.printStackTrace();
            }

        });

        binding.tvCheckPm.setOnClickListener(v -> startActivity(new Intent(mContext, PermissionActivity.class)));

        binding.tvLnmPic.setOnClickListener(v -> {


            Bitmap bitmap = ImageUtils.getBitmap(lnmBg);

            if (bitmap != null) {

                new MessageDialog.Builder(mContext)
                        .setTitle("注意！")
                        .setMessage("发现用户已经更换过背景，你是准备？")
                        .setTextGravity(Gravity.CENTER)
                        .setConfirm("更换背景图")
                        .setCancel("恢复默认背景图")
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                getBackGround();
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                                binding.clLnmMain.setBackground(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                            }
                        })
                        .show();

            } else {
                getBackGround();
            }

        });

        binding.lnmTitle.setOnClickListener(view -> SimToast.toastEe("请在我的界面修改个性签名"));


        binding.tvStartLnm.setOnClickListener(v -> {
//            if (noNickName.equals(UsrMsgUtils.getNickName())) {
//                SimToast.toastEL("未设置“昵称”，也可能是注册失败，请前往“我的”查看");
//                startActivity(new Intent(mContext, MangeUserActivity.class));
//                return;
//            }
            if (!hasStartPermissions()) {
                SimToast.toastSL("请先授予相关的权限");
                startActivity(new Intent(mContext, PermissionActivity.class));
                return;
            }

            long id = getThisId();
            if (id > 0) {
                if (lnmDBUtils.countById((int) id) <= 0) {
                    finishLearn();
                    clearManualCancelMark();
                    id = -1;
                } else if (shouldResumeLearn()) {
                    showResumeLearnDialog();
                    return;
                } else {
                    findLastLearn(id);
                    return;
                }
            }
            if (tryStartSleepModeDirectly()) {
                return;
            }

            showStudyProjectSelector(this::startLearnAfterProjectSelected);
        });

        binding.tvBai.setOnClickListener(v -> startActivity(new Intent(mContext, BaiMingDanActivity.class)));


    }

    /**
     * 初始化数据
     */
    @Override
    protected void initData() {
        super.initData();
        EventBus.getDefault().register(this);

        LogUtils.i("lnm==========register");

        setBackGround();

        binding.lnmTitle.setText(UsrMsgUtils.getSignature());
        updateApps();
        updateProjectManageLabel();
    }

    private void setBackGround() {

        Observable.create((ObservableOnSubscribe<Drawable>) emitter -> {
                    if (FileUtils.isFile(lnmBg)) {
                        try {
                            emitter.onNext(new BitmapDrawable(getResources(), ImageUtils.getBitmap(lnmBg)));
                        } catch (Exception e) {
                            emitter.onNext(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                            e.printStackTrace();
                        }
                    } else {
                        emitter.onNext(ContextCompat.getDrawable(mContext, R.mipmap.table_bg));
                    }
                    emitter.onComplete();

                })
                .compose(schedulersTransformer())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        binding.clLnmMain.setBackground(drawable);
                    }

                    @Override
                    public void onError(Throwable e) {
                        SimToast.toastEe("背景设置错误！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    private void getBackGround() {

        final int width = getScreenWidth();//宽度
        int height = getScreenHeight();//高度

        picSel().setCropEngine(new ImageCropEngine(width, height))
                .setCompressEngine(new ImageFileCompressEngine())
                .forResult(new OnResultCallbackListener<>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result.size() == 1) {
                            FileUtils.copy(PicUtils.getRealLocalPath(result.get(0)), lnmBg);
                            setBackGround();
                        } else {
                            SimToast.toastEe("请重新选择：图片数目错误" + result.size());
                        }
                    }

                    @Override
                    public void onCancel() {
                        // 取消
                    }
                });

    }



    private void showImportantActionDialog(TodoItem important) {
        if (important == null) return;
        String[] items = new String[]{"编辑", "删除"};
        new SelectDialog.Builder(mContext)
                .setTitle("重要目标")
                .setList(items)
                .setSingleSelect()
                .setListener((dialog, data) -> {
                    if (data == null || data.isEmpty()) return;
                    Object selectedKey = data.keySet().iterator().next();
                    int which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                    if (which == 0) {
                        showEditImportantDialog(important);
                    } else {
                        TodoPrefs.saveImportant(null);
                        EventBus.getDefault().post(new TodoImportantChanged(null));
                        updateImportantBanner();
                    }
                })
                .show();
    }

    private void showEditImportantDialog(TodoItem item) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_todo, null);
        EditText etTitle = view.findViewById(R.id.et_todo_title);
        View categorySection = view.findViewById(R.id.rg_category_mode);
        TextView tvCategoryLabel = view.findViewById(R.id.tv_category_label);
        Spinner spCategory = view.findViewById(R.id.sp_category);
        EditText etCategoryNew = view.findViewById(R.id.et_category_new);
        RadioButton rbRepeatYes = view.findViewById(R.id.rb_repeat_yes);
        RadioButton rbRepeatNo = view.findViewById(R.id.rb_repeat_no);
        View rgRepeatUnit = view.findViewById(R.id.rg_repeat_unit);
        RadioButton rbRepeatDay = view.findViewById(R.id.rb_repeat_day);
        RadioButton rbRepeatMonth = view.findViewById(R.id.rb_repeat_month);
        RadioButton rbRepeatYear = view.findViewById(R.id.rb_repeat_year);
        RadioButton rbNonRepeatCountdown = view.findViewById(R.id.rb_non_repeat_countdown);
        RadioButton rbNonRepeatDate = view.findViewById(R.id.rb_non_repeat_date);
        View layoutCountdown = view.findViewById(R.id.layout_countdown);
        View layoutDate = view.findViewById(R.id.layout_date);
        TextView tvPickDate = view.findViewById(R.id.tv_pick_date);
        TextView tvPickTime = view.findViewById(R.id.tv_pick_time);
        android.widget.Switch swImportant = view.findViewById(R.id.sw_important);

        swImportant.setChecked(true);
        swImportant.setEnabled(false);
        categorySection.setVisibility(View.GONE);
        tvCategoryLabel.setVisibility(View.GONE);
        spCategory.setVisibility(View.GONE);
        etCategoryNew.setVisibility(View.GONE);

        etTitle.setText(item.getTitle());
        if (item.isRepeat()) {
            rbRepeatYes.setChecked(true);
            rgRepeatUnit.setVisibility(View.VISIBLE);
            rbRepeatNo.setChecked(false);
            String unit = item.getRepeatUnit();
            if ("月".equals(unit)) rbRepeatMonth.setChecked(true);
            else if ("年".equals(unit)) rbRepeatYear.setChecked(true);
            else rbRepeatDay.setChecked(true);
        } else {
            rbRepeatNo.setChecked(true);
            rgRepeatUnit.setVisibility(View.GONE);
        }

        rbRepeatYes.setOnCheckedChangeListener((buttonView, isChecked) -> rgRepeatUnit.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(item.getDueAt());
        rbNonRepeatDate.setChecked(true);
        rbNonRepeatCountdown.setChecked(false);
        layoutCountdown.setVisibility(View.GONE);
        layoutDate.setVisibility(View.VISIBLE);
        tvPickDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", selected.get(Calendar.YEAR), selected.get(Calendar.MONTH) + 1, selected.get(Calendar.DAY_OF_MONTH)));
        tvPickTime.setText(String.format(Locale.CHINA, "%02d:%02d", selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE)));

        tvPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(mContext, R.style.DatePickerDialogTheme,
                    (view1, year, month, dayOfMonth) -> {
                        selected.set(Calendar.YEAR, year);
                        selected.set(Calendar.MONTH, month);
                        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        tvPickDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", year, month + 1, dayOfMonth));
                    },
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH),
                    selected.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });
        tvPickTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(mContext, R.style.TimePickerDialogTheme,
                    (view12, hourOfDay, minute) -> {
                        selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selected.set(Calendar.MINUTE, minute);
                        selected.set(Calendar.SECOND, 0);
                        tvPickTime.setText(String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minute));
                    },
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE),
                    true);
            dialog.show();
        });

        new UIDialog.Builder(mContext) {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.tv_ui_cancel) {
                    dismiss();
                    return;
                }
                if (v.getId() != R.id.tv_ui_confirm) {
                    return;
                }
                String title = etTitle.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    SimToast.toastEL("请输入作业名称");
                    return;
                }
                boolean repeat = rbRepeatYes.isChecked();
                String repeatUnit = null;
                long dueAt;
                if (repeat) {
                    Calendar repeatCal = Calendar.getInstance();
                    if (rbRepeatDay.isChecked()) {
                        repeatUnit = "天";
                        repeatCal.add(Calendar.DAY_OF_YEAR, 1);
                    } else if (rbRepeatMonth.isChecked()) {
                        repeatUnit = "月";
                        repeatCal.add(Calendar.MONTH, 1);
                    } else {
                        repeatUnit = "年";
                        repeatCal.add(Calendar.YEAR, 1);
                    }
                    dueAt = repeatCal.getTimeInMillis();
                } else {
                    dueAt = selected.getTimeInMillis();
                    if (dueAt <= System.currentTimeMillis()) {
                        SimToast.toastEL("请选择未来的时间");
                        return;
                    }
                }

                item.setTitle(title);
                item.setRepeat(repeat);
                item.setRepeatUnit(repeatUnit);
                item.setDueAt(dueAt);
                TodoPrefs.saveImportant(item);
                EventBus.getDefault().post(new TodoImportantChanged(item));
                updateImportantBanner();
                dismiss();
            }
        }
                .setTitle("编辑重要目标")
                .setCustomView(view)
                .setCancel("取消")
                .setConfirm("保存")
                .setAutoDismiss(false)
                .show();
    }


    private void updateProjectManageLabel() {
        String selected = lnm2file.getSelectedStudyProject();
        if (selected == null || selected.trim().isEmpty()) {
            binding.tvProjectManage.setText("选择学科");
        } else {
            binding.tvProjectManage.setText("当前：" + selected);
        }
    }

    private void showStudyProjectSelector(Runnable onSelected) {
        List<String> projects = lnm2file.getStudyProjects();
        if (projects.isEmpty()) {
            showAddProjectDialog(() -> showStudyProjectSelector(onSelected));
            return;
        }
        String selected = lnm2file.getSelectedStudyProject();
        int checked = projects.indexOf(selected);
        if (checked < 0) checked = 0;
        final int defaultIndex = checked;
        new SelectDialog.Builder(mContext)
                .setTitle("选择学科")
                .setList(projects)
                .setConfirm("开始")
                .setCancel("管理学科")
                .setSingleSelect()
                .setSelect(checked)
                .setListener(new SelectDialog.OnListener<Object>() {
                    @Override
                    public void onSelected(BaseDialog dialog, HashMap<Integer, Object> data) {
                        int which = defaultIndex;
                        if (data != null && !data.isEmpty()) {
                            Object selectedKey = data.keySet().iterator().next();
                            which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                        }
                        if (which < 0 || which >= projects.size()) return;
                        String name = projects.get(which);
                        lnm2file.saveSelectedStudyProject(name);
                        updateProjectManageLabel();
                        onSelected.run();
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        showManageProjectsDialog(onSelected);
                    }
                })
                .show();
    }

    private void showManageProjectsDialog(Runnable onSelected) {
        List<String> projects = new ArrayList<>(lnm2file.getStudyProjects());
        if (projects.isEmpty()) {
            showAddProjectDialog(() -> showManageProjectsDialog(onSelected));
            return;
        }
        int checked = 0;
        String selected = lnm2file.getSelectedStudyProject();
        int selectedIndex = projects.indexOf(selected);
        if (selectedIndex >= 0) checked = selectedIndex;
        final int defaultIndex = checked;
        new SelectDialog.Builder(mContext)
                .setTitle("管理学科")
                .setList(projects)
                .setConfirm("管理选中")
                .setCancel("新增学科")
                .setSingleSelect()
                .setSelect(checked)
                .setListener(new SelectDialog.OnListener<Object>() {
                    @Override
                    public void onSelected(BaseDialog dialog, HashMap<Integer, Object> data) {
                        int which = defaultIndex;
                        if (data != null && !data.isEmpty()) {
                            Object selectedKey = data.keySet().iterator().next();
                            which = selectedKey instanceof Integer ? (Integer) selectedKey : Integer.parseInt(String.valueOf(selectedKey));
                        }
                        if (which < 0 || which >= projects.size()) return;
                        showProjectActionDialog(projects, which, onSelected);
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        showAddProjectDialog(() -> showManageProjectsDialog(onSelected));
                    }
                })
                .show();
    }

    private void showProjectActionDialog(List<String> projects, int index, Runnable onSelected) {
        if (projects == null || index < 0 || index >= projects.size()) return;
        String current = projects.get(index);
        new MessageDialog.Builder(mContext)
                .setTitle(current)
                .setMessage("请选择操作")
                .setConfirm("编辑")
                .setCancel("删除")
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        showEditProjectDialog(projects, index, onSelected);
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        lnm2file.moveStudyProjectRecordsToDeleted(current);
                        projects.remove(index);
                        lnm2file.saveStudyProjects(projects);
                        if (current.equals(lnm2file.getSelectedStudyProject())) {
                            lnm2file.saveSelectedStudyProject(projects.isEmpty() ? "" : projects.get(0));
                        }
                        showManageProjectsDialog(onSelected);
                    }
                })
                .show();
    }

    private void showAddProjectDialog(Runnable onDone) {
        new InputDialog.Builder(mContext)
                .setTitle("新增学科")
                .setHint("输入学科")
                .setCancel("取消")
                .setConfirm("保存")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String name = content == null ? "" : content.trim();
                        if (name.isEmpty()) {
                            SimToast.toastEL("学科名称不能为空");
                            return;
                        }
                        List<String> projects = new ArrayList<>(lnm2file.getStudyProjects());
                        projects.add(name);
                        lnm2file.saveStudyProjects(projects);
                        lnm2file.saveSelectedStudyProject(name);
                        updateProjectManageLabel();
                        onDone.run();
                    }
                })
                .show();
    }

    private void showEditProjectDialog(List<String> projects, int index, Runnable onSelected) {
        if (projects == null || index < 0 || index >= projects.size()) return;
        String current = projects.get(index);
        new InputDialog.Builder(mContext)
                .setTitle("编辑学科")
                .setContent(current)
                .setCancel("取消")
                .setConfirm("保存")
                .setListener(new InputDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog, String content) {
                        String name = content == null ? "" : content.trim();
                        if (name.isEmpty()) {
                            SimToast.toastEL("学科名称不能为空");
                            return;
                        }
                        projects.set(index, name);
                        lnm2file.saveStudyProjects(projects);
                        lnm2file.renameStudyProjectInRecords(current, name);
                        if (current.equals(lnm2file.getSelectedStudyProject())) {
                            lnm2file.saveSelectedStudyProject(name);
                        }
                        showManageProjectsDialog(onSelected);
                    }
                })
                .show();
    }

    private void startLearnAfterProjectSelected() {
        if (!hasStartPermissions()) {
            SimToast.toastSL("请先授予相关的权限");
            startActivity(new Intent(mContext, PermissionActivity.class));
            return;
        }
        if (tryStartSleepModeDirectly()) {
            return;
        }

        new TimePickerDialog(mContext, R.style.TimePickerDialogTheme, (view, hourOfDay, minute) -> {

                long inputTime = hourOfDay * 60 * 60 * 1000 + minute * 60 * 1000;
                if (inputTime > 4 * 60 * 60 * 1000) {

                    new MessageDialog.Builder(mContext)
                            .setTitle("提示")
                            .setMessage("你确定要持续专注超过4个小时？")
                            .setConfirm("确定")
                            .setCancel("点错了")
                            .setCancelable(false)
                            .setListener(new MessageDialog.OnListener() {
                                @Override
                                public void onConfirm(BaseDialog dialog) {
                                    SimToast.toastEe("确定也不行…");

                                }

                                @Override
                                public void onCancel(BaseDialog dialog) {
                                    SimToast.toastSe("重新选择吧…");
                                }
                            }).show();

                } else {

                    if (UsrMsgUtils.getSignature().isEmpty()) {
                        TastyToast.makeText(mContext, "记得在“我的”界面修改个人签名", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                    }

                    lnm2file.saveTime(hourOfDay, minute);
                    lnm2file.savePlanSpan(inputTime);


                    Intent intent = new Intent(mContext, StartLearnActivity.class);
                    intent.putExtra(lnmState, lnmStart);
                    startActivity(intent);

                }

        }, lnm2file.getHourOfDay(), lnm2file.getMinute(), true)
                .show();
    }

    private boolean hasStartPermissions() {
        boolean access = DetectService.isAccessibilitySettingsOn(mContext);
        boolean timeOk = Math.abs(spanTime) <= okSpan;
        LogUtils.i(access, timeOk);
        return access && timeOk;
    }

    private boolean tryStartSleepModeDirectly() {
        FocusRulePrefs.RuleConfig cfg = FocusRulePrefs.load();
        if (cfg == null || !cfg.enabled || !isInSleepWindow(cfg)) {
            return false;
        }
        long now = System.currentTimeMillis();
        long endAt = calcSleepEndAtMs(cfg);
        long span = endAt > 0 ? Math.max(60_000L, endAt - now) : 0L;
        if (span > 0) {
            lnm2file.savePlanSpan(span);
        }
        cfg.tempPassUntil = 0L;
        FocusRulePrefs.save(cfg);
        FocusRulePrefs.setSleepAutoActive(true);
        SleepReportStore.startSession(now, endAt);

        Intent intent = new Intent(mContext, StartLearnActivity.class);
        intent.putExtra(lnmState, lnmStart);
        intent.putExtra(StartLearnActivity.EXTRA_SLEEP_AUTO, true);
        startActivity(intent);
        return true;
    }

    private boolean isInSleepWindow(FocusRulePrefs.RuleConfig cfg) {
        if (cfg == null || !cfg.enabled) return false;
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        int nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        FocusRulePrefs.TimeWindow w = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        return w != null && w.contains(nowMin);
    }

    private long calcSleepEndAtMs(FocusRulePrefs.RuleConfig cfg) {
        if (cfg == null || !cfg.enabled) return 0L;
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        boolean weekend = (day == Calendar.SATURDAY || day == Calendar.SUNDAY);
        FocusRulePrefs.TimeWindow w = weekend ? cfg.weekendSleep : cfg.schoolSleep;
        if (w == null) return 0L;
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        if (!w.contains(nowMin)) return 0L;
        int endMin = w.endMin;
        Calendar end = (Calendar) now.clone();
        if (w.startMin < w.endMin) {
            end.set(Calendar.HOUR_OF_DAY, endMin / 60);
            end.set(Calendar.MINUTE, endMin % 60);
        } else {
            if (nowMin >= w.startMin) {
                end.add(Calendar.DAY_OF_MONTH, 1);
            }
            end.set(Calendar.HOUR_OF_DAY, endMin / 60);
            end.set(Calendar.MINUTE, endMin % 60);
        }
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        return end.getTimeInMillis();
    }

    private void updateApps() {

        List<String> stringList = lnm2file.getEnableApp();

        LogUtils.i("===========" + stringList.toString());
        List<LnmApp> lnmApps = new ArrayList<>();

        for (String packageName : stringList) {
            lnmApps.add(new LnmApp(AppUtils.getAppName(packageName), AppUtils.getAppIcon(packageName), packageName));
        }
        enableAppAdapter.submitList(lnmApps);
    }


    private void updateImportantBanner() {
        TodoItem important = TodoPrefs.loadImportant();
        if (important == null) {
            binding.includeImportantBanner.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.includeImportantBanner.getRoot().setVisibility(View.VISIBLE);
        binding.includeImportantBanner.tvImportantTitle.setText(important.getTitle());
        binding.includeImportantBanner.tvImportantDays.setText(TodoTimeUtils.formatImportantDays(important.getDueAt()));
        binding.includeImportantBanner.getRoot().setOnClickListener(v -> {
            showImportantActionDialog(important);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTodoImportantChanged(TodoImportantChanged event) {
        updateImportantBanner();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EBLnmBai ebLnmBai) {

        if (ebLnmBai.isOk()) updateApps();
    }

    @Override
    public void onStart() {
        super.onStart();
        getWebsiteDatetime();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LogUtils.i("lnm==========unregister");
    }


    private void getWebsiteDatetime() {

        new Thread() {
            @Override
            public void run() {//不进行异步，卡顿厉害
                try {
//                    getNetTime();
//                    getNetTime2();
                    String s = "https://baidu.com";
                    URLConnection uc = new URL(s).openConnection();// 生成连接对象
                    uc.connect();// 发出连接
                    spanTime = uc.getDate() - System.currentTimeMillis();

                    LogUtils.file("轻学伴时间 - 手机时间 = " + spanTime);

                } catch (IOException e) {
                    spanTime = errorSpan;
                    LogUtils.file("\n\n 时间核对失败\n" + e.toString());
                    LogUtils.i(e.toString());
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void findLastLearn(long id) {

        Date plan = lnm2file.getPlanTime();
        Date start = lnm2file.getStartTime();

        if (plan == null || start == null) {
            new MessageDialog.Builder(mContext)
                    .setTitle("注意！")
                    .setMessage("检测到残留专注状态，已无法恢复，本次记录将清除。")
                    .setCancelable(false)
                    .setConfirm("清除")
                    .setListener(new MessageDialog.OnListener() {
                        @Override
                        public void onConfirm(BaseDialog dialog) {
                            cancelLastLearn(id);
                        }
                    })
                    .show();

            return;
        }

        String s = "发现有以前的本地专注数据未处理！\n\n";
        double time = Math.ceil(TimeUtils.getTimeSpan(plan, start, TimeConstants.SEC) / 60.0);
        s = s + "上次计划专注约" + time + "分钟，未在中断后及时返回。\n将记为专注失败（用于统计完成率）。";

        new MessageDialog.Builder(mContext)
                .setTitle("注意！")
                .setMessage(s)
                .setCancelable(false)
                .setConfirm("记为失败")
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        LogUtils.file("\n\n处理上次本地专注记录 ~ ");
                        LogUtils.i("处理上次本地专注记录 ~ ");
                        failLastLearn(id);
                    }
                })
                .show();

    }

    private boolean shouldResumeLearn() {
        long id = getThisId();
        Date last = getLastTime();
        long manualCancelAt = getManualCancelAt();
        boolean recentInterrupted = last != null
                && System.currentTimeMillis() - last.getTime() <= RESUME_PROMPT_WINDOW_MS;
        boolean justManuallyCancelled = manualCancelAt > 0
                && System.currentTimeMillis() - manualCancelAt <= MANUAL_CANCEL_SUPPRESS_RESUME_MS;
        return id > 0
                && recentInterrupted
                && !justManuallyCancelled
                && lnmDBUtils.countPendingById((int) id) > 0;
    }

    private void showResumeLearnDialog() {
        new MessageDialog.Builder(mContext)
                .setTitle("继续专注")
                .setMessage("检测到上次专注仍在进行中，是否继续？")
                .setCancelable(false)
                .setConfirm("继续")
                .setCancel("放弃本次")
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        Intent intent = new Intent(mContext, StartLearnActivity.class);
                        intent.putExtra(lnmState, lnmStart);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        long id = getThisId();
                        if (id > 0) failLastLearn(id);
                    }
                })
                .show();
    }

    private void finishLastLearn(long id) {
        Date start = lnm2file.getStartTime();
        Date plan = lnm2file.getPlanTime();
        if (start == null || plan == null) {
            toastEL("本地记录缺失，无法恢复");
            cancelLastLearn(id);
            return;
        }

        Lnm lnm = new Lnm();
        lnm.id = (int) id;
        lnm.createdDate = start;
        lnm.schedule = plan;
        lnm.endTime = new Date();
        lnm.finish = true;
        lnmDBUtils.insert(lnm);
        EventBus.getDefault().post(new MeLnmShowChart(true));

        LogUtils.file("\n\n上次专注记录本地保存成功！");
        toastSe("上次专注记录已保存到本地！\n可以开始专注");
        finishLearn();

    }

    private void cancelLastLearn(long id) {
        lnmDBUtils.deleteById((int) id);
        toastSe("成功！\n可以开始专注");
        finishLearn();

        try {
            LogUtils.file("\n\n上次专注取消(本地)：id-" + id);
        } catch (Exception e) {
            toastSe("错误！请重试");
            LogUtils.file("\n\n上次专注取消(本地)：记录错误" + e);
            e.printStackTrace();
        }
    }

    private void failLastLearn(long id) {
        Date start = lnm2file.getStartTime();
        Date plan = lnm2file.getPlanTime();
        if (start == null || plan == null) {
            toastEL("本地记录缺失，无法标记失败");
            cancelLastLearn(id);
            return;
        }

        Date last = lnm2file.getLastTime();
        Date end = last == null ? new Date() : last;
        if (end.before(start)) end = start;

        Lnm lnm = new Lnm();
        lnm.id = (int) id;
        lnm.createdDate = start;
        lnm.schedule = plan;
        lnm.endTime = end;
        lnm.finish = false;
        lnmDBUtils.insert(lnm);
        EventBus.getDefault().post(new MeLnmShowChart(true));

        LogUtils.file("\n\n上次专注记录已记为失败：id-" + id);
        toastSe("上次中断专注已记为失败\n可以开始专注");
        finishLearn();
    }

}
