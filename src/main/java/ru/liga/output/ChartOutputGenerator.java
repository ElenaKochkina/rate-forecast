package ru.liga.output;

import lombok.RequiredArgsConstructor;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class ChartOutputGenerator {
    private static final String CHART_NAME = "График курсов валют";
    private static final String X_AXIS_LABEL = "День";
    private static final String Y_AXIS_LABEL = "Курс";
    private static final int CHART_WIDTH = 800;
    private static final int CHART_HEIGHT = 600;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");

    private final DefaultCategoryDataset dataset;

    /**
     * Создает график курсов валют на основе предоставленных данных.
     *
     * @param currencyData Данные о курсах валют по дням.
     * @return Массив байтов, представляющий собой изображение графика.
     */
    public byte[] createChart(Map<String, List<Currency>> currencyData) {
        log.info("Создание графика прогнозируемых курсов валют");

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

        log.info("График прогнозируемых курсов валют сгенерирован");

        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(stream, lineChart, CHART_WIDTH, CHART_HEIGHT);
            return stream.toByteArray();
        } catch (IOException e) {
            log.error("Ошибка при сохранении графика: {}", e.getMessage());
            return new byte[0];
        }
    }
}