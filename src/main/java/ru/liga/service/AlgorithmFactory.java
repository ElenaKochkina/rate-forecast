package ru.liga.service;

import ru.liga.algorithm.*;
import ru.liga.enums.AlgorithmType;
import ru.liga.exceptions.NoSuchAlgorithmException;

public class AlgorithmFactory {

    /**
     * Создает экземпляр алгоритма прогнозирования на основе переданного типа алгоритма.
     *
     * @param algorithm Тип алгоритма прогнозирования.
     * @return Экземпляр соответствующего алгоритма прогнозирования.
     * @throws NoSuchAlgorithmException если указанный тип алгоритма не существует.
     */
    public ForecastAlgorithm createAlgorithm(AlgorithmType algorithm) {
        switch (algorithm) {
            case MEAN -> {
                return new MeanAlgorithm();
            }
            case LAST_YEAR -> {
                return new LastYearAlgorithm();
            }
            case INTERNET -> {
                return new InternetAlgorithm();
            }
            case MYST -> {
                return new MysticalAlgorithm();
            }
            default -> throw new NoSuchAlgorithmException(String.format("Недопустимый тип алгоритма: %s", algorithm));
        }
    }
}
