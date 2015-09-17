package org.kluge.remoting.server;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.server.handler.ResourceHandler;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.kluge.remoting.server.http.HttpRemotingSupervisor;
import org.kluge.remoting.server.socketio.SocketIORemotingServer;

/**
 * Created by giko on 12/25/14.
 */
public class Main {
    private int socketIOPort = 8082;
    private int jettyPort = 8085;

    public int getSocketIOPort() {
        return socketIOPort;
    }

    public void setSocketIOPort(int socketIOPort) {
        this.socketIOPort = socketIOPort;
    }

    public int getJettyPort() {
        return jettyPort;
    }

    public void setJettyPort(int jettyPort) {
        this.jettyPort = jettyPort;
    }

    public void run() {
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(socketIOPort);
        config.setMaxHttpContentLength(Integer.MAX_VALUE);
        config.setMaxFramePayloadLength(Integer.MAX_VALUE);
        config.getSocketConfig().setReuseAddress(true);
        config.setUseLinuxNativeEpoll(true);

        Server httpServer = new Server(jettyPort);
        SocketIOServer server = new SocketIOServer(config);

        RemotingServer<String> remotingServer = new SocketIORemotingServer<>(server, String.class,
                DomSharingSession::new);

        HandlerList handlers = new HandlerList();
        httpServer.setHandler(handlers);

        new HttpRemotingSupervisor(handler -> handlers.addHandler(handlers), remotingServer);
        ResourceHandler resources = new ResourceHandler();
        resources.setBaseResource(Resource.newClassPathResource("web-resources"));
        handlers.addHandler(new ResourceHandler());

        try {
            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException("An error occurred!", e);
        }

        try {
            server.startAsync().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("An error occurred!", e);
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        for (int i = 0; i < args.length; ++i) {
            switch (args[i++]) {
                case "-p":
                    if (i >= args.length) {
                        System.err.println("-p option requires argument");
                        printUsage();
                        return;
                    }
                    try {
                        main.setSocketIOPort(Integer.parseInt(args[i++]));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number");
                        printUsage();
                        return;
                    }
                    break;
                case "-s":
                    if (i >= args.length) {
                        System.err.println("-p option requires argument");
                        printUsage();
                        return;
                    }
                    try {
                        main.setJettyPort(Integer.parseInt(args[i++]));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port number");
                        printUsage();
                        return;
                    }
                    break;
                default:
                    System.err.println("Invalid argument: " + args[i]);
                    printUsage();
                    return;
            }
        }

        main.run();
    }

    private static void printUsage() {
        System.err.println("Usage: java org.kluge.remoting.server.Main [options]");
        System.err.println("where options are:");
        System.err.println("  -p <number>   socket.io port (8082 by default)");
        System.err.println("  -s <number>   supervisor/static resources port (8085 by default)");
    }
}
