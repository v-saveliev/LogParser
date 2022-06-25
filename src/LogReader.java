import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import sun.security.util.ArrayUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader extends Thread {
    private FileReader fileReader;
    private long startIndex;
    private long endIndex;
    private Map<Long, String> unreadSections;
    private char[] charArray = new char[Solution.NUMBER_OF_LINE_TO_READ];
    private HashSet<String> ipAddresses = new HashSet<>();

    public LogReader(FileReader fileReader, long startIndex, long endIndex, Map<Long, String> unreadSections) {
        this.fileReader = fileReader;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.unreadSections = unreadSections;
    }

    @Override
    public void run() {
        readCharsFromFile();
    }

    private boolean readCharsFromFile() {
        try {
            long startTime = System.currentTimeMillis();

            fileReader.read(charArray);

            long endTime = System.currentTimeMillis();
            if (startIndex == 0)
            System.out.println("fileReadingTime execution time: " + (endTime-startTime) + " ms");

            int firstBreak = 0, lastBreak = 0;
            collectUnusedSections(firstBreak, lastBreak);
            recordUniqueAddresses(firstBreak, lastBreak);

            return true;
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
    }

    private boolean collectUnusedSections(int firstBreak, int lastBreak) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < Solution.NUMBER_OF_LINE_TO_READ; i++) {
            if (charArray[i] == 10) {
                firstBreak = i;
                break;
            }
        }

        for (int i = Solution.NUMBER_OF_LINE_TO_READ - 1; i >= 0; i--) {
            if (charArray[i] == 10) {
                lastBreak = i;
                break;
            }
        }

        if (firstBreak == 0 || lastBreak == 0) {
            return false;
        }

        String firstUnreadSection = String.valueOf(Arrays.copyOfRange(charArray, 0, firstBreak));
        String lastUnreadSection  = String.valueOf(Arrays.copyOfRange(charArray, lastBreak, Solution.NUMBER_OF_LINE_TO_READ));

        unreadSections.put(startIndex, firstUnreadSection);
        unreadSections.put(endIndex, lastUnreadSection);

        long endTime = System.currentTimeMillis();
        if (startIndex == 0)
            System.out.println("makesUnreadSectionexecution time: " + (endTime - startTime) + " ms");

        return true;
    }

    private void recordUniqueAddresses(int firstBreak, int lastBreak) {
        long startParsingTime = System.currentTimeMillis();
        CharBuffer charBuffer = CharBuffer.allocate(Solution.NUMBER_OF_LINE_TO_READ);
        charBuffer.put(charArray, firstBreak, lastBreak - firstBreak);

        StringBuffer parsingStringBuffer = new StringBuffer(charBuffer);
        charBuffer = null;
        charArray = null;
        Pattern pattern = Pattern.compile("\\-\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\]");
        Matcher matcher = pattern.matcher(parsingStringBuffer);
        while (matcher.find()) {//
            ipAddresses.add(parsingStringBuffer.substring(matcher.start() + 1, matcher.end() - 1));
        }
        parsingStringBuffer = null;



        long endParsingTime = System.currentTimeMillis();
        if (startIndex == 0)
            System.out.println("generalTime time: " + (endParsingTime - startParsingTime) + " ms");
    }

    public HashSet<String> getIpAddresses() {
        return ipAddresses;
    }
}
