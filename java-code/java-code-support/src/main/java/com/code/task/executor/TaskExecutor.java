package com.code.task.executor;

import com.code.task.dto.Task;

/**
 * @author hengjian
 * @date 2019/2/28
 */
public interface TaskExecutor {
    public void before(Task task);

    public void executeTask(Task task);

    public void after(Task task);
}
