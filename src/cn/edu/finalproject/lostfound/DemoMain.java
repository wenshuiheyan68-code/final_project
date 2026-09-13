package cn.edu.finalproject.lostfound;

import cn.edu.finalproject.lostfound.client.ClientMain;
import cn.edu.finalproject.lostfound.client.LostFoundClient;
import cn.edu.finalproject.lostfound.protocol.Request;
import cn.edu.finalproject.lostfound.server.LostFoundServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 一键演示入口：在同一进程内启动服务端线程，再通过 TCP 客户端执行发布、查询、认领和统计。
 * 正式使用时，请分别运行 ServerMain 与 ClientMain。
 */
public class DemoMain {
    public static void main(String[] args) throws Exception {
        int port = 9899;
        String demoDataFile = "data/demo-lost-found-" + System.currentTimeMillis() + ".dat";
        LostFoundServer server = new LostFoundServer(port, demoDataFile);
        Thread serverThread = new Thread(server::start, "lost-found-server-thread");
        serverThread.start();
        if (!server.awaitReady(3, TimeUnit.SECONDS)) {
            throw new IllegalStateException("演示服务启动超时");
        }

        LostFoundClient client = new LostFoundClient("127.0.0.1", port);
        Map<String, String> publish = new LinkedHashMap<>();
        publish.put("type", "FOUND");
        publish.put("title", "黑色校园卡");
        publish.put("description", "卡面带有蓝色挂绳，请凭姓名核验后认领。");
        publish.put("location", "图书馆一楼自习区");
        publish.put("contact", "张同学 13800000000");
        ClientMain.printResponse(client.request(new Request("publish", publish)));
        ClientMain.printResponse(client.request(new Request("search", Map.of("keyword", "校园卡", "type", "", "status", "OPEN"))));
        ClientMain.printResponse(client.request(new Request("claim", Map.of("id", "1", "claimer", "李同学"))));
        ClientMain.printResponse(client.request(new Request("stats", null)));

        server.close();
        serverThread.join(2000);
        System.out.println("演示结束，数据已保存至 " + demoDataFile + "。");
    }
}
