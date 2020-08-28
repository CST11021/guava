/*
 * Copyright (C) 2014 The Guava Authors
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

package com.google.common.eventbus;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Queues;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 用于将事件分发给订阅者的处理程序
 *
 * <p><b>Note:</b> The dispatcher is orthogonal to the subscriber's {@code Executor}. The dispatcher
 * controls the order in which events are dispatched, while the executor controls how (i.e. on which
 * thread) the subscriber is actually called when an event is dispatched to it.
 *
 * @author Colin Decker
 */
abstract class Dispatcher {

    /**
     * 返回一个调度程序，该调度程序在发布订阅者后立即将事件分发给订阅者，而无需使用中间队列来更改分发顺序。
     * 与使用队列时的广度优先相比，这实际上是深度优先的调度顺序。
     */
    static Dispatcher immediate() {
        return ImmediateDispatcher.INSTANCE;
    }

    /**
     * 返回一个分派器，该分派器将在已分派事件的线程上重新进入的事件排队，以确保在单个线程上分派的所有事件均按其发布顺序分派给所有订阅者，默认的EventBus实现
     */
    static Dispatcher perThreadDispatchQueue() {
        return new PerThreadQueuedDispatcher();
    }

    /**
     * 返回一个调度程序：该调度程序将发布在单个全局队列中的事件排队，次实现配合AsyncEventBus一起使用，对于同步调度，通常最好使用{@linkplain #immediate()}立即调度程序
     */
    static Dispatcher legacyAsync() {
        return new LegacyAsyncDispatcher();
    }

    /**
     * Dispatches the given {@code event} to the given {@code subscribers}.
     */
    abstract void dispatch(Object event, Iterator<Subscriber> subscribers);


    /**
     * 单线程立即同步执行的监听回调实现
     */
    private static final class ImmediateDispatcher extends Dispatcher {

        private static final ImmediateDispatcher INSTANCE = new ImmediateDispatcher();

        @Override
        void dispatch(Object event, Iterator<Subscriber> subscribers) {
            checkNotNull(event);
            while (subscribers.hasNext()) {
                subscribers.next().dispatchEvent(event);
            }
        }
    }

    /**
     * 每个线程都有对应的事件队列，事件发布时，依次从队列中获取时间，并执行事件回调程序，该实现是线程独立的，因此是线程安全的
     */
    private static final class PerThreadQueuedDispatcher extends Dispatcher {

        // 该调度程序与EventBus的原始调度行为相匹配

        /**
         * 表示每个线程的事件队列，该队列是有序队列
         */
        private final ThreadLocal<Queue<Event>> queue = new ThreadLocal<Queue<Event>>() {
            @Override
            protected Queue<Event> initialValue() {
                return Queues.newArrayDeque();
            }
        };

        /**
         * 标记当前线程是否在执行监听回调程序
         */
        private final ThreadLocal<Boolean> dispatching = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return false;
            }
        };

        @Override
        void dispatch(Object event, Iterator<Subscriber> subscribers) {
            // event和subscribers都不允许为null
            checkNotNull(event);
            checkNotNull(subscribers);

            // 获取当前线程的事件队列
            Queue<Event> queueForThread = queue.get();
            // 将事件添加到队列中
            queueForThread.offer(new Event(event, subscribers));

            // 遍历事件队列，依次调用监听器回调
            if (!dispatching.get()) {
                dispatching.set(true);
                try {
                    Event nextEvent;
                    while ((nextEvent = queueForThread.poll()) != null) {
                        while (nextEvent.subscribers.hasNext()) {
                            nextEvent.subscribers.next().dispatchEvent(nextEvent.event);
                        }
                    }
                } finally {
                    dispatching.remove();
                    queue.remove();
                }
            }
        }

        /**
         * 分支事件源和监听器
         */
        private static final class Event {
            private final Object event;
            private final Iterator<Subscriber> subscribers;

            private Event(Object event, Iterator<Subscriber> subscribers) {
                this.event = event;
                this.subscribers = subscribers;
            }
        }
    }

    /**
     * 将事件放到全局的事件队列中，适合高并发的事件发布场景
     */
    private static final class LegacyAsyncDispatcher extends Dispatcher {

        // This dispatcher matches the original dispatch behavior of AsyncEventBus.
        //
        // We can't really make any guarantees about the overall dispatch order for this dispatcher in
        // a multithreaded environment for a couple reasons:
        //
        // 1. Subscribers to events posted on different threads can be interleaved with each other
        //    freely. (A event on one thread, B event on another could yield any of
        //    [a1, a2, a3, b1, b2], [a1, b2, a2, a3, b2], [a1, b2, b3, a2, a3], etc.)
        // 2. It's possible for subscribers to actually be dispatched to in a different order than they
        //    were added to the queue. It's easily possible for one thread to take the head of the
        //    queue, immediately followed by another thread taking the next element in the queue. That
        //    second thread can then dispatch to the subscriber it took before the first thread does.
        //
        // All this makes me really wonder if there's any value in queueing here at all. A dispatcher
        // that simply loops through the subscribers and dispatches the event to each would actually
        // probably provide a stronger order guarantee, though that order would obviously be different
        // in some cases.

        /**
         * Global event queue.
         */
        private final ConcurrentLinkedQueue<EventWithSubscriber> queue = Queues.newConcurrentLinkedQueue();

        @Override
        void dispatch(Object event, Iterator<Subscriber> subscribers) {
            checkNotNull(event);
            while (subscribers.hasNext()) {
                queue.add(new EventWithSubscriber(event, subscribers.next()));
            }

            EventWithSubscriber e;
            while ((e = queue.poll()) != null) {
                e.subscriber.dispatchEvent(e.event);
            }
        }

        private static final class EventWithSubscriber {
            private final Object event;
            private final Subscriber subscriber;

            private EventWithSubscriber(Object event, Subscriber subscriber) {
                this.event = event;
                this.subscriber = subscriber;
            }
        }
    }


}
