package ru.isg.invest.helper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Table(name = "authors")
@Getter
@Accessors(chain = true)
@ToString
@NoArgsConstructor(access = PROTECTED)
public class Author {

    public Author(String name) {
        this.name = name;
    }

    @Id
    private UUID id = UUID.randomUUID();

    @Setter
    @Column(nullable = false)
    private String name;
}
