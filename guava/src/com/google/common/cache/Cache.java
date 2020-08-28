/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.cache;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * A semi-persistent mapping from keys to values. Cache entries are manually added using
 * {@link #get(Object, Callable)} or {@link #put(Object, Object)}, and are stored in the cache until
 * either evicted or manually invalidated. The common way to build instances is using
 * {@link CacheBuilder}.
 *
 * <p>Implementations of this interface are expected to be thread-safe, and can be safely accessed
 * by multiple concurrent threads.
 *
 * @author Charles Fry
 * @since 10.0
 */
@GwtCompatible
public interface Cache<K, V> {

    /**
     * 返回与此缓存中的{@code key}关联的值；如果没有{@code key}的缓存值，则返回{@code null}。
     *
     * @since 11.0
     */
    @Nullable
    V getIfPresent(Object key);

    /**
     * 获取key对应的值，如果值不在缓存中，则调用Callable获取值，并将该值加入缓存
     *
     * @param key
     * @param loader
     * @return
     * @throws ExecutionException
     */
    V get(K key, Callable<? extends V> loader) throws ExecutionException;

    /**
     * 返回与此缓存中的{@code keys}关联的值的映射
     *
     * @since 11.0
     */
    ImmutableMap<K, V> getAllPresent(Iterable<?> keys);

    /**
     * 添加或更新缓存
     *
     * @since 11.0
     */
    void put(K key, V value);

    /**
     * 批量添加或更新缓存
     *
     * @since 12.0
     */
    void putAll(Map<? extends K, ? extends V> m);

    /**
     * 移除缓存
     *
     * @param key
     */
    void invalidate(Object key);

    /**
     * 批量移除缓存
     *
     * @since 11.0
     */
    void invalidateAll(Iterable<?> keys);

    /**
     * 移除所有的缓存
     */
    void invalidateAll();

    /**
     * 获取缓存的数量
     */
    long size();

    /**
     * Returns a current snapshot of this cache's cumulative statistics, or a set of default values if
     * the cache is not recording statistics. All statistics begin at zero and never decrease over the
     * lifetime of the cache.
     *
     * <p><b>Warning:</b> this cache may not be recording statistical data. For example, a cache
     * created using {@link CacheBuilder} only does so if the {@link CacheBuilder#recordStats} method
     * was called. If statistics are not being recorded, a {@code CacheStats} instance with zero for
     * all values is returned.
     */
    CacheStats stats();

    /**
     * Returns a view of the entries stored in this cache as a thread-safe map. Modifications made to
     * the map directly affect the cache.
     *
     * <p>Iterators from the returned map are at least <i>weakly consistent</i>: they are safe for
     * concurrent use, but if the cache is modified (including by eviction) after the iterator is
     * created, it is undefined which of the changes (if any) will be reflected in that iterator.
     */
    ConcurrentMap<K, V> asMap();

    /**
     * Performs any pending maintenance operations needed by the cache. Exactly which activities are
     * performed -- if any -- is implementation-dependent.
     */
    void cleanUp();
}
