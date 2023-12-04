package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "page")
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "id")
    private int id;

    @Column(name = "path")
    @NotNull
    private String path;

    @Column(name = "code")
    @NotNull
    private int code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    @NotNull
    private String content;

    @OneToMany(mappedBy = "page")
    private List<Index> index;

    @ManyToMany
    @JoinTable(
            name = "`index`"
            , joinColumns = @JoinColumn(name = "page_id")
            , inverseJoinColumns = @JoinColumn(name = "lemma_id")
    )
    private List<Lemma> lemmas;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "site_id")
    private Site site;

    public Page() {
    }

    public Page(String path, int code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(List<Lemma> lemmas) {
        this.lemmas = lemmas;
    }

    public List<Index> getIndex() {
        return index;
    }

    public void setIndex(List<Index> index) {
        this.index = index;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
