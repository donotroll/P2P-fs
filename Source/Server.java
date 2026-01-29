package Source;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Server {

    private class ActiveUser {

        public InetSocketAddress address;
        public String username;
        public long lastBeat;

        public ActiveUser(String username, InetSocketAddress address, long lastBeat) {
            this.username = username;
            this.address = address;
            this.lastBeat = lastBeat;
        }
    }

    private ServerUDP serverUDP;
    private HashMap<SocketAddress, ActiveUser> activeUsers = new HashMap<>();
    private static final int HEARTBEAT_TIMEOUT = 3000;
    private ScheduledExecutorService beatCheck = new ScheduledThreadPoolExecutor(1);
    private UserService userService;

    public Server(UserService service) {
        this.userService = service;
        serverUDP = new ServerUDP();
    }

    public Server(UserService service, int port) {
        this.userService = service;
        serverUDP = new ServerUDP(port);
    }

    public void run() {
        Runnable _beatCheck = new Thread() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<SocketAddress, ActiveUser>> iter = activeUsers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<SocketAddress, ActiveUser> e = iter.next();
                    if (now - e.getValue().lastBeat > HEARTBEAT_TIMEOUT) {
                        iter.remove();
                        System.out.println("removed:" + e.getValue().username);
                    }
                }
            }
        };

        beatCheck.scheduleAtFixedRate(_beatCheck, HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT, TimeUnit.MILLISECONDS);

        while (true) {
            DatagramPacket p = serverUDP.recieve();
            List<String> Line = Arrays
                    .asList(new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8).split(" "));

            ActiveUser requester = activeUsers.get(p.getSocketAddress());
            List<String> args = Line.size() == 1 ? new ArrayList<>() : Line.subList(1, Line.size());
            String response = handleRequest(Line.get(0), args, requester, p.getSocketAddress());
            
            requester = activeUsers.get(p.getSocketAddress());
            if (requester != null) {
            System.out.print(LocalDateTime.now().toString() + ": [" + String.join(" ", Line) + "] from user "
                    + requester.username + " ("
                    + p.getSocketAddress().toString() + ")\n");
            }
            else 
                System.out.print(LocalDateTime.now().toString() + ": [" + String.join(" ", Line) + "] from "
                        + p.getSocketAddress().toString() + "\n");
            

            if (response != null)
                serverUDP.send(response, new InetSocketAddress(p.getAddress(), p.getPort()));
        }
    }

    private List<String> getActiveUsernames() {
        return activeUsers.values().stream().map(u -> u.username).collect(Collectors.toList());
    }

    private String handleRequest(String command, List<String> args, ActiveUser requester, SocketAddress address) {
        String s = null;
        switch (command) {
            case "ping" -> {
                requester.lastBeat = System.currentTimeMillis();
            }
            case "get" -> {
                String senderName = userService.get(getActiveUsernames(), args.get(0));
                if (senderName != null) {
                    ActiveUser sender = activeUsers.values().stream().filter(u -> u.username.equals(senderName))
                            .findFirst().orElse(null);
                    s = sender.address.getAddress() + " " + sender.address.getPort();
                } else
                    s = String.format("invalid get for %s", args.get(0));
            }
            case "lap" -> {
                List<String> peers = getActiveUsernames().stream().filter(u -> !u.equals(requester.username))
                        .collect(Collectors.toList());
                if (peers.isEmpty())
                    s = "No active peers";
                else
                    s = "Active: \n " + String.join("\n ", peers);
            }
            case "lpf" -> {
                s = userService.lpf();
            }
            case "pub" -> {
                if (userService.pub(requester.username, args.get(0)))
                    s = String.format("File %s published sucessfully", args.get(0));
                else
                    s = String.format("File %s already exists for you", args.get(0));

            }
            case "unp" -> {
                if (userService.unp(requester.username, args.get(0)))
                    s = String.format("File %s unpublished sucessfully", args.get(0));
                else
                    s = String.format("File %s does not exist or you do not own this file", args.get(0));
            }
            case "sch" -> {
                List<String> peers = getActiveUsernames().stream()
                        .filter(u -> !u.equals(requester.username))
                        .collect(Collectors.toList());
                s = userService.sch(peers, args.get(0));
            }
            case "auth" -> {
                if (userService.auth(args.get(0), args.get(1)) && !getActiveUsernames().contains(args.get(0))) {
                    System.out.println("user active: " + args.get(0));
                    activeUsers.put(address,
                            new ActiveUser(args.get(0),
                                    new InetSocketAddress("localhost", Integer.parseInt(args.get(3))),
                                    System.currentTimeMillis()));
                    s = "valid";
                } else
                    s = "invalid";
            }
            default ->
                s = "unrecognised command" + command;
        }

        return s;
    }

    public static void main(String[] args) {
        try {
            UserService service = new UserService("credentials.txt");
            Server server;

            if (args.length == 1)
                server = new Server(service, Integer.parseInt(args[0]));
            else
                server = new Server(service);

            server.run();
        } catch (FileNotFoundException e) {
            System.out.println(e.toString());
        }
    }
}
