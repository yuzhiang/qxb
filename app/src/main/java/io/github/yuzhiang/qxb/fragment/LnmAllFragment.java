package io.github.yuzhiang.qxb.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import io.github.yuzhiang.qxb.MyUtils.StatusBarUtil;
import io.github.yuzhiang.qxb.R;
import io.github.yuzhiang.qxb.adapter.TodoListAdapter;
import io.github.yuzhiang.qxb.base.LazyFragment;
import io.github.yuzhiang.qxb.databinding.LnmFragmentAllBinding;
import io.github.yuzhiang.qxb.model.eventbus.TodoImportantChanged;
import io.github.yuzhiang.qxb.model.todo.TodoGroup;
import io.github.yuzhiang.qxb.model.todo.TodoItem;
import io.github.yuzhiang.qxb.model.todo.TodoListEntry;
import io.github.yuzhiang.qxb.model.todo.TodoPrefs;
import io.github.yuzhiang.qxb.model.todo.TodoTimeUtils;
import io.github.yuzhiang.qxb.view.tastytoast.SimToast;

public class LnmAllFragment extends LazyFragment {

    private static final String DEFAULT_GROUP_NAME = "未分类";

    private Context mContext;
    private LnmFragmentAllBinding binding;
    private TodoListAdapter adapter;

    private final List<TodoGroup> groups = new ArrayList<>();
    private final Set<String> stickyGroups = new HashSet<>();
    private final Set<String> expandedGroups = new HashSet<>();

    public static LnmAllFragment newInstance() {
        return new LnmAllFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.lnm_fragment_all;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        LogUtils.i("todo======initView");

        mContext = getContext();
        binding = LnmFragmentAllBinding.bind(view);
        StatusBarUtil.setPaddingSmart(mContext, binding.ablTodo);

        adapter = new TodoListAdapter();
        adapter.setListener(new TodoListAdapter.Listener() {
            @Override
            public void onGroupClick(int position, TodoListEntry entry) {
                String groupId = entry.getGroup().getId();
                if (expandedGroups.contains(groupId)) {
                    expandedGroups.remove(groupId);
                } else {
                    expandedGroups.add(groupId);
                }
                rebuildList();
            }

            @Override
            public void onGroupLongClick(int position, TodoListEntry entry) {
                showGroupActionDialog(entry.getGroup());
            }

            @Override
            public void onItemClick(int position, TodoListEntry entry) {
                if (entry.getItem() == null) return;
                showEditTodoDialog(entry.getItem(), entry.getGroup());
            }

            @Override
            public void onItemCheckedChanged(int position, TodoListEntry entry, boolean checked) {
                TodoItem item = entry.getItem();
                if (item == null) return;
                item.setCompleted(checked);
                saveGroups();
                rebuildList();
            }

        });

        binding.rvTodo.setLayoutManager(new LinearLayoutManager(mContext));
        binding.rvTodo.setAdapter(adapter);


        attachSwipeToDelete();

        binding.btnAddTodo.setOnClickListener(v -> showAddTodoDialog());

        stickyGroups.clear();
        stickyGroups.addAll(TodoPrefs.loadStickyGroups());

        loadGroups();
        updateImportantBanner();
        rebuildList();
    }

