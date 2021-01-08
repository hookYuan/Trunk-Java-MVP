package com.yuan.simple.core.ui.adapter;

import android.support.v7.widget.GridLayoutManager;

import com.yuan.simple.core.adapter.MultiTypeAdapter;
import com.yuan.simple.core.module.SubjectBean;
import com.yuan.simple.core.presenter.AdapterPresenter;
import com.yuan.simple.main.contract.MainContract;

import java.util.ArrayList;
import java.util.List;

import yuan.core.list.GridDivider;
import yuan.core.title.TitleBar;
import yuan.core.ui.Adapter;
import yuan.core.ui.RecyclerActivity;
import yuan.core.ui.Title;

@Title(titleStr = "多类型Adapter")
@Adapter(adapter = MultiTypeAdapter.class)
public class MultipleActivity extends RecyclerActivity<AdapterPresenter, SubjectBean>
        implements MainContract {

    @Override
    public void initData() {
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position % 5 == 0) {
                    return 3;
                }
                return 1;
            }
        });
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new GridDivider());

        getPresenter().loadData(mData);
        List<String> menu = new ArrayList<>();
        menu.add("数据");
        menu.add("空布局");
        menu.add("失败布局");
        menu.add("加载中布局");
        titleBar.setRightText("更多")
                .setRightMenu(menu, new TitleBar.OnMenuItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        mAdapter.setAutoSwitch(false);
                        switch (position) {
                            case 0:
                                mAdapter.showContent();
                                break;
                            case 1:
                                mAdapter.showEmpty();
                                break;
                            case 2:
                                mAdapter.showError();
                                break;
                            case 3:
                                mAdapter.showLoading();
                                break;
                        }
                    }
                });
    }

    @Override
    public void setListener() {

    }

    @Override
    public void notifyDataChange(boolean isSuccess) {
        mAdapter.notifyDataSetChanged();
    }
}
