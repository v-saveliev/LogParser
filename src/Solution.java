import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution {
    public static int NUMBER_OF_LINE_TO_READ = 900000;
    static Set<String> AllIpAddresses = new HashSet<>();
    static  Map<Long, String> unreadSections = new HashMap<>();
    static List<LogReader> allReaders = new ArrayList<>();
    static Parser parser;
    static Matcher matcher;
    static  String filePath;
    static  String file1 = "C:\\dev\\111.txt";
    static  String file2 = "C:\\dev\\222.txt";
    static String file3 = "D:\\Learning\\111.txt";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
//            filePath = bufferedReader.readLine();
        } catch (IOException ioException) {
            System.out.println("can't read entered file path");
            ioException.printStackTrace();
            return;
        }

        try (FileReader fileReader = new FileReader(file3)) {

            File file = new File(file3);

            int threadsCount = 0;
            long lastFileIndex = file.length();
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            for (long i = 0; i <= lastFileIndex; i += NUMBER_OF_LINE_TO_READ) {
                LogReader logReader = new LogReader(fileReader, i, Math.min(i + NUMBER_OF_LINE_TO_READ, lastFileIndex), unreadSections);

                executorService.submit(logReader);
                allReaders.add(logReader);

                threadsCount++;
                // контроль количества запускаемых парсеров
                if (threadsCount == 10) {
                    executorService.shutdown();
                    executorService.awaitTermination(10, TimeUnit.MINUTES);
                    executorService = Executors.newFixedThreadPool(10);
                    threadsCount = 0;

                }
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.MINUTES);

            long endGeneralPartTime = System.currentTimeMillis();
            System.out.println("General part execution time: " + ((endGeneralPartTime-startTime) / 1000 ) + " seconds");

            Long[] keysUnreadSections = unreadSections.keySet().toArray(new Long[0]);
            Arrays.sort(keysUnreadSections);

            Pattern pattern = Pattern.compile("\\-\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\]");
            Matcher matcher;
            for (Long key : keysUnreadSections) {
                String unreadSection = unreadSections.get(key);
                matcher = pattern.matcher(unreadSection);
                while (matcher.find())
                    AllIpAddresses.add(unreadSection.substring(matcher.start() + 1, matcher.end() - 1));
            }

            System.out.println(allReaders.size());
            // запись полученных парсерами ип адресов в общую коллекцию
            for (LogReader curLogReader : allReaders) {
                Set<String> curReaderAddresses = curLogReader.getIpAddresses();
                for (String ipAddress : curReaderAddresses) {
                    AllIpAddresses.add(ipAddress);
                }
            }

            System.out.println(AllIpAddresses.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + ((endTime-startTime) / 1000 ) + " seconds");
    }
}
