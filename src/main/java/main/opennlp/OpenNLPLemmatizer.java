package main.opennlp;

import lombok.RequiredArgsConstructor;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.tokenize.Tokenizer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenNLPLemmatizer {

    private final Tokenizer tokenizer;
    private final POSTagger posTagger;
    private final Lemmatizer lemmatizer;

    public String[] getLemmas(String content) {
        String[] tokens = tokenizer.tokenize(content);
        String[] tags = posTagger.tag(tokens);
        return lemmatizer.lemmatize(tokens, tags);
    }
}
