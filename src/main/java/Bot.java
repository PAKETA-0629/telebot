import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Bot extends TelegramLongPollingBot {

    private static final String exitEmoji = EmojiParser.parseToUnicode(":x:");
    private static int currentUniversity = 1;
    private static String currentCity = "";
    private static Integer currentMessageId = -1;
    private static String currentMessageText = "";
    private static String currentField = "";
    private static int maxCountOfUniversity = Integer.MAX_VALUE;
    private static boolean nameFlag = false;
    private static String currentCityName = "";
    private static String currentOblast = "";

    private void changeRatingMessage(int number, String field, Update update) {

        Integer message_id = update.getCallbackQuery().getMessage().getMessageId();
        long chat_id = update.getCallbackQuery().getMessage().getChatId();
        String[] array = {"Наступні 10 ВНЗ", "Попередні 10 ВНЗ", "Вихід"};
        String result;
        EditMessageText msg;

        currentUniversity += number;
        if (currentUniversity >= maxCountOfUniversity-9) array = new String[]{"Попередні 10 ВНЗ", "Вихід"};
        else if (currentUniversity <= 1) array = new String[]{"Наступні 10 ВНЗ", "Вихід"};
        result = select(field);

        currentMessageText = result;
        msg = new EditMessageText()
                .setChatId(chat_id)
                .setMessageId(message_id)
                .setText(result);
        msg.setReplyMarkup(inline(array));
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void getMaxCountOfUniversity(String field) {
        DBWorker worker = new DBWorker();
        try {
            Statement statement = worker.getConnection().createStatement();
            ResultSet rs;
            int max = 0;
            switch (field){
                case "scopus":
                case "rating":
                    rs = statement.executeQuery("SELECT * FROM univers");
                    while (rs.next()) {
                        max++;
                    }
                    maxCountOfUniversity = max;
                    break;
                case "city":
                    rs = statement.executeQuery("SELECT * FROM univers WHERE city ='" + currentCity + "'");
                    while(rs.next()) {
                        max++;
                    }
                    maxCountOfUniversity = max;
                    break;
                case "oblast":
                    rs = statement.executeQuery("SELECT * FROM univers WHERE city ='" + currentOblast + "'");
                    while(rs.next()) {
                        max++;
                    }
                    maxCountOfUniversity = max;
                    break;
            }
            worker.getConnection().close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }


    private String select(String field) {
        currentField = field;
        StringBuilder result = new StringBuilder();
        DBWorker worker = new DBWorker();
        try {
            Statement statement = worker.getConnection().createStatement();
            ResultSet rs;
            switch (field){
                case "top200ukr":
                    result.append("Рейтинг топ-200 України\n");
                    rs = statement.executeQuery("SELECT * FROM univers ORDER BY top200ukr LIMIT 10 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("top200ukr")).append(" місце").append(": ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
                case "scopus":
                    result.append("Рейтинг за версією Scopus\n");
                    rs = statement.executeQuery("SELECT * FROM univers ORDER BY scopus LIMIT 10 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("scopus")).append(" місце").append(": ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
                case "world":
                    result.append("Світовий рейтинг\n");
                    rs = statement.executeQuery("SELECT * FROM univers ORDER BY world LIMIT 6 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("world")).append(" місце").append(": ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
                case "city":
                    result.append("Рейтинг за містами України\n");
                    rs = statement.executeQuery("SELECT * FROM univers WHERE city ='" + currentCity + "' ORDER BY rating LIMIT 10 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("rating")).append(" місце у загальному рейтингу: ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
                case "oblast":
                    result.append("Рейтинг за областями України\n");
                    rs = statement.executeQuery("SELECT * FROM univers WHERE oblast='" + currentOblast + "' ORDER BY rating LIMIT 10 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("rating")).append(" місце").append(": ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
                case "znocontract":
                    result.append("Рейтинг за вступним балом ЗНО\n");
                    rs = statement.executeQuery("SELECT * FROM univers ORDER BY znocontract LIMIT 10 OFFSET " + (currentUniversity-1));
                    while (rs.next()) {
                        result.append(rs.getString("znocontract")).append(" місце").append(": ").append(rs.getString(4))
                                .append("\n" + "Веб сайт: ").append(rs.getString("website")).append("\n\n");
                    }
                    break;
            }
            worker.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result.toString();

    }


    public String searchName(String name) {
        currentCityName = name;
        return select("name");
    }

    public void sendMsgInline(SendMessage msg, String[] array) {
        msg.setReplyMarkup(inline(array));
        try {
            execute(msg);
        } catch(TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(SendMessage msg) {
        try {
            execute(msg);
        } catch(TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhtInline(SendPhoto msg, String[] array) {
        msg.setReplyMarkup(inline(array));
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPht(SendPhoto msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public InlineKeyboardMarkup inline(String[] array) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String str: array){
            rowInline.add(new InlineKeyboardButton().setText(str).setCallbackData(str));
            rowsInline.add(rowInline);
            rowInline = new ArrayList<>();
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private void exit(Integer message_id, long chat_id, String text) {
        EditMessageText msg = new EditMessageText()
                .setChatId(chat_id)
                .setMessageId(message_id)
                .setText(text);
        currentUniversity = 1;
        maxCountOfUniversity = Integer.MAX_VALUE;
        currentCityName = "";
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void markupOblast(Long chatId) {
        SendMessage newMessage = new SendMessage()
                .setChatId(chatId)
                .setText("Оберіть область.");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        /*
                    case "Харків":
                    case "Одеса":
                    case "Львів":
                    case "Маріуполь":
                    case "Дніпро":
                    case "Запоріжжя":
                    case "Вінниця":
                    case "Івано-Франківськ":
                    case "Тернопіль":
                    case "Полтава":
                    case "Хмельницький":
                    case "Херсон":
                    case "Кривий Ріг":
                    case "Миколаїв":*/
        row.add("Київська область");
        row.add("Донецька область");
        row.add("Львівська область");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Харківська область");
        row.add("Одеська область");
        row.add("Дніпропетровська область");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Запорізька область");
        row.add("Вінницька область");
        row.add("Івано-Франківська область");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Полтавська область");
        row.add("Тернопільська область");
        row.add("Хмельницька область");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add(exitEmoji);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void markupCity(Long chatId) {
        SendMessage newMessage = new SendMessage()
                .setChatId(chatId)
                .setText("Оберіть місто.");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Харків");
        row.add("Одеса");
        row.add("Львів");
        row.add("Маріуполь");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Київ");
        row.add("Дніпро");
        row.add("Запоріжжя");
        row.add("Житомир");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Вінниця");
        row.add("Івано-Франківськ");
        row.add("Тернопіль");
        row.add("Полтава");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Хмельницький");
        row.add("Херсон");
        row.add("Кривий Ріг");
        row.add(exitEmoji);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void info(Long chatId){
        SendMessage newMessage = new SendMessage()
                .setChatId(chatId)
                .setText("«ТОП-200 Україна» — базовим принципом ранжування університетів у рейтингу є забезпечення повної відкритості, " +
                        "прозорості і незалежності ранжування університетів. Для укладання рейтингу використовувалися відкриті дані прямих вимірів, " +
                        "виставлені на відкритих веб-ресурсах незалежних національних та міжнародних організацій і установ.\n\n" +
                        "Рейтинг «Scopus» — результати рейтингу закладів вищої освіти базуються на показниках бази даних Scopus, що є інструментом " +
                        "для відстеження цитованості наукових статей, які публікуються навчальним закладом або його працівниками у наукових виданнях. " +
                        "У рейтинговій таблиці заклади освіти України ранжовані за індексом Гірша - кількісним показником, що базується на кількості " +
                        "наукових публікацій і кількості цитувань цих публікацій.\n\n" +
                        "Рейтинг «Бал ЗНО на контракт» — рейтинг закладів вищої освіти за показником середнього бала ЗНО абітурієнтів минулого року, " +
                        "яких зараховано до вишів на навчання за кошти фізичних та юридичних осіб (контракт).\n\n" +
                        "Світовий рейтинг - QS World University Rankings вважається одним із найавторитетніших глобальних рейтингів вищих навчальних закладів. " +
                        "При його укладанні автори керуються низкою критеріїв, таких як академічна репутація закладу, індекс цитування статей його представників, " +
                        "відсоток іноземних студентів, репутація ВНЗ серед роботодавців тощо.\n\n" +
                        "Рейтинг за містами та областями України - результати базуються на консолідованому рейтингу. У консолідованому рейтингу кожному закладу " +
                        "освіти присвоєно бал, що дорівнює сумі його місць у рейтингах \"ТОП-200 Україна\", \"Scopus\" і \"Бал ЗНО на контракт\".");
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void rating(Long chatId) {
        SendMessage newMessage = new SendMessage()
                .setChatId(chatId)
                .setText("Оберіть спосіб порівняння Університетів.");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Рейтинг топ-200 України");
        row.add("Рейтинг за версією Scopus");
        row.add("Світовий рейтинг");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Рейтинг за містами України");
        row.add("Рейтинг за областями України");
        row.add("Рейтинг за балом ЗНО");
        keyboard.add(row);
        row = new KeyboardRow();
        row.add("Закрити клавіатуру");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);

        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void hideKeyBoard(Long chatId) {
        SendMessage msg = new SendMessage()
                .setChatId(chatId)
                .setText("Клавіатура схована");
        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
        msg.setReplyMarkup(keyboardMarkup);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void menu(Long chatId) {
        sendPht(new SendPhoto()
                .setChatId(chatId)
                .setPhoto("AgACAgIAAxkBAAPBX1eWcM43tTnUaeIKTOdlKXBt-7EAAmquMRt0lbhKOqOijfj1L-P9jG6XLgADAQADAgADeAADaXoAAhsE")
                .setCaption("Команда /rating - отримання рейтингу\n" +
                        "Команда /hide - заховати клавіатуру\n" +
                        "Команда /menu - відкрити меню"));
    }



    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && update.getMessage().getText().equals(exitEmoji)) {
            rating(message.getChatId());
        } else if (nameFlag && update.hasMessage() && update.getMessage().hasText()) {
            String result = searchName(message.getText());
            sendMsg(new SendMessage()
                            .setChatId(message.getChatId())
                            .setText(result)
                            .enableMarkdown(true));
            nameFlag = false;
        }
        else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = message.getChatId();
            if (message.getText().contains("область")) {
                String[] array = {"Наступні 10 ВНЗ", "Вихід"};
                exit(currentMessageId, chatId, currentMessageText);
                currentUniversity = 1;
                currentOblast = message.getText();

                String result = select("oblast");
                getMaxCountOfUniversity("oblast");
                if (maxCountOfUniversity < 11) array = new String[]{"Вихід"};
                sendMsgInline(new SendMessage()
                                .setChatId(chatId)
                                .setText(result)
                                .enableMarkdown(true),
                        array);
            } else {

                switch (message.getText()) {
                    case "/menu":
                    case "/start":
                        sendPhtInline(new SendPhoto()
                                        .setChatId(chatId)
                                        .setPhoto("AgACAgIAAxkBAAICOl9lDsG9pbmlx4AlXDTO4c2kyGoxAAKcrzEbrOcpSxKgelx2RoXGNKJpli4AAwEAAwIAA3gAAyecAQABGwQ")
                                        .setCaption("Привіт, цей телеграм бот розкаже " +
                                                "тобі про найкращі університети України та допоможе обрати один з них!\n\n" +
                                                "Команда /rating щоб Ознайомитись з рейтингами!\n" +
                                                "Команда /info щоб ознайомитись з наявними рейтингами та критеріями оцінювання.\n\n" +
                                                "Цей бот створенний для забезпечення повної відкритості, прозорості і незалежності ранжування університетів. " +
                                                "Для цього використовувалися лише відкриті дані прямих вимірів, виставлені на відкритих веб-ресурсах незалежних " +
                                                "національних та міжнародних організацій і установ. Будь які дані, або експертні оцінки самих університетів та " +
                                                "органів управління ними не використовувалися. Методика розрахунків університетських рейтингів є доступною для " +
                                                "громадськості з метою перевірки отриманих результатів.\n"),
                                new String[]{"Ознайомитись з рейтингами", "Інформація про рейтинги"});

                        break;
                    case "/rating":
                        currentUniversity = 1;
                        rating(chatId);
                        break;
                    case ":x:":
                        rating(chatId);
                        break;
                    case "Закрити клавіатуру":
                    case "/hide":
                        hideKeyBoard(chatId);
                        break;
                    case "/info":
                        info(chatId);
                        break;
                    case "Рейтинг топ-200 України":
                        exit(currentMessageId, chatId, currentMessageText);
                        currentUniversity = 1;
                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        String result = select("top200ukr");
                        getMaxCountOfUniversity("top200ukr");

                        sendMsgInline(new SendMessage()
                                        .setChatId(chatId)
                                        .setText(result)
                                        .enableMarkdown(true),
                                new String[]{"Наступні 10 ВНЗ", "Вихід"});
                        break;
                    case "Рейтинг за версією Scopus":
                        exit(currentMessageId, chatId, currentMessageText);
                        currentUniversity = 1;
                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        result = select("scopus");
                        getMaxCountOfUniversity("scopus");
                        sendMsgInline(new SendMessage()
                                        .setChatId(chatId)
                                        .setText(result)
                                        .enableMarkdown(true),
                                new String[]{"Наступні 10 ВНЗ", "Вихід"});
                        break;
                    case "Світовий рейтинг":
                        result = select("world");
                        sendMsg(new SendMessage()
                                .setChatId(chatId)
                                .setText(result)
                                .enableMarkdown(true));
                        break;
                    case "Рейтинг за містами України":
                        exit(currentMessageId, chatId, currentMessageText);

                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        markupCity(chatId);
                        break;
                    case "Рейтинг за балом ЗНО":
                        exit(currentMessageId, chatId, currentMessageText);
                        currentUniversity = 1;
                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        result = select("znocontract");
                        getMaxCountOfUniversity("znocontract");
                        sendMsgInline(new SendMessage()
                                        .setChatId(chatId)
                                        .setText(result)
                                        .enableMarkdown(true),
                                new String[]{"Наступні 10 ВНЗ", "Вихід"});
                        break;
                    case "Рейтинг за областями України":
                        exit(currentMessageId, chatId, currentMessageText);
                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        markupOblast(chatId);
                        break;

                    case "Київ":
                    case "Харків":
                    case "Одеса":
                    case "Львів":
                    case "Маріуполь":
                    case "Дніпро":
                    case "Запоріжжя":
                    case "Вінниця":
                    case "Івано-Франківськ":
                    case "Тернопіль":
                    case "Полтава":
                    case "Хмельницький":
                    case "Херсон":
                    case "Кривий Ріг":
                    case "Миколаїв":
                        String[] array = {"Наступні 10 ВНЗ", "Вихід"};
                        exit(currentMessageId, chatId, currentMessageText);
                        currentUniversity = 1;
                        currentMessageId = Math.toIntExact(message.getMessageId());
                        currentMessageText = message.getText();
                        currentCity = message.getText();

                        result = select("city");
                        getMaxCountOfUniversity("city");
                        if (maxCountOfUniversity < 11) array = new String[]{"Вихід"};
                        sendMsgInline(new SendMessage()
                                        .setChatId(chatId)
                                        .setText(result)
                                        .enableMarkdown(true),
                                array);
                        break;
                    default:
                        sendMsg(new SendMessage().setChatId(message.getChatId()).setText("Невідома команда, введіть /menu"));
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {

            long chat_id = update.getMessage().getChatId();
            List<PhotoSize> photos = update.getMessage().getPhoto();
            String f_id = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getFileId();
            int f_width = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getWidth();
            int f_height = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getHeight();
            String caption = "file_id: " + f_id + "\nwidth: " + f_width + "\nheight: " + (f_height);
            SendPhoto msg = new SendPhoto()
                    .setChatId(chat_id)
                    .setPhoto(f_id)
                    .setCaption(caption);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            Integer message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            switch (call_data) {
                case "Вихід":
                    exit(message_id, chat_id, update.getCallbackQuery().getMessage().getText());
                    break;
                case "Ознайомитись з рейтингами":
                    rating(chat_id);
                    break;
                case "Наступні 10 ВНЗ":
                    changeRatingMessage(10, currentField, update);
                    break;
                case "Інформація про рейтинги":
                    info(chat_id);
                    break;
                case "Попередні 10 ВНЗ":
                    changeRatingMessage(-10, currentField, update);
                    break;
//                case "Наступні 10 ВНЗ":
//                    currentUniversity+=10;
//                    if (currentUniversity > 241-10) array = new String[]{"Попередні 10 ВНЗ", "Вихід"};
//                    select(chat_id, array, currentUniversity);
//                    break;
//                case "Попередні 10 ВНЗ":
//                    currentUniversity-=10;
//                    if (currentUniversity < 10) array = new String[]{"Наступні 10 ВНЗ", "Вихід"};
//                    select(chat_id, array, currentUniversity);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "Kyrylo0629_bot";
    }

    @Override
    public String getBotToken() {
        return "1286877301:AAGkbM8iCKjP_QPA9oRjwSnm8eCdnO5p780";
    }
}