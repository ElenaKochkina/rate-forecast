package ru.liga.output;

import lombok.extern.log4j.Log4j2;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import ru.liga.domain.Currency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Log4j2
public class ChartOutputGenerator {
    private static final String CHART_NAME = "График курсов валют";
    private static final String X_AXIS_LABEL = "День";
    private static final String Y_AXIS_LABEL = "Курс";
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;

    private static final String FILE_NAME = "currencyExchangeRateChart.png";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");

    /**
     * Создает график курсов валют на основе предоставленных данных.
     *
     * @param currencyData Данные о курсах валют по дням.
     * @return Массив байтов, представляющий собой изображение графика.
     */
    public byte[] createChart(Map<String, List<Currency>> currencyData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        log.debug("Создание графика курсов валют");

        for (Map.Entry<String, List<Currency>> entry : currencyData.entrySet()) {
            String currencyCode = entry.getKey();
            List<Currency> currencyList = entry.getValue();

            for (Currency currency : currencyList) {
                String formattedDate = currency.getRateDate().format(DATE_FORMATTER);
                dataset.addValue(currency.getRate(), currencyCode, formattedDate);
                log.debug("Добавлены данные в график: Валюта={}, Дата={}, Курс={}",
                        currencyCode, formattedDate, currency.getRate());
            }
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                CHART_NAME,
                X_AXIS_LABEL,
                Y_AXIS_LABEL,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) lineChart.getPlot();
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        log.debug("График сгенерирован");

        try {
            File chartFile = new File(FILE_NAME);
            ChartUtils.saveChartAsPNG(chartFile, lineChart, CHART_WIDTH, CHART_HEIGHT);
            log.info("График сохранен в " + FILE_NAME);
            return Files.readAllBytes(chartFile.toPath());
        } catch (IOException e) {
            log.error("Ошибка при сохранении графика: " + e.getMessage());
            return new byte[0];
        }
    }
}