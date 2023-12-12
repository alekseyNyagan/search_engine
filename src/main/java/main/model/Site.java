package main.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "site")
public class Site extends AbstractEntity {

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Column(name = "status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Status status;

    @Column(name = "status_time")
    @NotNull
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url")
    @NotNull
    private String url;

    @Column(name = "name")
    @NotNull
    private String name;

    @OneToMany(mappedBy = "site")
    @ToString.Exclude
    private List<Page> page;

    @OneToMany(mappedBy = "site")
    @ToString.Exclude
    private List<Lemma> lemma;

    public Site(Status status, LocalDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

}
