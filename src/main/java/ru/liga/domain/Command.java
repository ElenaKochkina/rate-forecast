package ru.liga.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.liga.enums.AlgorithmType;
import ru.liga.enums.CurrencyCode;
import ru.liga.enums.OutputType;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Command {
    private List<CurrencyCode> currencyCodes;
    private LocalDate startDate;
    private LocalDate endDate;
    private AlgorithmType algorithmType;
    private OutputType outputType;
}
