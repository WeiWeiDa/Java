package com.code.task.service;

import com.code.task.dto.Task;
import com.code.task.executor.TaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author hengjian
 * @date 2019/2/28
 */
public final class TaskService {
    private int poolSize;
    private String taskName;

    public void addTask(Task task) {
        TaskService.TaskCacheHolder.addTask(task);
    }

    public void addTaskList(List<Task> taskList) {
        TaskService.TaskCacheHolder.addTaskList(taskList);
    }

    public void execute(TaskExecutor taskExecutor) {
        List<Task> taskList = TaskService.TaskCacheHolder.getTaskList();
        ExecutorService executorService = this.getExecutorService();
        final CountDownLatch latch = new CountDownLatch(taskList.size());
        for (final Task task : taskList) {
            executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    try {
                        taskExecutor.before(task);
                        taskExecutor.executeTask(task);
                        taskExecutor.after(task);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        latch.countDown();
                    }
                    return null;
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted when processing data access request in concurrency", e);
        }
    }

    private ExecutorService getExecutorService() {
        TaskExecutorHolder taskExecutorHolder = new TaskExecutorHolder();
        return taskExecutorHolder.getExecutorService();
    }

    private final class TaskExecutorHolder {
        private ExecutorService executorService;

        TaskExecutorHolder() {

        }

        public ExecutorService getExecutorService() {
            if (this.executorService == null) {
                this.executorService = this.createExecutorService(poolSize, taskName);
            }
            return this.executorService;
        }

        private ExecutorService createExecutorService(int poolSize, final String threadName) {
            int coreSize = poolSize;
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "custom thread:" + threadName);
                    return thread;
                }
            };
            BlockingQueue<Runnable> queueToUse = new LinkedBlockingQueue<Runnable>();
            final ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize,
                    poolSize, 60, TimeUnit.SECONDS, queueToUse, threadFactory,
                    new ThreadPoolExecutor.CallerRunsPolicy());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        executor.shutdown();
                        executor.awaitTermination(1, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {

                    }
                }
            });
            return executor;
        }
    }

    private static final class TaskCacheHolder {
        private static final ThreadLocal<List<Task>> taskCache = new ThreadLocal<List<Task>>();

        public static void addTask(Task task) {
            List<Task> taskList = taskCache.get();
            if (taskList == null || taskList.size() == 0) {
                taskList = Collections.synchronizedList(new ArrayList<>());
                taskCache.set(taskList);
            }
            taskList.add(task);
        }

        public static void addTaskList(List<Task> taskList) {
            for (Task task : taskList) {
                TaskCacheHolder.addTask(task);
            }
        }

        public static List<Task> getTaskList() {
            return taskCache.get();
        }
    }
}
