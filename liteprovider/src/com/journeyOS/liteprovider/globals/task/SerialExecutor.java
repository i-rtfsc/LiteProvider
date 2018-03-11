/*
 * Copyright (c) 2018 anqi.huang@outlook.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.journeyOS.liteprovider.globals.task;

import com.journeyOS.liteprovider.globals.task.AbstractTask.TaskListener;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * The serial task executor.
 */
public class SerialExecutor {

    private static final String DEFAULT_TASK_NAME = "Executor";

    private static final long SHUTDOWN_TIMEOUT_MILLIS = 1000;

    private ExecutorService mExecutor;
    private TaskPool mPool;

    private String mTaskName;

    public SerialExecutor() {
        this(DEFAULT_TASK_NAME);
    }

    public SerialExecutor(String taskName) {
        mTaskName = taskName;
        ThreadFactory threadFactory = null;
        if (mTaskName == null) {
            threadFactory = Executors.defaultThreadFactory();
        } else {
            threadFactory = new NamedThreadFactory(mTaskName);
        }

        mExecutor = Executors.newSingleThreadExecutor(threadFactory);
        mPool = new TaskPool();
    }

    @Override
    public void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }

    public void destroy() {
        try {
            mExecutor.shutdown();
            if (mExecutor.awaitTermination(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                mExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            mExecutor.shutdownNow();
        }
    }

    public String getTaskName() {
        return mTaskName;
    }

    public String execute(AbstractTask task) {
        UUID uuid = UUID.randomUUID();
        String taskId = uuid.toString();
        task.setId(taskId);
        task.setListener(mPool);
        mPool.addTask(taskId, mExecutor.submit(task));
        return taskId;
    }

    /**
     * The task pool to manage the current tasks.
     */
    private static class TaskPool implements TaskListener {

        /**
         * The map of {@link Future}s.
         */
        private Map<String, Future<?>> mTaskMap = new ConcurrentHashMap<String, Future<?>>();

        public TaskPool() {
        }

        public void addTask(String taskId, Future<?> futureTask) {
            mTaskMap.put(taskId, futureTask);
        }

        public boolean removeTask(String taskId) {
            return (mTaskMap.remove(taskId) != null);
        }

        public Set<String> getTaskIdSet() {
            return mTaskMap.keySet();
        }

        public Collection<Future<?>> getTasks() {
            return mTaskMap.values();
        }

        public Future<?> getTask(String taskId) {
            return mTaskMap.get(taskId);
        }

        @Override
        public void onStart(String taskId) {
        }

        @Override
        public void onCancel(String taskId) {
            removeTask(taskId);
        }

        @Override
        public void onComplete(String taskId) {
            removeTask(taskId);
        }
    }

    /**
     * The factory to create a named thread.
     */
    private static class NamedThreadFactory implements ThreadFactory {

        /**
         * The thread name.
         */
        private String mName;

        /**
         * The thread group.
         */
        private final ThreadGroup mGroup;

        public NamedThreadFactory(String name) {
            mName = name;
            Thread currentThread = Thread.currentThread();
            mGroup = currentThread.getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            // The current thread group will be used.
            Thread task = new Thread(mGroup, r, mName);
            if (task.isDaemon()) {
                task.setDaemon(false);
            }

            int priority = task.getPriority();
            if (priority != Thread.NORM_PRIORITY) {
                task.setPriority(Thread.NORM_PRIORITY);
            }
            return task;
        }
    }

}
