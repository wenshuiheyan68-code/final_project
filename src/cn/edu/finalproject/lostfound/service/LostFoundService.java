package cn.edu.finalproject.lostfound.service;

import cn.edu.finalproject.lostfound.command.RemoteCommand;
import cn.edu.finalproject.lostfound.model.ItemStatus;
import cn.edu.finalproject.lostfound.model.ItemType;
import cn.edu.finalproject.lostfound.model.LostItem;
import cn.edu.finalproject.lostfound.protocol.Request;
import cn.edu.finalproject.lostfound.protocol.Response;
import cn.edu.finalproject.lostfound.repository.InMemoryItemRepository;
import cn.edu.finalproject.lostfound.repository.ItemRepository;
import cn.edu.finalproject.lostfound.repository.SnapshotStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 校园失物招领系统的核心业务服务。 */
public class LostFoundService {
    private final ItemRepository repository;
    private final SnapshotStore snapshotStore;

    public LostFoundService(SnapshotStore snapshotStore) {
        this.snapshotStore = snapshotStore;
        this.repository = new InMemoryItemRepository(snapshotStore.load());
    }

    @RemoteCommand("publish")
    public synchronized Response publish(Request request) {
        ItemType type = ItemType.from(request.get("type"));
        LostItem item = new LostItem(repository.nextId(), type, request.get("title"),
                request.get("description"), request.get("location"), request.get("contact"));
        repository.add(item);
        saveNow();
        return Response.items("发布成功，记录编号为 " + item.getId(), List.of(item));
    }

    @RemoteCommand("list")
    public Response list(Request request) {
        return Response.items("当前共有 " + repository.findAll().size() + " 条记录", repository.findAll());
    }

    @RemoteCommand("search")
    public Response search(Request request) {
        List<LostItem> result = repository.search(request.get("keyword"), request.get("type"), request.get("status"));
        return Response.items("查询到 " + result.size() + " 条记录", result);
    }

    @RemoteCommand("detail")
    public Response detail(Request request) {
        LostItem item = repository.find(parseId(request));
        return item == null ? Response.error("未找到该编号的记录") : Response.items("查询成功", List.of(item));
    }

    @RemoteCommand("claim")
    public synchronized Response claim(Request request) {
        LostItem item = repository.find(parseId(request));
        if (item == null) {
            return Response.error("未找到该编号的记录");
        }
        if (!item.claim(request.get("claimer"))) {
            return Response.error("该物品已经被认领，不能重复认领");
        }
        saveNow();
        return Response.items("认领登记成功", List.of(item));
    }

    @RemoteCommand("remove")
    public synchronized Response remove(Request request) {
        LostItem removed = repository.remove(parseId(request));
        if (removed == null) {
            return Response.error("未找到该编号的记录");
        }
        saveNow();
        return Response.ok("已删除编号 " + removed.getId() + " 的记录");
    }

    @RemoteCommand("stats")
    public Response stats(Request request) {
        Map<String, Integer> statistics = new LinkedHashMap<>();
        int total = repository.findAll().size();
        int lost = repository.search("", ItemType.LOST.name(), "").size();
        int found = repository.search("", ItemType.FOUND.name(), "").size();
        int open = repository.search("", "", ItemStatus.OPEN.name()).size();
        int claimed = repository.search("", "", ItemStatus.CLAIMED.name()).size();
        statistics.put("记录总数", total);
        statistics.put("寻物信息", lost);
        statistics.put("招领信息", found);
        statistics.put("待认领", open);
        statistics.put("已认领", claimed);
        return Response.statistics("统计完成", statistics);
    }

    @RemoteCommand("help")
    public Response help(Request request) {
        return Response.ok("支持命令：publish、list、search、detail、claim、remove、stats、help");
    }

    public synchronized void saveNow() {
        snapshotStore.save(repository.snapshot());
    }

    private long parseId(Request request) {
        try {
            return Long.parseLong(request.get("id"));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("编号必须是整数");
        }
    }
}