    @Override
    protected void initData() {
        super.initData();
        EventBus.getDefault().register(this);
    }


    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                TodoListEntry entry = adapter.getEntry(position);
                if (entry == null) {
                    rebuildList();
                    return;
                }
                if (entry.getType() == TodoListEntry.TYPE_GROUP) {
                    TodoGroup group = entry.getGroup();
                    if (group == null) {
                        rebuildList();
                        return;
                    }
                    if (DEFAULT_GROUP_NAME.equals(group.getName())) {
                        SimToast.toastEL("未分类不可删除");
                        rebuildList();
                        return;
                    }
                    new AlertDialog.Builder(mContext)
                            .setTitle("删除分类")
                            .setMessage("删除分类后，该分类内待办会移到未分类")
                            .setNegativeButton("取消", (d, w) -> rebuildList())
                            .setPositiveButton("删除", (d, w) -> {
                                moveGroupToDefault(group);
                                saveGroups();
                                rebuildList();
                            })
                            .show();
                } else {
                    TodoItem item = entry.getItem();
                    if (item == null) {
                        rebuildList();
                        return;
                    }
                    new AlertDialog.Builder(mContext)
                            .setTitle("删除待办")
                            .setMessage("确定删除该待办？")
                            .setNegativeButton("取消", (d, w) -> rebuildList())
                            .setPositiveButton("删除", (d, w) -> {
                                removeTodoItem(entry.getGroup(), item);
                                saveGroups();
                                rebuildList();
                            })
                            .show();
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvTodo);
    }

    private void showGroupActionDialog(TodoGroup group) {
        if (group == null) return;
        List<String> options = new ArrayList<>();
        boolean pinned = stickyGroups.contains(group.getId());
        options.add(pinned ? "取消置顶" : "置顶");
        if (!DEFAULT_GROUP_NAME.equals(group.getName())) {
            options.add("编辑分类");
            options.add("删除分类");
        }
        String[] items = options.toArray(new String[0]);
        new AlertDialog.Builder(mContext)
                .setTitle(group.getName())
                .setItems(items, (d, which) -> {
                    String action = items[which];
                    if ("置顶".equals(action) || "取消置顶".equals(action)) {
                        String groupId = group.getId();
                        if (stickyGroups.contains(groupId)) {
                            stickyGroups.remove(groupId);
                            SimToast.toastSe("已取消置顶");
                        } else {
                            stickyGroups.add(groupId);
                            SimToast.toastSe("已置顶");
                        }
                        TodoPrefs.saveStickyGroups(stickyGroups);
                        rebuildList();
                    } else if ("编辑分类".equals(action)) {
                        showEditGroupDialog(group);
                    } else if ("删除分类".equals(action)) {
                        new AlertDialog.Builder(mContext)
                                .setTitle("删除分类")
                                .setMessage("删除分类后，该分类内待办会移到未分类")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("删除", (d2, w2) -> {
                                    moveGroupToDefault(group);
                                    saveGroups();
                                    rebuildList();
                                })
                                .show();
                    }
                })
                .show();
    }

    private void showEditGroupDialog(TodoGroup group) {
        if (group == null) return;
        EditText input = new EditText(mContext);
        input.setText(group.getName());
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(mContext)
                .setTitle("编辑分类")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        SimToast.toastEL("分类名不能为空");
                        return;
                    }
                    if (DEFAULT_GROUP_NAME.equals(name)) {
                        SimToast.toastEL("不可命名为未分类");
                        return;
                    }
                    renameGroup(group, name);
                    saveGroups();
                    rebuildList();
                })
                .show();
    }

    private void renameGroup(TodoGroup group, String newName) {
        if (group == null) return;
        String oldName = group.getName();
        group.setName(newName);
        group.setId(newName);
        if (group.getItems() != null) {
            for (TodoItem item : group.getItems()) {
                if (item == null) continue;
                item.setCategory(newName);
            }
        }
        if (oldName != null) {
            if (stickyGroups.contains(oldName)) {
                stickyGroups.remove(oldName);
                stickyGroups.add(newName);
                TodoPrefs.saveStickyGroups(stickyGroups);
            }
            if (expandedGroups.contains(oldName)) {
                expandedGroups.remove(oldName);
                expandedGroups.add(newName);
            }
        }
    }

    private void showEditTodoDialog(TodoItem item, TodoGroup group) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_todo, null);
        EditText etTitle = view.findViewById(R.id.et_todo_title);
        RadioButton rbCategoryNone = view.findViewById(R.id.rb_category_none);
        RadioButton rbCategoryExist = view.findViewById(R.id.rb_category_exist);
        RadioButton rbCategoryNew = view.findViewById(R.id.rb_category_new);
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
        EditText etHours = view.findViewById(R.id.et_countdown_hours);
        EditText etMinutes = view.findViewById(R.id.et_countdown_minutes);
        EditText etSeconds = view.findViewById(R.id.et_countdown_seconds);
        TextView tvPickDate = view.findViewById(R.id.tv_pick_date);
        TextView tvPickTime = view.findViewById(R.id.tv_pick_time);
        android.widget.Switch swImportant = view.findViewById(R.id.sw_important);

        swImportant.setVisibility(View.GONE);

        List<String> categories = buildCategoryList();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        etTitle.setText(item.getTitle());
        if (item.getCategory() == null || item.getCategory().trim().isEmpty() || DEFAULT_GROUP_NAME.equals(item.getCategory())) {
            rbCategoryNone.setChecked(true);
            spCategory.setVisibility(View.GONE);
            etCategoryNew.setVisibility(View.GONE);
        } else if (categories.contains(item.getCategory())) {
            rbCategoryExist.setChecked(true);
            spCategory.setVisibility(View.VISIBLE);
            etCategoryNew.setVisibility(View.GONE);
            spCategory.setSelection(categories.indexOf(item.getCategory()));
        } else {
            rbCategoryNew.setChecked(true);
            spCategory.setVisibility(View.GONE);
            etCategoryNew.setVisibility(View.VISIBLE);
            etCategoryNew.setText(item.getCategory());
        }

        rbCategoryNone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.GONE);
                etCategoryNew.setVisibility(View.GONE);
            }
        });
        rbCategoryExist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.VISIBLE);
                etCategoryNew.setVisibility(View.GONE);
            }
        });
        rbCategoryNew.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.GONE);
                etCategoryNew.setVisibility(View.VISIBLE);
            }
        });

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

        rbRepeatYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgRepeatUnit.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        Calendar selected = Calendar.getInstance();
        selected.setTimeInMillis(item.getDueAt());
        rbNonRepeatDate.setChecked(true);
        rbNonRepeatCountdown.setChecked(false);
        layoutCountdown.setVisibility(View.GONE);
        layoutDate.setVisibility(View.VISIBLE);
        tvPickDate.setText(String.format(Locale.CHINA, "%d-%02d-%02d", selected.get(Calendar.YEAR), selected.get(Calendar.MONTH) + 1, selected.get(Calendar.DAY_OF_MONTH)));
        tvPickTime.setText(String.format(Locale.CHINA, "%02d:%02d", selected.get(Calendar.HOUR_OF_DAY), selected.get(Calendar.MINUTE)));

        tvPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(mContext,
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
            TimePickerDialog dialog = new TimePickerDialog(mContext,
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

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("编辑待办")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                SimToast.toastEL("请输入待办名称");
                return;
            }

            String category = "";
            if (rbCategoryExist.isChecked()) {
                if (!categories.isEmpty()) {
                    category = categories.get(spCategory.getSelectedItemPosition());
                }
            } else if (rbCategoryNew.isChecked()) {
                category = etCategoryNew.getText().toString().trim();
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

            updateItemCategory(group, item, category);
            saveGroups();
            rebuildList();
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void updateItemCategory(TodoGroup oldGroup, TodoItem item, String newCategory) {
        String groupName = TextUtils.isEmpty(newCategory) ? DEFAULT_GROUP_NAME : newCategory;
        if (oldGroup != null && groupName.equals(oldGroup.getName())) {
            item.setCategory(groupName);
            return;
        }
        removeTodoItem(oldGroup, item);
        item.setCategory(groupName);
        addTodoItem(item, groupName);
    }

    private void showImportantActionDialog(TodoItem important) {
        if (important == null) return;
        String[] items = new String[]{"编辑", "删除"};
        new AlertDialog.Builder(mContext)
                .setTitle("重要待办")
                .setItems(items, (d, which) -> {
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
            DatePickerDialog dialog = new DatePickerDialog(mContext,
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
            TimePickerDialog dialog = new TimePickerDialog(mContext,
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

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("编辑重要待办")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                SimToast.toastEL("请输入待办名称");
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
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void loadGroups() {
        groups.clear();
        List<TodoGroup> stored = TodoPrefs.loadGroups();
        if (stored.isEmpty()) {
            groups.addAll(buildDemoGroups());
        } else {
            groups.addAll(stored);
        }
    }

    private void saveGroups() {
        TodoPrefs.saveGroups(groups);
    }


    private long getGroupNearestDue(TodoGroup group) {
        if (group == null || group.getItems() == null) return Long.MAX_VALUE;
        long nearest = Long.MAX_VALUE;
        for (TodoItem item : group.getItems()) {
            if (item == null) continue;
            if (item.isCompleted()) continue;
            long due = item.getDueAt();
            if (due > 0 && due < nearest) {
                nearest = due;
            }
        }
        return nearest;
    }

    private boolean isGroupAllCompleted(TodoGroup group) {
        if (group == null || group.getItems() == null || group.getItems().isEmpty()) return false;
        for (TodoItem item : group.getItems()) {
            if (item == null) continue;
            if (!item.isCompleted()) return false;
        }
        return true;
    }

    private void rebuildList() {
        List<TodoListEntry> entries = new ArrayList<>();
        List<TodoGroup> ordered = new ArrayList<>(groups);
        Collections.sort(ordered, (a, b) -> {
            boolean aAllDone = isGroupAllCompleted(a);
            boolean bAllDone = isGroupAllCompleted(b);
            if (aAllDone != bAllDone) {
                return aAllDone ? 1 : -1;
            }
            boolean ap = stickyGroups.contains(a.getId());
            boolean bp = stickyGroups.contains(b.getId());
            if (ap != bp) {
                return ap ? -1 : 1;
            }
            long ad = getGroupNearestDue(a);
            long bd = getGroupNearestDue(b);
            if (ad != bd) {
                return Long.compare(ad, bd);
            }
            String an = a.getName() == null ? "" : a.getName();
            String bn = b.getName() == null ? "" : b.getName();
            return an.compareToIgnoreCase(bn);
        });
        for (TodoGroup group : ordered) {
            if (group.getItems() == null) {
                group.setItems(new ArrayList<>());
            }
            List<TodoItem> items = new ArrayList<>(group.getItems());
            Collections.sort(items, (o1, o2) -> {
                if (o1.isCompleted() != o2.isCompleted()) {
                    return o1.isCompleted() ? 1 : -1;
                }
                return Long.compare(o1.getDueAt(), o2.getDueAt());
            });
            boolean expanded = expandedGroups.contains(group.getId());
            entries.add(TodoListEntry.group(group, expanded, stickyGroups.contains(group.getId())));
            if (expanded) {
                for (TodoItem item : items) {
                    entries.add(TodoListEntry.item(group, item));
                }
            }
        }

        adapter.submit(entries);
        binding.tvTodoEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddTodoDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_todo, null);
        EditText etTitle = view.findViewById(R.id.et_todo_title);
        RadioButton rbCategoryNone = view.findViewById(R.id.rb_category_none);
        RadioButton rbCategoryExist = view.findViewById(R.id.rb_category_exist);
        RadioButton rbCategoryNew = view.findViewById(R.id.rb_category_new);
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
        EditText etHours = view.findViewById(R.id.et_countdown_hours);
        EditText etMinutes = view.findViewById(R.id.et_countdown_minutes);
        EditText etSeconds = view.findViewById(R.id.et_countdown_seconds);
        TextView tvPickDate = view.findViewById(R.id.tv_pick_date);
        TextView tvPickTime = view.findViewById(R.id.tv_pick_time);
        Switch swImportant = view.findViewById(R.id.sw_important);

        List<String> categories = buildCategoryList();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        rbCategoryNone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.GONE);
                etCategoryNew.setVisibility(View.GONE);
            }
        });
        rbCategoryExist.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.VISIBLE);
                etCategoryNew.setVisibility(View.GONE);
            }
        });
        rbCategoryNew.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                spCategory.setVisibility(View.GONE);
                etCategoryNew.setVisibility(View.VISIBLE);
            }
        });

        rbRepeatYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgRepeatUnit.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        rbRepeatNo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rgRepeatUnit.setVisibility(View.GONE);
            }
        });

        rbNonRepeatCountdown.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutCountdown.setVisibility(View.VISIBLE);
                layoutDate.setVisibility(View.GONE);
            }
        });
        rbNonRepeatDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutCountdown.setVisibility(View.GONE);
                layoutDate.setVisibility(View.VISIBLE);
            }
        });

        Calendar selected = Calendar.getInstance();
        tvPickDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(mContext,
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
            TimePickerDialog dialog = new TimePickerDialog(mContext,
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

        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("添加待办")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                SimToast.toastEL("请输入待办名称");
                return;
            }

            String category = "";
            if (rbCategoryExist.isChecked()) {
                if (!categories.isEmpty()) {
                    category = categories.get(spCategory.getSelectedItemPosition());
                }
            } else if (rbCategoryNew.isChecked()) {
                category = etCategoryNew.getText().toString().trim();
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
            } else if (rbNonRepeatDate.isChecked()) {
                dueAt = selected.getTimeInMillis();
                if (dueAt <= System.currentTimeMillis()) {
                    SimToast.toastEL("请选择未来的时间");
                    return;
                }
            } else {
                int hours = parseInt(etHours.getText().toString());
                int minutes = parseInt(etMinutes.getText().toString());
                int seconds = parseInt(etSeconds.getText().toString());
                long totalSeconds = hours * 3600L + minutes * 60L + seconds;
                if (totalSeconds <= 0) {
                    SimToast.toastEL("请输入倒计时时间");
                    return;
                }
                dueAt = System.currentTimeMillis() + totalSeconds * 1000L;
            }


            if (swImportant.isChecked() && TodoPrefs.loadImportant() != null) {
                SimToast.toastEL("先专心把之前保存的事情做完吧");
                return;
            }

            TodoItem item = new TodoItem(UUID.randomUUID().toString(), title);
            item.setCategory(category);
            item.setRepeat(repeat);
            item.setRepeatUnit(repeatUnit);
            item.setDueAt(dueAt);
            item.setCreatedAt(System.currentTimeMillis());
            item.setImportant(swImportant.isChecked());

            if (item.isImportant()) {
                TodoPrefs.saveImportant(item);
                EventBus.getDefault().post(new TodoImportantChanged(item));
            } else {
                addTodoItem(item, category);
                saveGroups();
            }
            rebuildList();
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void addTodoItem(TodoItem item, String category) {
        String groupName = TextUtils.isEmpty(category) ? DEFAULT_GROUP_NAME : category;
        TodoGroup target = null;
        for (TodoGroup group : groups) {
            if (groupName.equals(group.getName())) {
                target = group;
                break;
            }
        }
        if (target == null) {
            target = new TodoGroup(groupName, groupName);
            groups.add(target);
        }
        if (target.getItems() == null) {
            target.setItems(new ArrayList<>());
        }
        target.getItems().add(item);
    }

    private void moveGroupToDefault(TodoGroup group) {
        if (group == null) return;
        if (group.getItems() == null || group.getItems().isEmpty()) {
            groups.remove(group);
            return;
        }
        TodoGroup target = null;
        for (TodoGroup g : groups) {
            if (DEFAULT_GROUP_NAME.equals(g.getName())) {
                target = g;
                break;
            }
        }
        if (target == null) {
            target = new TodoGroup(DEFAULT_GROUP_NAME, DEFAULT_GROUP_NAME);
            groups.add(target);
        }
        if (target.getItems() == null) {
            target.setItems(new ArrayList<>());
        }
        target.getItems().addAll(group.getItems());
        groups.remove(group);
    }


    private void removeTodoItem(TodoGroup group, TodoItem item) {
        if (group == null || item == null) return;
        if (group.getItems() == null) return;
        List<TodoItem> newItems = new ArrayList<>();
        for (TodoItem it : group.getItems()) {
            if (it == null) continue;
            if (item.getId() != null && item.getId().equals(it.getId())) {
                continue;
            }
            newItems.add(it);
        }
        group.setItems(newItems);
        if (group.getItems().isEmpty() && !DEFAULT_GROUP_NAME.equals(group.getName())) {
            groups.remove(group);
        }
    }

    private List<String> buildCategoryList() {
        List<String> categories = new ArrayList<>();
        for (TodoGroup group : groups) {
            if (group.getName() == null) continue;
            if (DEFAULT_GROUP_NAME.equals(group.getName())) continue;
            categories.add(group.getName());
        }
        Collections.sort(categories);
        return categories;
    }

    private List<TodoGroup> buildDemoGroups() {
        List<TodoGroup> list = new ArrayList<>();
        TodoGroup group1 = new TodoGroup("study", "学习");
        group1.getItems().add(buildDemoItem("背单词 30 分钟", "学习", 2 * 60 * 60));
        group1.getItems().add(buildDemoItem("刷题一套", "学习", 5 * 60 * 60));
        TodoGroup group2 = new TodoGroup("life", "生活");
        group2.getItems().add(buildDemoItem("健身 45 分钟", "生活", 3 * 60 * 60));
        TodoGroup group3 = new TodoGroup(DEFAULT_GROUP_NAME, DEFAULT_GROUP_NAME);
        group3.getItems().add(buildDemoItem("今天喝水 8 杯", "", 6 * 60 * 60));
        list.add(group1);
        list.add(group2);
        list.add(group3);
        return list;
    }

    private TodoItem buildDemoItem(String title, String category, long secondsFromNow) {
        TodoItem item = new TodoItem(UUID.randomUUID().toString(), title);
        item.setCategory(category);
        item.setDueAt(System.currentTimeMillis() + secondsFromNow * 1000L);
        item.setRepeat(false);
        return item;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
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
        binding.includeImportantBanner.getRoot().setOnClickListener(v -> showImportantActionDialog(important));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImportantChanged(TodoImportantChanged event) {
        updateImportantBanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
