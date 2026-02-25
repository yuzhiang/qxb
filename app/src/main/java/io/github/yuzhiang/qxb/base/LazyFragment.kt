package io.github.yuzhiang.qxb.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class LazyFragment : Fragment() {
    private var isFirstLoad = true // 是否第一次加载
    protected var TAG: String = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isFirstLoad = true
        return LayoutInflater.from(context).inflate(getContentViewId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

    }

    override fun onResume() {
        super.onResume()
        canSee()
        if (isFirstLoad) {
            initData()
            initEvent()
            isFirstLoad = false
        }
    }


    /**
     * 设置布局资源id
     *
     * @return
     */
    protected abstract fun getContentViewId(): Int

    /**
     * 初始化视图
     *
     * @param view
     */
    protected open fun initView(view: View) {}

    /**
     * 初始化数据
     */
    protected open fun initData() {}
    protected open fun canSee() {}

    /**
     * 初始化事件
     */
    protected open fun initEvent() {}
}