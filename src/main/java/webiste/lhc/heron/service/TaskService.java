package webiste.lhc.heron.service;

import com.github.pagehelper.PageInfo;
import webiste.lhc.heron.dto.TaskDto;
import webiste.lhc.heron.model.Task;

/**
 * @description:
 * @author: 582895699@qq.com
 * @time: 2020/9/28 下午 10:53
 */
public interface TaskService {
    void addTask(TaskDto dto);

    void updateTask(TaskDto dto);

    PageInfo<Task> listTasks(int offset, int size);

    void initTasks();

    void removeTask(long id);
}
