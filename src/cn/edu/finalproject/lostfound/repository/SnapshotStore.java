package cn.edu.finalproject.lostfound.repository;

import cn.edu.finalproject.lostfound.model.LostItem;

import java.util.Map;

/** 本地快照存储接口。 */
public interface SnapshotStore {
    Map<Long, LostItem> load();

    void save(Map<Long, LostItem> items);
}
