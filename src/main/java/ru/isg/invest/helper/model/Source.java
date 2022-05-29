package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Table(name = "sources")
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
public class Source {

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    @ManyToMany
    @JoinTable(name = "source_author_links",
            joinColumns = {@JoinColumn(name = "source_id")},
            inverseJoinColumns = {@JoinColumn(name = "author_id")})
    private List<Author> authors = newArrayList();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceTypes type;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private String address;

    public Author addAuthor(String name) {
        Author author = new Author(name);
        authors.add(author);
        return author;
    }
}
