package ru.liga.telegram;

public class Constants {

    public static final String START_COMMAND = "/start";
    public static final String HELP_COMMAND = "/help";
    public static final String EXIT_COMMAND = "/exit";
    public static final String RATE_COMMAND = "/rate";

    public static final String FINISH_BUTTON = "Завершить";

    public static final String START_MESSAGE = """
            Добро пожаловать!
            Этот бот предоставляет прогнозы курсов валют на основе различных параметров и алгоритмов.
            Чтобы начать, отправьте одну из вышеперечисленных команд:
            /help - для получения справки и инструкций по использованию бота
            /rate - для получения прогноза курсов валют
            /exit - для завершения общения с ботом
            """;

    public static final String HELP_MESSAGE = """
            Справка по использованию бота:

            /start - начать общение с ботом и получить краткое приветствие
            /rate - получить прогноз курсов валют
            /exit - завершить общение с ботом

            Для получения прогноза курсов валют, отправьте команду /rate и следуйте инструкциям.
            """;

    public static final String EXIT_MESSAGE = "Диалог с ботом завершен.\n";

    public static final String RATE_MESSAGE = """
            Чтобы получить прогноз курсов валют, выберите коды валют, которые вас интересуют.
            Вы можете выбрать одну или несколько валют с помощью клавиатуры.
            После завершения выбора, нажмите 'Завершить'
            """;

    public static final String NO_SELECTED_CURRENCY_ERROR_MESSAGE = "Выберите хотя бы одну валюту перед завершением выбора.";

    public static final String NO_SUCH_CURRENCY_ERROR_MESSAGE = "Неверный код валюты. Выберите коды валют из списка и нажмите 'Завершить'.";

    public static final String CURRENCY_CONFIRM_MESSAGE = "Выбранные коды валют: ";

    public static final String SELECT_ALGORITHM_MESSAGE = """
            Выберите алгоритм прогнозирования:
            *MEAN* - cреднее значение курса валюты на основании 7 предыдущих курсов.
            *LAST_YEAR* - курс валюты на основе данных прошлого года.
            *INTERNET* - прогноз на основе алгоритма линейной регрессии.
            *MYST* - таинственный алгоритм.
            """;


    public static final String NO_SUCH_ALGORITHM_ERROR_MESSAGE = "Неверный тип алгоритма. Выберите алгоритм прогнозирования из списка.";

    public static final String SELECT_DATE_OR_PERIOD_MESSAGE = "Вы хотите получить прогноз валюты на период или определенную дату? Выберите один из вариантов:";

    public static final String DATE_OR_PERIOD_ERROR_MESSAGE = "Неверный тип прогноза. Выберите тип прогноза из предложенных вариантов.";

    public static final String SELECT_DATE_MESSAGE = "Введите будущую дату в формате ДД.ММ.ГГГГ (например, 01.01.2025) или 'tomorrow' для прогноза на завтрашний день.\n";

    public static final String SELECT_DATE_ERROR_MESSAGE = "Неверно указана дата. Введите корректную будущую дату в формате ДД.ММ.ГГГГ (например, 01.01.2025) или 'tomorrow' для прогноза на завтра.\n";

    public static final String SELECT_PERIOD_MESSAGE = "Выберите период прогнозирования:";

    public static final String NO_SUCH_PERIOD_ERROR_MESSAGE = "Неверный период прогноза. Выберите период из списка.";

    public static final String SELECT_OUTPUT_TYPE_MESSAGE = "Выберите тип вывода прогноза:";

    public static final String NO_SUCH_OUTPUT_TYPE_ERROR_MESSAGE = "Неверный тип вывода прогноза. Выберите тип вывода прогноза из списка.";

    public static final String FINISH_MESSAGE = "Готово! Ваш прогноз курса валют успешно сформирован. Для получения нового прогноза используйте команду /rate.";

    public static final String NO_PREDICTION_DATA_ERROR_MESSAGE = """
            Недостаточно данных для прогноза по заданному алгоритму на выбранный период.
            Для получения нового прогноза используйте команду /rate.
            """;
    private Constants() {
    }
}
