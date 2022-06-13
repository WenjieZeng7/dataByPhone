 package com.example.sensordemo_type_gyroscope;

import android.app.Application;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;


/**
 * <pre>
 *     author : Mr.Fu
 *     e-mail : 18622268981@163.com
 *     time   : 2020/03/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class MyApplication extends Application {

    // 需要将 CH34x 的驱动类写在APP类下面，使得帮助类的生命周期与整个应用程序的生命周期是相同的
    public static CH34xUARTDriver driver;
}
