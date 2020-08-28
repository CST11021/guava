package com.whz.guava.costtime;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.util.concurrent.TimeUnit;

/**
 * @program: guava
 * @description: 计算中间代码的运行时间
 * @author: 赖键锋
 * @create: 2018-08-30 21:26
 **/
public class StopwatchTest {
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        doSomeThing();
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));// 3

        doSomeThing();
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));// 6

        stopwatch.reset();
        stopwatch.start();
        doSomeThing();
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));// 3
    }

    private static void doSomeThing() {
        // do some thing
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
