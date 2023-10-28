package ru.liga.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Command {
    private CurrencyCode currencyCode;
    private ForecastType forecastType;
}
