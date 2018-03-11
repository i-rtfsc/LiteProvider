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

/**
 * An abstract class to implement a task executed on
 * {@link com.journeyOS.liteprovider.globals.task.SerialExecutor}.
 */
public abstract class AbstractTask implements Runnable {

    /**
     * A unique ID for this task.
     *
     * @see #setId(String)
     */
    protected String mTaskId;

    /**
     * The listener to observe the status of this task.
     *
     * @see #setListener(com.journeyOS.liteprovider.globals.task.AbstractTask.TaskListener)
     */
    protected TaskListener mListener;

    public AbstractTask() {
    }

    public abstract void execute() throws InterruptedException;

    /* package */ void setId(String id) {
        mTaskId = id;
    }

    /* package */ String getId() {
        return mTaskId;
    }

    /* package */ void setListener(TaskListener l) {
        mListener = l;
    }

    @Override
    public void run() {
        onStart(mTaskId);
        try {
            execute();
        } catch (InterruptedException e) {
            onCancel(mTaskId);
        } finally {
            onComplete(mTaskId);
        }
    }

    protected void onStart(String taskId) {
        if (mListener != null) {
            mListener.onStart(mTaskId);
        }
    }

    protected void onCancel(String taskId) {
        if (mListener != null) {
            mListener.onCancel(mTaskId);
        }
    }

    protected void onComplete(String taskId) {
        if (mListener != null) {
            mListener.onComplete(mTaskId);
        }
    }

    protected void throwInterruptedExceptionIfNeeded() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException(Thread.currentThread().getName() + " was interrupted");
        } else {
            // Do nothing.
        }
    }

    /* package */ interface TaskListener {
        public void onStart(String taskId);

        public void onCancel(String taskId);

        public void onComplete(String taskId);

    }
}
