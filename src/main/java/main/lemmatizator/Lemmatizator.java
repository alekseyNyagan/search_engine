package main.lemmatizator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemmatizator {

    private static final String HTML_TAG_REGEX = "\\<.*?\\>";
    private final LuceneMorphology morphology;

    public Lemmatizator() throws IOException {
        morphology = new RussianLuceneMorphology();
    }

    public Map<String, Integer> getLemmas(String field) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = field
                .replaceAll(HTML_TAG_REGEX, "")
                .toLowerCase()
                .replaceAll("[^А-Яа-я\\s]", "")
                .split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                if (!morphology.getMorphInfo(word).get(0).matches("(.*)(СОЮЗ|ПРЕДЛ|МЕЖД|ЧАСТ)(.*)")) {
                    String normalFormWord = morphology.getNormalForms(word).get(0);
                    int count = lemmas.getOrDefault(normalFormWord, 0);

                    lemmas.put(normalFormWord, ++count);
                }
            }
        }
        return lemmas;
    }
}
