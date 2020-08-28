package com.whz.guava.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * Created by zhangzh on 2017/1/10.
 */
public class EventBusTest {

    private static EventBus eventBus = new EventBus();

    public static void main(String[] args) {

        EventListener listener = new EventListener();

        // 注册监听器
        eventBus.register(listener);

        // 发布事件
        eventBus.post("post string method");
        eventBus.post(123);

        // 注销监听器
        eventBus.unregister(listener);
        eventBus.post("post string method");
        eventBus.post(123);

    }
}  