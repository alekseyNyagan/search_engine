package main.model;

import org.hibernate.annotations.*;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "lemma")
@SQLInsert(sql = "INSERT INTO lemma (frequency, lemma, site_id) VALUES (?, ?, ?) ON CONFLICT ON CONSTRAINT lemma_site " +
        "DO UPDATE SET frequency = lemma.frequency + 1")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "id")
    private int id;

    @Column(name = "lemma", unique = true)
    @NotNull
    private String lemma;

    @Column(name = "frequency")
    @NotNull
    private int frequency;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @OneToMany(mappedBy = "lemma")
    private List<Index> index;

    @ManyToMany
    @JoinTable(name = "`index`"
            , joinColumns = @JoinColumn(name = "lemma_id")
            , inverseJoinColumns = @JoinColumn(name = "page_id"))
    private List<Page> pages;

    public Lemma() {
    }

    public Lemma(int id, String lemma, int frequency) {
        this.id = id;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public Lemma(String lemma, int frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site siteId) {
        this.site = siteId;
    }

    public List<Index> getIndex() {
        return index;
    }

    public void setIndex(List<Index> index) {
        this.index = index;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
