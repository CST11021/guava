package com.whz.guava.eventbus;

import com.google.common.eventbus.Subscribe;

/**
 * @Author: wanghz
 * @Date: 2020/8/27 3:32 PM
 */
public class EventListener {

    /**
     * 只有通过@Subscribe注解的方法才会被注册进EventBus 而且方法有且只能有1个参数
     *
     * @param msg
     */
    @Subscribe
    public void listener1(String msg) {
        System.out.println("监听String事件回调：" + msg);
    }

    /**
     * post() 不支持自动装箱功能,只能使用Integer,不能使用int,否则handlersByType的Class会是int而不是Intege
     * 而传入的int msg参数在post(int msg)的时候会被包装成Integer,导致无法匹配到
     */
    @Subscribe
    public void listener2(Integer msg) {
        System.out.println("监听Integer事件回调：" + msg);
    }


}
