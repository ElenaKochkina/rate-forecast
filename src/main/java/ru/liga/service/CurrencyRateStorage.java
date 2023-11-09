package ru.liga.service;

import lombok.RequiredArgsConstructor;
import ru.liga.domain.Currency;
import ru.liga.enums.CurrencyCode;
import ru.liga.parser.CsvParser;

import java.util.EnumMap;
import java.util.List;

@RequiredArgsConstructor
public class CurrencyRateStorage {
    private final CsvParser csvParser;
    private final EnumMap<CurrencyCode, List<Currency>> currencyStorageMap = new EnumMap<>(CurrencyCode.class);

    public void addCurrencyData() {
        for (CurrencyCode currencyCode : CurrencyCode.values()) {
            currencyStorageMap.put(currencyCode, csvParser.parseCurrencyDataFromFile(getFileName(currencyCode)));
        }
    }

    public List<Currency> getCurrencyData(CurrencyCode currencyCode) {
        return currencyStorageMap.get(currencyCode);
    }

    private String getFileName(CurrencyCode currencyCode) {
        return currencyCode.name().toLowerCase() + ".csv";
    }
}
