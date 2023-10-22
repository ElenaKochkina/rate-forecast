package ru.liga.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class Currency {
    private LocalDate rateDate;
    private BigDecimal rate;
}
