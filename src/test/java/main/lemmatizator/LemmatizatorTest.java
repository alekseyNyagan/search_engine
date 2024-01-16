package main.lemmatizator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LemmatizatorTest {

    private static final String FIELD = "Это тестовое поле";
    private static final String QUERY = "Это тестовый запрос";
    private AutoCloseable closeable;

    @Mock
    private LuceneMorphology morphology;

    private Lemmatizator lemmatizator;

    @BeforeEach
    void initService() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        lemmatizator = new Lemmatizator();
        lemmatizator.setMorphology(morphology);
    }

    @AfterEach
    void closeService() throws Exception {
        closeable.close();
    }

    @Test
    void testGetLemmas() {
        when(morphology.getMorphInfo("тестовое")).thenReturn(Collections.singletonList("П"));
        when(morphology.getNormalForms("тестовое")).thenReturn(Collections.singletonList("тестовый"));
        when(morphology.getMorphInfo("это")).thenReturn(Collections.singletonList("МС-П"));
        when(morphology.getNormalForms("это")).thenReturn(Collections.singletonList("этот"));
        when(morphology.getMorphInfo("поле")).thenReturn(Collections.singletonList("С"));
        when(morphology.getNormalForms("поле")).thenReturn(Collections.singletonList("поль"));

        Map<String, Integer> expectedLemmas = new HashMap<>();
        expectedLemmas.put("тестовый", 1);
        expectedLemmas.put("этот", 1);
        expectedLemmas.put("поль", 1);

        Map<String, Integer> actualLemmas = lemmatizator.getLemmas(FIELD);

        assertEquals(expectedLemmas, actualLemmas);
    }

    @Test
    void testGetQueryLemmas() {
        when(morphology.getMorphInfo("тестовый")).thenReturn(Collections.singletonList("П"));
        when(morphology.getMorphInfo("это")).thenReturn(Collections.singletonList("МС-П"));
        when(morphology.getMorphInfo("запрос")).thenReturn(Collections.singletonList("С"));

        Set<String> expectedLemmas = new HashSet<>();
        expectedLemmas.add("это");
        expectedLemmas.add("запрос");
        expectedLemmas.add("тестовый");

        Set<String> actualLemmas = lemmatizator.getQueryLemmas(QUERY);

        assertEquals(expectedLemmas, actualLemmas);
    }

    @Test
    void testGetWordNormalForm() {
        String expectedNormalForm = "тестовый";
        when(morphology.getNormalForms("тестовое")).thenReturn(Collections.singletonList(expectedNormalForm));

        String actualNormalForm = lemmatizator.getWordNormalForm("тестовое");

        assertEquals(expectedNormalForm, actualNormalForm);
    }
}