package io.github.yuzhiang.qxb.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class Viewpager2Adapter extends FragmentStateAdapter {

    private List<Fragment> mFragments;

    public Viewpager2Adapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.mFragments = fragments;
    }

    //fragment中嵌套viewpager2
    public Viewpager2Adapter(@NonNull Fragment fragment, List<Fragment> fragments) {
        super(fragment);
        this.mFragments = fragments;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    //添加一个Fragment
    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
        notifyDataSetChanged();
    }

    //删除一个Fragment
    public void removeFragment() {
        if (!mFragments.isEmpty()) {
            mFragments.remove(mFragments.size() - 1);
            notifyDataSetChanged();
        }
    }

//
//    作者：陈希
//    链接：https://juejin.im/post/5e218d6de51d454d2f764878
//    来源：掘金
//    著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

}
