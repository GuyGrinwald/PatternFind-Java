import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by guy on 02/06/2015.
 */
public class PatternHolder {

    private ArrayList<String> sentences;
    private ArrayList<String> words;
    private String pattern;

    public PatternHolder(String pattern, ArrayList<String> sentences, ArrayList<String> words) {
        this.sentences = sentences;
        this.words = words;
        this.pattern = pattern;
    }

    public Stream<String> getSentences() {
        return sentences.stream();
    }

    public void addSentence(String sentence) {
        sentences.add(sentence);
    }

    public Stream<String> getWords() {
        return words.stream();
    }

    public void addWord(String word) {
        words.add(word);
    }

    public String getPattern() {
        return this.pattern;
    }
}
