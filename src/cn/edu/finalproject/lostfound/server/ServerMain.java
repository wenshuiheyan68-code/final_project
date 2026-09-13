package cn.edu.finalproject.lostfound.server;

/** 服务端程序入口。 */
public class ServerMain {
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9876;
        LostFoundServer server = new LostFoundServer(port, "data/lost-found.dat");
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();
    }
}
