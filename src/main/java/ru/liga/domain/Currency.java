package ru.liga.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class Currency {
    private final LocalDate rateDate;
    private final BigDecimal rate;
}
