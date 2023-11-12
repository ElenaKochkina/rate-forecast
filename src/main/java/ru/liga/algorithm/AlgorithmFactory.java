package ru.liga.algorithm;

import ru.liga.enums.AlgorithmType;
import ru.liga.exceptions.NoSuchAlgorithmException;

public class AlgorithmFactory {

    MeanAlgorithm meanAlgorithm = new MeanAlgorithm();
    LastYearAlgorithm lastYearAlgorithm = new LastYearAlgorithm();
    InternetAlgorithm internetAlgorithm = new InternetAlgorithm();
    MysticalAlgorithm mysticalAlgorithm = new MysticalAlgorithm();

    /**
     * Создает экземпляр алгоритма прогнозирования на основе переданного типа алгоритма.
     *
     * @param algorithm Тип алгоритма прогнозирования.
     * @return Экземпляр соответствующего алгоритма прогнозирования.
     * @throws NoSuchAlgorithmException если указанный тип алгоритма не существует.
     */
    public ForecastAlgorithm createAlgorithm(AlgorithmType algorithm) {
        return switch (algorithm) {
            case MEAN -> meanAlgorithm;
            case LAST_YEAR -> lastYearAlgorithm;
            case INTERNET -> internetAlgorithm;
            case MYST -> mysticalAlgorithm;
        };
    }
}
