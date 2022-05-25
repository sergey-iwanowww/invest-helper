package ru.isg.invest.helper.dto;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * Created by s.ivanov on 26.05.2022.
 */
@Value
public class ImportCandlesResult {
    LocalDateTime lastImportedCandleDateFrom;
}
