package main.lemmatizator;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.WrongCharaterException;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

@Setter
@Getter
public class Lemmatizator {

    private static final String HTML_TAG_REGEX = "<.*?>";
    private LuceneMorphology morphology;

    public Lemmatizator() throws IOException {
        morphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> getLemmas(String field) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = getCleanWords(field);
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!isInvalidMorphInfo(word)) {
                    String normalFormWord = getWordNormalForm(word);
                    int count = lemmas.getOrDefault(normalFormWord, 0);

                    lemmas.put(normalFormWord, ++count);
                }
            }
        }
        return lemmas;
    }

    public Set<String> getQueryLemmas(String query) {
        Set<String> lemmas = new HashSet<>();
        String[] words = getCleanWords(query);
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!isInvalidMorphInfo(word)) {
                    lemmas.add(word);
                }
            }
        }
        return lemmas;
    }

    public String getWordNormalForm(String word) throws WrongCharaterException {
        return morphology.getNormalForms(word).get(0);
    }

    private String[] getCleanWords(String text) {
        return text
                .replaceAll(HTML_TAG_REGEX, "")
                .toLowerCase()
                .replaceAll("[^А-Яа-я\\s]", "")
                .split("\\s+");
    }

    private boolean isInvalidMorphInfo(String word) {
        return morphology.getMorphInfo(word).get(0).matches("(.*)(СОЮЗ|ПРЕДЛ|МЕЖД|ЧАСТ)(.*)");
    }
}