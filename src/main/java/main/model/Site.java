package main.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(name = "html_pages_count")
    private Long htmlPagesCount;

    @Column(name = "lemmas_count")
    private Long lemmasCount;
}