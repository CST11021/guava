package com.whz.guava.eventbus;

import com.google.common.eventbus.Subscribe;

/**
 * Created by zhangzh on 2017/1/10.
 */
public class DataObserver1 {

    /**
     * 只有通过@Subscribe注解的方法才会被注册进EventBus 而且方法有且只能有1个参数
     *
     * @param msg
     */
    @Subscribe
    public void func(String msg) {
        System.out.println("String msg: " + msg);
    }

} 