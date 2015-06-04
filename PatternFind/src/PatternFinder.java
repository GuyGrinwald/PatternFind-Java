import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by guy on 02/06/2015.
 */
public class PatternFinder {

    private ArrayList<PatternHolder> patterns = new ArrayList<>();

    private final String TIME_STAMP_PREFIX_REGEX = "^\\d{2}-\\d{2}-\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\s";
    private Pattern sanitizeDateRegex = Pattern.compile(TIME_STAMP_PREFIX_REGEX);

    /**
     * Iterates through all of the lines in the stream and finds and returns the patterns in the file
     * @param fileStream
     * @return
     */
    public Stream<PatternHolder> FindPatternsInFile(Stream<String> fileStream) {

        fileStream.forEach(sentence -> {
            Stream<PatternHolder> matchingPatterns = FindMatchingPatterns(sentence);
            AddSentenceToPatterns(matchingPatterns, sentence);
        });

        return patterns.stream();
    }

    /**
     * Finds all of the patterns that match the given sentence
     * @param sentence
     * @return
     */
    private Stream<PatternHolder> FindMatchingPatterns(String sentence) {

        List<PatternHolder> matches = new ArrayList<>();

        if (sentence.isEmpty()) {
            return matches.stream();
        }

        // remove timestamp from sentence
        String sanitizedSentence = SanitizeSentence(sentence);

        // filter the right patterns for the sentence
        matches = patterns.stream().filter(patternHolder -> IsSentenceMatchPattern
                (patternHolder, sanitizedSentence)).collect(Collectors.toList());

        // create new pattern if no matching pattern exists
        if (matches.isEmpty()) {
            PatternHolder newPattern = new PatternHolder(sanitizedSentence, new ArrayList<String>(), new ArrayList<String>());
            patterns.add(newPattern);
            matches.add(newPattern);
        }

        return matches.stream();
    }

    /**
     * Adds the given sentence to the given pattern
     * @param matchingPatterns
     * @param originalSentence
     */
    private void AddSentenceToPatterns(Stream<PatternHolder> matchingPatterns, String originalSentence) {

        if (matchingPatterns.count() == 0 || originalSentence.isEmpty())
        {
            return;
        }

        String sanitizedSentence = SanitizeSentence(originalSentence);

        // for each matching pattern add the sentence to it's patternHolder

        matchingPatterns.forEach(match -> {

            int diffWordIndex = FindDiffWord(match.getPattern(), sanitizedSentence);
            String[] patternWords = match.getPattern().split(" ");
            String[] sentenceWords = sanitizedSentence.split(" ");

            // ignore duplicate sentences
            Stream<String> sanitizedSentences = match.getSentences().map(original -> SanitizeSentence(original));

            if (sanitizedSentences.anyMatch(sanitized -> sanitized.equalsIgnoreCase(sanitizedSentence)))
                return;

            // if the pattern is new just add the sentence to it - we cant know the diff word yet
            if (match.getSentences().count() == 0)
            {
                match.addSentence(originalSentence);
                return;
            }

            // handel the case for the first pattern found - adds it's word to the patternHolder
            if (match.getWords().count() == 0)
            {
                match.addWord(patternWords[diffWordIndex]);
            }

            match.addWord(sentenceWords[diffWordIndex]);

            match.addSentence(originalSentence);
        });
    }

    private String SanitizeSentence(String sentence) {
        return sanitizeDateRegex.matcher(sentence).replaceAll("");
    }

    private boolean IsSentenceMatchPattern(PatternHolder patternHolder, String sanitizedSentence) {

        if (patternHolder.getPattern().isEmpty() || sanitizedSentence.isEmpty())
            return false;

        // if the sentence already exists in the pattern then the new copy of it also matches the pattern
        Stream<String> sanitizedSentences = patternHolder.getSentences().map(original -> SanitizeSentence(original));
        if (sanitizedSentences.anyMatch(sanitized -> sanitized.equalsIgnoreCase(sanitizedSentence)))
            return true;

        // the sentence matches the pattern iff they have only one word that differs between them in the same location;
        int diffWordsIndex = FindDiffWord(patternHolder.getPattern(), sanitizedSentence);
        boolean patternMatch = diffWordsIndex >= 0;

        return patternMatch;
    }

    /**
     * Finds the only different word in each string and it's index.
     * The function assumes the input is a grammical proper english sentence.
     * @param phrase1
     * @param phrase2
     * @return The index of the different word or -1 if none is found.
     */
    private int FindDiffWord(String phrase1, String phrase2) {
        int diffWordLocation = -1;

        class WordTuple {
            private String wordInPhrase1;
            private String wordInPhrase2;
            private int wordIndex;

            public WordTuple(String word1, String word2, int wordIndex) {
                this.wordInPhrase1 = word1;
                this.wordInPhrase2 = word2;
                this.wordIndex = wordIndex;
            }
        }

        if (phrase1.isEmpty() || phrase2.isEmpty())
        {
            return diffWordLocation;
        }

        List<String> phrase1Words = Arrays.asList(phrase1.split(" "));
        String[] phrase2Words = phrase2.split(" ");

        if (phrase1Words.size() != phrase2Words.length)
        {
            return diffWordLocation;
        }

        // create tuples of words form each collection and their index and filters those whose words are not the same
        Stream<WordTuple> wordTupels = phrase1Words.stream().map(word -> new WordTuple(word,
                phrase2Words[phrase1Words.indexOf(word)], phrase1Words.indexOf(word)));

        Stream<WordTuple> diffWords = wordTupels.filter(wordTuple ->
                !wordTuple.wordInPhrase1.equalsIgnoreCase(wordTuple.wordInPhrase2));

        if (diffWords.count() == 1)
        {
            diffWordLocation = diffWords.findFirst().get().wordIndex;
        }

        return diffWordLocation;
    }
}
