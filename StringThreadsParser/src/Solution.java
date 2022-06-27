import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Solution {
    private static final int NUMBER_OF_LINES_TO_READ = 10000;
    private static final String IPADDRESS_PATTERN = "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b";
    private static final Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
    private static final Set<String> AllUniqueIpAddresses = new HashSet<>();
    private static final List<Parser> allParsers = new ArrayList<>();
    private static String[] stringArray = new String[NUMBER_OF_LINES_TO_READ];
    private static Parser parser;
    private static String filePath;

    public static void main(String[] args) {
       if (readFilePath())
           readUniqueAddresses();
    }

    private static boolean readFilePath() {
        boolean result = false;
        System.out.println("Enter the path to the parsing file");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            filePath = bufferedReader.readLine();
            result = true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return result;
    }

    private static void readUniqueAddresses() {
        long startTime = System.currentTimeMillis();

        try (BufferedReader logReader = new BufferedReader(new FileReader(filePath))) {
            // чтение строк файла, фиксированное количество строк отправляется новому потоку на разбор
            int count = 0;
            while ((stringArray[count] = logReader.readLine()) != null) {
                if (count == NUMBER_OF_LINES_TO_READ - 1) {
                    startingStringParserThread();

                    stringArray = new String[NUMBER_OF_LINES_TO_READ];
                    count = -1;
                }
                count++;
            }
            // передача последних прочитанных строк входящего файла парсеру
            startingStringParserThread();
            //ожидание завершения работы всех потоков парсеров
            for (Parser curParser : allParsers) {
                curParser.join();
            }

            // запись полученных парсерами ип адресов в HashSet для контроля уникальности
            for (Parser curParser : allParsers) {
                Set<String> curParserAddresses = curParser.getIpAddresses();
                for (String ipAddress : curParserAddresses) {
                    AllUniqueIpAddresses.add(ipAddress);
                }
            }

            System.out.println(AllUniqueIpAddresses.size() + " addresses have been read.");
            System.out.println("Received addresses:\n" + AllUniqueIpAddresses.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + ((endTime-startTime) / 1000 ) + " seconds");
    }

    private static void startingStringParserThread() {
        parser = new Parser(stringArray, pattern);
        allParsers.add(parser);
        parser.start();
    }
}
