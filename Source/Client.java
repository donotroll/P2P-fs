package Source;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {

    private final Scanner scanner = new Scanner(System.in);
    private ClientUDP ClientUDP;
    private final WelcomeTCP welcomeTCP = new WelcomeTCP();
    private final Map<String, Integer> validCommands = Map.of(
            "xit", 1,
            "lpf", 1,
            "sch", 2,
            "lap", 1,
            "pub", 2,
            "unp", 2,
            "get", 2);


    public Client(int ServerPort) {
         ClientUDP = new ClientUDP(ServerPort);
    }

    public Client() {
         ClientUDP = new ClientUDP();
    }

    public void run() {
        while (!authenticate())
            System.out.println("Invalid Credentials");

        ClientUDP.startHeart();
        new Thread(welcomeTCP).start();

        while (true) {
            List<String> Line = new ArrayList<>();
            prompt(">", Line);
            String command = Line.get(0);

            if (!validCommands.containsKey(command) || !validCommands.get(command).equals(Line.size())) {
                System.out.println("Enter valid a command");
                continue;
            }

            if (command.equals("xit")) {
                ClientUDP.stop();
                welcomeTCP.stop();
                System.out.println("closing");
                break;
            }
            else {
                ClientUDP.send(String.join(" ", Line));
            }

            String recieved = ClientUDP.recieve().strip();
            if (command.equals("get") && !recieved.equals(String.format("invalid get for %s", Line.get(1)))) {
                String[] address = recieved.split(" ");
                welcomeTCP.download(Line.get(1), new InetSocketAddress("localhost", Integer.parseInt(address[1])));
            }
            else
                System.out.print(recieved + "\n");
        }
    }

    public boolean authenticate() {
        List<String> Line = new ArrayList<>();

        Line.add("auth");
        prompt("username: ", Line);
        prompt("password: ", Line);
        Line.add(welcomeTCP.getAddress().toString());
        Line.add(String.valueOf(welcomeTCP.getPort()));

        if (Line.size() != 5)
            return false;

        ClientUDP.send(String.join(" ", Line));

        String s = ClientUDP.recieve();
        return s.equals("valid");
    }

    public void prompt(String prompt, List<String> Line) {
        System.out.print(prompt);
        Line.addAll(Arrays.asList(scanner.nextLine().strip().split(" ")));
    }
    
    public static void main(String[] args) {
        Client client;
        if (args.length == 1)
            client = new Client(Integer.parseInt(args[0]));
        else
            client = new Client();
        client.run();
    }
}
