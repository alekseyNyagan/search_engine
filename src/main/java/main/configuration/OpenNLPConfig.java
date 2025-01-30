package main.configuration;

import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class OpenNLPConfig {

    @Bean
    public Tokenizer tokenizerModel() throws IOException {
        try (InputStream modelIn = new FileInputStream("opennlp/opennlp-ru-ud-gsd-tokens-1.2-2.5.0.bin")) {
            return new TokenizerME(new TokenizerModel(modelIn));
        }
    }

    @Bean
    public POSTagger posTagger() throws IOException {
        try (InputStream modelIn = new FileInputStream("opennlp/opennlp-ru-ud-gsd-pos-1.2-2.5.0.bin")) {
            return new POSTaggerME(new POSModel(modelIn));
        }
    }

    @Bean
    public Lemmatizer lemmatizer() throws IOException {
        try (InputStream modelIn = new FileInputStream("opennlp/opennlp-ru-ud-gsd-lemmas-1.2-2.5.0.bin")) {
            return new LemmatizerME(new LemmatizerModel(modelIn));
        }
    }
}
