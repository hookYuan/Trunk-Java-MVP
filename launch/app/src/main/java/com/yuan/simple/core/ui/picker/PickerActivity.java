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
package com.yuan.simple.core.ui.picker;

import android.view.View;

import com.yuan.simple.core.adapter.TextAdapter;
import com.yuan.simple.core.module.SubjectBean;
import com.yuan.simple.core.presenter.PickerPresenter;
import com.yuan.simple.main.contract.MainContract;

import yuan.core.list.BaseViewHolder;
import yuan.core.list.GridDivider;
import yuan.core.list.RecyclerAdapter;
import yuan.core.tool.PickerUtil;
import yuan.core.tool.ToastUtil;
import yuan.core.ui.Adapter;
import yuan.core.ui.RecyclerActivity;
import yuan.core.ui.Title;

/**
 * 系统常用选择器
 * 1.图片选择器
 * 2.通讯录选择器
 * 3.相机拍照
 *
 * @author YuanYe
 * @date 2019/7/19  23:59
 */
@Title(titleStr = "PickerUtil")
@Adapter(adapter = TextAdapter.class)
public class PickerActivity extends RecyclerActivity<PickerPresenter, SubjectBean>
        implements MainContract {

    @Override
    public void initData() {
        mRecyclerView.addItemDecoration(new GridDivider());
        getPresenter().loadData(mData);
    }

    @Override
    public void setListener() {
        mAdapter.setOnItemClick(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseViewHolder holder, View view, int position) {
                switch (position) {
                    case 0:
                        PickerUtil.startCamera(mContext, new PickerUtil.SelectBack() {
                            @Override
                            public void onBack(String path) {
                                ToastUtil.showShort(mContext, path);
                            }
                        });
                        break;
                    case 1:
                        PickerUtil.startAlbum(mContext, new PickerUtil.SelectBack() {
                            @Override
                            public void onBack(String path) {
                                ToastUtil.showShort(mContext, path);
                            }
                        });
                        break;
                    case 2:
                        PickerUtil.startCameraAlbum(mContext, new PickerUtil.SelectBack() {
                            @Override
                            public void onBack(String path) {
                                ToastUtil.showShort(mContext, path);
                            }
                        });
                        break;
                    case 3:
                        PickerUtil.startAddressBook(mContext, new PickerUtil.ContactBack() {
                            @Override
                            public void onBack(String name, String phone) {
                                ToastUtil.showShort(mContext, "姓名：" + name + "  电话：" + phone);
                            }
                        });
                        break;
                }
            }
        });
    }

    @Override
    public void notifyDataChange(boolean isSuccess) {
        mAdapter.notifyDataSetChanged();
    }
}
