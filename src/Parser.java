import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser extends Thread {
    private String[] lines;
    private Set<String> ipAddresses = new HashSet<>();
    Matcher matcher;
    Pattern pattern;

    public Parser(String[] lines, Pattern pattern) {
        this.lines = lines;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        readIpAddresses();
    }

    private void readIpAddresses() {
        try {
            for (String line : lines) {
                if (line == null)
                    continue;
                matcher = pattern.matcher(line);
                if (matcher.find())
                    ipAddresses.add(line.substring(matcher.start() + 1, matcher.end() - 1));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        lines = null;
    }

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }
}
