import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Solution {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        Runtime.getRuntime().gc();
        // коллекция для хранения уникальных ип адресов из читаемого лога
        Set<String> AllIpAddresses = new HashSet<>();
        Parser parser;
        String filePath;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            filePath = bufferedReader.readLine();
        } catch (IOException ioException) {
            System.out.println("can't read entered file path");
            ioException.printStackTrace();
            return;
        }

        try (BufferedReader logReader = new BufferedReader(new FileReader(filePath))) {

            //List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            // Files.newBufferedReader() почитать про данный метод с использованием массива символов в качестве аргумента
            // переписать на построчное чтение с записью каждой тысячи строк в List и отправкой в поток Parser

            List<Parser> allParsers = new ArrayList<>();
            String[] stringArray = new String[1000];
            int count = 0;

            Pattern pattern = Pattern.compile("\\-\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\]");
            while (logReader.ready()) {
                // каждая 1000 строк уходит в новый поток для парсинга
                if (count == 999) {
                    parser = new Parser(stringArray, pattern);
                    allParsers.add(parser);
                    parser.start();

                    count = 0;
                    stringArray = new String[1000];
                }

                stringArray[count] = logReader.readLine();
                count++;
            }

           // ожидание завершения работы всех парсеров
            for (Parser curParser : allParsers) {
                curParser.join();
            }

            // запись полученных парсерами ип адресов в общую коллекцию
            for (Parser curParser : allParsers) {
                Set<String> curParserAddresses = curParser.getIpAddresses();
                for (String ipAddress : curParserAddresses) {
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
