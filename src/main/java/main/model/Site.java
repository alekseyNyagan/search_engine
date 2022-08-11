package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "site")
public class Site extends AbstractEntity {

    @Enumerated(value = EnumType.STRING)
    @NotNull
    @Column(name = "status")
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
    private List<Page> page;

    @OneToMany(mappedBy = "site")
    private List<Lemma> lemma;

    public Site() {
    }

    public Site(Status status, LocalDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(LocalDateTime statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Page> getPage() {
        return page;
    }

    public void setPage(List<Page> pages) {
        this.page = pages;
    }

    public List<Lemma> getLemma() {
        return lemma;
    }

    public void setLemma(List<Lemma> lemmas) {
        this.lemma = lemmas;
    }
}
