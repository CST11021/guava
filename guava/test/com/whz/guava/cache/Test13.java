package com.whz.guava.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @program: guava
 * @description: 缓存
 * @author: 赖键锋
 * @create: 2018-08-30 21:37
 **/
public class Test13 {

    private static final Cache<String, Object> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(2, TimeUnit.SECONDS)  // 超过缓存最大数量时，过期时间才生效
            .removalListener((notification) -> {            // 缓存被移除或者更新时触发
                System.out.println("缓存过期：" + notification.getCause()
                        + ", key = " +  notification.getKey()
                        + ", value = " + notification.getValue());
            })
            .build();

    public static void main(String[] args) throws ExecutionException, IOException {

        String key = "123";
        Object value = cache.get(key, new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                String strProValue = "hello " + key + "!";
                return strProValue;
            }

        });
        System.out.println(value);
        cache.put(key, "test");
        // cache.invalidate(key);


        cacheLoader();
        callback();

        System.in.read();
    }

    public static void cacheLoader() {
        try {

            LoadingCache<String, String> cahceBuilder = CacheBuilder
                    .newBuilder()
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String key) throws Exception {
                            String strProValue = "hello " + key + "!";
                            return strProValue;
                        }
                    });
            System.out.println(cahceBuilder.get("begincode")); //hello begincode!
            cahceBuilder.put("begin", "code");
            System.out.println(cahceBuilder.get("begin")); //code
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void callback() {
        try {
            Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(1000).build();
            String resultVal = cache.get("code", new Callable<String>() {
                @Override
                public String call() {
                    String strProValue = "begin " + "code" + "!";
                    return strProValue;
                }
            });
            System.out.println("value : " + resultVal); //value : begin code!
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
