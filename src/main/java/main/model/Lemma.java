package main.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
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

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "site_id", nullable = false)
    @ToString.Exclude
    private Site site;

    @OneToMany(mappedBy = "lemma")
    @ToString.Exclude
    private List<Index> index;

    @ManyToMany
    @JoinTable(name = "`index`"
            , joinColumns = @JoinColumn(name = "lemma_id")
            , inverseJoinColumns = @JoinColumn(name = "page_id"))
    @ToString.Exclude
    private List<Page> pages;

    public Lemma(String lemma, int frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

}
