package ru.liga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;
import ru.liga.enums.CurrencyCode;
import ru.liga.parser.CsvParser;

import java.util.EnumMap;
import java.util.List;


@Log4j2
@RequiredArgsConstructor
public class CurrencyRateStorage {
    private final CsvParser csvParser;
    private final EnumMap<CurrencyCode, List<Currency>> currencyStorageMap = new EnumMap<>(CurrencyCode.class);

    /**
     * Добавляет исторические данные о курсах валют из CSV файлов в хранилище.
     */
    public void addCurrencyData() {
        for (CurrencyCode currencyCode : CurrencyCode.values()) {
            currencyStorageMap.put(currencyCode, csvParser.parseCurrencyDataFromFile(getFileName(currencyCode)));
            log.info("Добавлены исторические данные о курсах валюты {}", currencyCode);
        }
    }

    /**
     * Возвращает исторические данные о курсах валют по указанному коду.
     *
     * @param currencyCode Код валюты.
     * @return Список объектов Currency, представляющих данные о курсах валют.
     */
    public List<Currency> getCurrencyDataByCurrencyCode(CurrencyCode currencyCode) {
        log.info("Получение исторических данных о курсах валюты {}", currencyCode);
        return currencyStorageMap.get(currencyCode);
    }

    /**
     * Генерирует имя файла для указанного кода валюты.
     *
     * @param currencyCode Код валюты.
     * @return Имя файла в нижнем регистре с расширением ".csv".
     */
    private String getFileName(CurrencyCode currencyCode) {
        return currencyCode.name().toLowerCase() + ".csv";
    }
}
