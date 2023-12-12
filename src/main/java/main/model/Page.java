package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
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
    @ToString.Exclude
    private List<Index> index;

    @ManyToMany
    @JoinTable(
            name = "`index`"
            , joinColumns = @JoinColumn(name = "page_id")
            , inverseJoinColumns = @JoinColumn(name = "lemma_id")
    )
    @ToString.Exclude
    private List<Lemma> lemmas;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "site_id")
    @ToString.Exclude
    private Site site;

    public Page(String path, int code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
    }

}
