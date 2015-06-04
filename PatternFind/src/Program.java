import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by guy on 02/06/2015.
 */
public class Program {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            return;
        }

        String path = args[0];
        PatternFinder patternFinder = new PatternFinder();

        try (Stream<String> fileStream = Files.lines(Paths.get(path), Charset.defaultCharset())) {

            Stream<PatternHolder> patterns = patternFinder.FindPatternsInFile(fileStream);

            if (patterns.anyMatch(patternHolder -> patternHolder.getSentences() != null
                    && patternHolder.getSentences().count() > 0)) {

                patterns.forEach(pattern -> PrintPattern(pattern));
            }
            else {
                System.out.println("No Patterns Found.");
            }
        }
    }

    private static void PrintPattern(PatternHolder pattern) {

        pattern.getSentences().forEach(sentence -> System.out.println(sentence));
        System.out.printf("The changing word was: %s", pattern.getWords().collect(Collectors.joining(", ")));
                System.out.println();
    }
}
