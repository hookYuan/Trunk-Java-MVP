package yuan.depends;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.jude.swipbackhelper.SwipeListener;


/**
 * Created by YuanYe on 2017/7/12.
 * SwipeBackHelper 的辅助实现类，方便替换
 * 使用详情，参考作者github
 * 实现初始化  使用需要绑定生命周期
 * <p>
 * 依赖库：
 * com.jude:swipebackhelper
 */
public class SwipeBackUtil {

    private SwipeBackUtil() {

    }

    public static void init(AppCompatActivity context) {
        SwipeBackHelper.onCreate(context);
        SwipeBackHelper.getCurrentPage(context)//获取当前页面
                .setSwipeBackEnable(true)//设置是否可滑动
                .setSwipeEdge(200)//可滑动的范围。px。200表示为左边200px的屏幕
                .setSwipeEdgePercent(0.2f)//可滑动的范围。百分比。0.2表示为左边20%的屏幕
                .setSwipeSensitivity(0.5f)//对横向滑动手势的敏感程度。0为迟钝 1为敏感
                .setScrimColor(Color.parseColor("#00ffffff"))//底层阴影颜色
                .setClosePercent(0.8f)//触发关闭Activity百分比
                .setSwipeRelateEnable(false)//是否与下一级activity联动(微信效果)。默认关
                .setSwipeRelateOffset(500)//activity联动时的偏移量。默认500px。
                .setDisallowInterceptTouchEvent(false)//不抢占事件，默认关（事件将先由子View处理再由滑动关闭处理）
                .addListener(new SwipeListener() {//滑动监听

                    @Override
                    public void onScroll(float percent, int px) {//滑动的百分比与距离
                    }

                    @Override
                    public void onEdgeTouch() {//当开始滑动
                    }

                    @Override
                    public void onScrollToClose() {//当滑动关闭
                    }
                });
    }

    /**
     * 需绑定生命周期
     *
     * @param activity Adapter
     */
    public static void bindOnCreate(@NonNull AppCompatActivity activity) {
        SwipeBackHelper.onPostCreate(activity);
    }

    /**
     * 销毁
     */
    public static void bindOnDestroy(AppCompatActivity activity) {
        SwipeBackHelper.onDestroy(activity);
    }
}
