package Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UserService {

    private final HashMap<String, String> credentials = new HashMap<>();
    private final HashMap<String, List<String>> files = new HashMap<>();

    public UserService(String credentialsPath) throws FileNotFoundException {
        try (Scanner fin = new Scanner(new File(credentialsPath))) {
            while (fin.hasNext()) {
                String[] user = fin.nextLine().split(" ");
                credentials.put(user[0], user[1]);
                files.put(user[0], new ArrayList<>());
                System.out.println(user[0] + " " + user[1]);
            }
        }
    }

    public String sch(List<String> peers, String substr) {
        List<String> l = files.entrySet().stream()
                                .filter(e -> peers.contains(e.getKey()))
                                .map(Map.Entry::getValue)
                                .flatMap(List::stream)
                                .filter(f -> f.contains(substr))
                                .collect(Collectors.toList());
        if (l.isEmpty())
            return "No files found";
        else
            return String.format("Downloadable files: \n %s", String.join("\n ", l));
    }

    public String get(List<String> peers, String filename) {
        return files.entrySet().stream()
                .filter(e -> peers.contains(e.getKey()) && e.getValue().contains(filename))
                .map(e -> e.getKey())
                .findFirst()
                .orElse(null);
    }

    public boolean auth(String username, String password) {
        System.out.println(username + " " + password);
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    public String lpf() {
        List<String> l = files.values().stream().flatMap(List::stream).collect(Collectors.toList());
        if (l.isEmpty())
            return "No files found";
        else return String.format("Available files: \n %s",
                String.join("\n ",l));
    }

    public boolean pub(String requester, String path) {
        if (files.get(requester).contains(path)) {
            return false;
        }

        return files.get(requester).add(path);
    }

    public boolean unp(String requester, String path) {
        if (!files.get(requester).contains(path)) {
            return false;
        }

        return files.get(requester).remove(path);
    }
}
