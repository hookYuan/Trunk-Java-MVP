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
package yuan.core.tool;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import yuan.core.dialog.DialogUtils;

/**
 * 采用系统启动方式：
 * 1.图片选择
 * 2.相机拍照
 * 3.通讯录选择
 *
 * @author yuanye
 * @date 2019/4/23
 */
public class PickerUtil {


    /**
     * 启动 图片/相机 选择
     *
     * @param context
     */
    public static void startCameraAlbum(final Context context, final SelectBack selectBack) {
        String[] menu = {"图库", "拍照"};
        DialogUtils
                .alertList(menu, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) { //图库选择
                            startAlbum(context, selectBack);
                        } else {
                            startCamera(context, selectBack);
                        }
                    }
                }).create(context);
    }

    /**
     * 启动图片选择
     * 拍照默认保存在Camera文件夹中
     *
     * @param context
     * @param selectBack
     */
    public static void startCamera(Context context, SelectBack selectBack) {
        //获取系统当前时间 yy-mm-dd
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmSS");
        String time = format.format(date);
        // 使用IO流将照片写入指定文件 getExternalFilesDir(null)
        String DCIMPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        final String DIRECTORY = DCIMPath + "/Camera" + "/IMG_" + time + "_" + 0 + ".jpg";
        startCamera(context, DIRECTORY, selectBack);
    }


    /**
     * 启动相机并获取照片
     *
     * @param context
     * @param selectBack
     * @param path       需要保存的拍照文件的路径
     */
    public static void startCamera(final Context context, final String path, final SelectBack selectBack) {
        if (!RouteUtil.checkPermission((AppCompatActivity) context, Manifest.permission.CAMERA)
                || !RouteUtil.checkPermission((AppCompatActivity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || !RouteUtil.checkPermission((AppCompatActivity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            RouteUtil.openPermission(context, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, new RouteUtil.OnPermissionListener() {
                @Override
                public void onResult(int requestCode, @NonNull String[] permissions, @NonNull boolean[] result) {
                    for (boolean check : result) {
                        if (check == true) continue;
                        DialogUtils
                                .alertText("请在设置中开启相机和存储权限", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RouteUtil.openSetting(context, RouteUtil.APPLICATION3);
                                    }
                                }).create(context);
                        return;
                    }
                    //将要保存图片的路径
                    File file = new File(path);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdir();
                    }
                    //优先创建文件
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Uri fileUri = PathUtil.file2Uri(context, file);
                    Intent thumbIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    thumbIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    handlerResult(context, thumbIntent, fileUri, selectBack);
                }
            });
        } else {
            //将要保存图片的路径
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            //优先创建文件
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //必须要先创建文件后才可以转化
            Uri fileUri = PathUtil.file2Uri(context, file);
            Intent thumbIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            thumbIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            handlerResult(context, thumbIntent, fileUri, selectBack);
        }
    }

    /**
     * 启动图库选择
     *
     * @param context
     * @param selectBack
     */
    public static void startAlbum(final Context context, final SelectBack selectBack) {
        if (!(Boolean) RouteUtil.checkPermission((AppCompatActivity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            RouteUtil.openPermission(context, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, new RouteUtil.OnPermissionListener() {
                @Override
                public void onResult(int requestCode, @NonNull String[] permissions, @NonNull boolean[] result) {
                    if (result.length > 0 && result[0] == false) {
                        DialogUtils.alertText("请在设置中开启存储权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RouteUtil.openSetting(context, RouteUtil.APPLICATION3);
                            }
                        }).create(context);
                    } else if (result.length > 0) {
                        //打开图库
                        Intent albumIntent = new Intent(Intent.ACTION_PICK);
                        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        handlerResult(context, albumIntent, null, selectBack);
                    }
                }
            });
        } else {
            //打开图库
            Intent albumIntent = new Intent(Intent.ACTION_PICK);
            albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            handlerResult(context, albumIntent, null, selectBack);
        }
    }

    /**
     * 处理图库或相册返回
     *
     * @param context
     * @param intent
     */
    private static void handlerResult(final Context context, final Intent intent, final Uri saveUri, final SelectBack selectBack) {
        RouteUtil.openResult(context, intent, new RouteUtil.OnActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_OK && requestCode == RouteUtil.REQUEST_CODE) {
                    String path = "";
                    if (saveUri != null) {
                        path = PathUtil.uri2Path(context, saveUri);
                    } else {
                        Uri uri = data.getData();// 获取图片的uri
                        path = PathUtil.uri2Path(context, uri);
                    }
                    selectBack.onBack(path);
                }
            }
        });
    }


    /**
     * 从通讯录中选择
     *
     * @param context
     */
    public static void startAddressBook(final AppCompatActivity context, final ContactBack contactBack) {
        if (RouteUtil.checkPermission(context, Manifest.permission.READ_CONTACTS)) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startAddressBook(context, intent, contactBack);
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            RouteUtil.openPermission(context, new String[]{
                    Manifest.permission.READ_CONTACTS
            }, new RouteUtil.OnPermissionListener() {
                @Override
                public void onResult(int requestCode, @NonNull String[] permissions, @NonNull boolean[] result) {
                    if (result.length > 0 && result[0] == false) {
                        DialogUtils.alertText("请在设置中开启通讯录权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RouteUtil.openSetting(context, RouteUtil.APPLICATION3);
                            }
                        }).create(context);
                    } else if (result.length > 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startAddressBook(context, intent, contactBack);
                    }
                }
            });
        }
    }

    /**
     * 处理通讯录返回结果
     */
    private static void startAddressBook(final Context context, final Intent intent, final ContactBack contactBack) {
        RouteUtil.openResult(context, intent, new RouteUtil.OnActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_OK && requestCode == RouteUtil.REQUEST_CODE) {
                    if (data == null) {
                        return;
                    }
                    try {
                        //处理返回的data,获取选择的联系人信息
                        Uri uri = data.getData();
                        String[] contacts = getPhoneContacts(uri, context);
                        contactBack.onBack(contacts[0], contacts[1]);
                    } catch (Exception e) {
                        //6.0以下，无权限
                    }
                }
            }
        });
    }

    /**
     * 获取通讯录
     *
     * @param uri
     * @param context
     * @return
     */
    private static String[] getPhoneContacts(Uri uri, Context context) {
        String[] contact = new String[2];
        //得到ContentResolver对象
        ContentResolver cr = context.getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //取得联系人姓名
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contact[0] = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            if (phone != null) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        } else {
            return null;
        }
        return contact;
    }


    public interface SelectBack {
        /**
         * 图片选择回调
         *
         * @param path
         */
        void onBack(String path);
    }

    public interface ContactBack {

        /**
         * @param name
         * @param phone
         */
        void onBack(String name, String phone);
    }
}
