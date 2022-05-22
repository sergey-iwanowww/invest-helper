package ru.isg.invest.helper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;

import static lombok.AccessLevel.PROTECTED;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Entity
@Accessors(chain = true)
@Getter
@ToString
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@DiscriminatorValue("DATE")
public class DateIdeaTrigger extends IdeaTrigger {

    @Column(nullable = false)
    private LocalDate date;
}
