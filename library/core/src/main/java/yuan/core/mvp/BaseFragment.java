/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package yuan.core.mvp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 描述：Fragment的基础封装
 *
 * <p>
 * 1.支持Fragment延迟加载
 * 2.支持快速实现Mvp风格代码
 * 3.支持快捷加载 String,View,Drawable,color等资源
 * 4.统一书写格式
 * 5.通过泛型自动绑定Presenter,可通过getP()获取Presenter实例
 *
 * @author yuanye
 * @date 2019/4/4 13:17
 */
public abstract class BaseFragment<presenter extends Presenter> extends Fragment implements BaseContract.View {

    private final static String TAG = "BaseFragment";
    /**
     * 保存Fragment的状态，防止重启后Fragment重叠
     */
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    /**
     * 加载的父布局
     */
    protected View mView;
    /**
     * 防止getActivity()空指针
     */
    protected AppCompatActivity mContext;
    /**
     * 切换到主线程
     */
    private Handler mainHandler;
    /**
     * 是否加载完成
     */
    private boolean isPrepared;
    /**
     * 是否是第一次可见
     */
    private boolean isFirstVisible = true;
    /**
     * 是否是第一次不可见
     */
    private boolean isFirstInvisible = true;
    /**
     * 第一次onResume中的调用onUserVisible避免操作与onFirstUserVisible操作重复
     */
    private boolean isFirstResume = true;
    /**
     * presenter
     */
    private presenter mPresenter;

    /**
     * 默认构造方法
     */
    public BaseFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //反射获取Presenter
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) { //获取上次显示状态
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
        mainHandler = new Handler(Looper.getMainLooper());

        if (mPresenter != null) mPresenter.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutView = getLayoutView();
        int layoutId = getLayoutId();
        if (layoutId != 0) mView = inflater.inflate(layoutId, container, false);
        else if (layoutView != null) mView = layoutView;
        else throw new NullPointerException("Fragment布局视图不能为空");
        return mView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPrepare();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //保存状态
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @TargetApi(23) //API<23不对调用该方法
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = (AppCompatActivity) context;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = (AppCompatActivity) activity;
        }
    }

    @Override
    public void onResume() {
        this.mContext = (AppCompatActivity) this.getActivity();
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        if (getUserVisibleHint()) {
            onUserVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserInvisible();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                Log.i(TAG, "initPrepare");
                initPrepare();
            } else {
                Log.i(TAG, "onUserVisible");
                onUserVisible();
            }
        } else {
            if (isFirstInvisible) {
                isFirstInvisible = false;
                Log.i(TAG, "onFirstUserInvisible");
                onFirstUserInvisible();
            } else {
                Log.i(TAG, "onUserInvisible");
                onUserInvisible();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mContext != null) {//关闭键盘
            InputMethodManager imm = (InputMethodManager) mContext
                    .getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                View view = mContext.getCurrentFocus();
                if (view == null) view = new View(mContext);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        super.onDestroy();
        if (mPresenter != null) mPresenter.onDestroy();
    }

    /**
     * 调用此方法时会导致{@link #getLayoutId()}失效
     * 默认返回空，需要替换时可以自行更新
     *
     * @return
     */
    @Override
    public View getLayoutView() {
        return null;
    }

    /**
     * 此方法为标准代码风格设置，所有查找控件方法都应该放于这里
     * 鉴于Kotlin已经不需要调用{@link #findViewById(int)},所以不是
     * 必须实现方法
     */
    @Override
    public void findViews() {

    }

    /**
     * 此方法为标准代码风格设置，所有从bundle中取值方法都应该放于这里
     * 非必须实现方法
     *
     * @param bundle
     */
    @Override
    public void parseBundle(@Nullable Bundle bundle) {

    }

    /**
     * 初始化完成
     */
    @Override
    public void initComplete() {

    }

    /**
     * 获取Presenter
     *
     * @return presenter实例
     */
    public presenter getPresenter() {
        if (mPresenter == null) {
            try {
                throw new NullPointerException("使用presenter,MVPActivity泛型不能为空");
            } catch (NullPointerException e) {
                throw e;
            }
        }
        return mPresenter;
    }


    /**
     * 加载View
     *
     * @param id
     * @param <T>
     * @return
     */
    public <T extends View> T findViewById(@IdRes int id) {
        if (mView == null) return null;
        return mView.findViewById(id);
    }

    /**
     * 第一次fragment可见（进行初始化工作）
     */
    protected void onFirstUserVisible() {
        findViews();
        parseBundle(getArguments());
        initData();
        initComplete();
        setListener();
        if (mPresenter != null) mPresenter.onResume();
    }

    /**
     * fragment可见（切换回来或者onResume）
     */
    protected void onUserVisible() {
        if (mPresenter != null) mPresenter.onResume();
    }

    /**
     * 第一次fragment不可见（不建议在此处理事件）
     */
    protected void onFirstUserInvisible() {

    }

    /**
     * fragment不可见（切换掉或者onPause）
     */
    protected void onUserInvisible() {

    }

    /**
     * 获取颜色
     *
     * @param colorId
     * @return
     */
    @ColorInt
    protected final int getColor2(@ColorRes int colorId) {
        return ContextCompat.getColor(getContext(), colorId);
    }

    /**
     * 获取Drawable
     *
     * @param drawableId
     * @return
     */
    @Nullable
    protected final Drawable getDrawable2(@DrawableRes int drawableId) {
        return ContextCompat.getDrawable(getContext(), drawableId);
    }

    /**
     * 获取String
     *
     * @param id
     * @return
     */
    @NonNull
    protected final String getString2(@StringRes int id) {
        return getResources().getString(id);
    }

    /**
     * Toast,系统默认样式
     *
     * @param msg 提示内容
     */
    protected final void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 运行在主线程
     *
     * @param runnable
     */
    protected final void runOnUiThread(Runnable runnable) {
        if (mainHandler != null) mainHandler.post(runnable);
    }

    /**
     * 获取Presenter实例
     * <p>
     * 默认反射第一个泛型创建Presenter
     * 如果未指定泛型，请重写该方法
     *
     * @return Presenter的实例化对象
     */
    protected presenter createPresenter() {
        //只获取当前类的泛型参数
        Type type = this.getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] types = parameterizedType.getActualTypeArguments();
        //当前class有泛型参数
        for (Type currentType : types) {
            /*遍历所有继承父类，判断是否包含Presenter类型*/
            Class superClass = ((Class) currentType).getSuperclass();
            while (superClass != Object.class) {
                if (superClass == Presenter.class) {
                    String presenterName = ((Class) currentType).getName();
                    try {
                        return (presenter) Class.forName(presenterName).newInstance();
                    } catch (java.lang.InstantiationException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
                superClass = superClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 初始化
     */
    private synchronized void initPrepare() {
        if (isPrepared) {
            onFirstUserVisible();
        } else {
            isPrepared = true;
        }
    }

}
