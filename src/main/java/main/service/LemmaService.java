package main.service;

import lombok.RequiredArgsConstructor;
import main.model.Site;
import main.opennlp.OpenNLPLemmatizer;
import main.repository.HtmlPageRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LemmaService {
    private final OpenNLPLemmatizer openNLPLemmatizer;
    private final HtmlPageRepository htmlPageRepository;

    public long recalculate(Site site) {
        Set<String> lemmas = new HashSet<>();
        htmlPageRepository.findBySiteName(site.getName())
                .forEach(hit -> lemmas.addAll(
                        Arrays.asList(openNLPLemmatizer.getLemmas(
                                Jsoup.clean(hit.getContent().getContent(), Safelist.none())))));
        site.setLemmasCount((long) lemmas.size());
        return lemmas.size();
    }
}
