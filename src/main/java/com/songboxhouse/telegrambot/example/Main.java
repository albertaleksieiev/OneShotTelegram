package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.TelegramLongPollingBotImpl;
import com.songboxhouse.telegrambot.anotations.DependencyProvider;
import com.songboxhouse.telegrambot.anotations.InstanceProvider;
import com.songboxhouse.telegrambot.BotCenter;
import com.songboxhouse.telegrambot.auth.ThreadLocalAuthUpdateReceiveListener;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

import static com.songboxhouse.telegrambot.example.R.LOCALE_EN;
import static com.songboxhouse.telegrambot.example.R.LOCALE_RU;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;

public class Main {
    private static final String BOT_TOKEN = null; // Retrieve it from here https://telegram.me/BotFather
    private static final String BOT_NAME = "OneShot bot";

    private static DBInMemory db = new DBInMemory();

    public static void main(String[] args) {
        if (BOT_TOKEN == null) {
            System.out.println("PLEASE PROVIDE BOT TOKEN  https://telegram.me/BotFather");
            throw new IllegalArgumentException("PLEASE PROVIDE BOT TOKEN  https://telegram.me/BotFather");
        }

        BotCenter botCenter = new BotCenter.Builder(BOT_NAME, BOT_TOKEN, HomeView.class /*Initial View*/)
                .setDependecyProvider(new MyDependencyProvider(db))
                .setUpdateReceiveListener(new ThreadLocalAuthUpdateReceiveListener()) // Apply ThreadLocalAuth for Bot active thread
                .setLocalization(new BotCenter.LocalizationProvider() {
                    @Override
                    public String getString(String locale, Integer stringId) {
                        R.string string = R.string.valueOf(stringId);
                        if (locale.equals(LOCALE_EN)) {
                            return string.en;
                        } else if (locale.equals(LOCALE_RU)) {
                            return string.rus;
                        }
                        
                        return null; // Catch issue here
                    }

                    @Override
                    public String getLocale(Update update) {
                        return db.getLocale(update);
                    }
                })
                .build();
        botCenter.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @DependencyProvider
    public static class MyDependencyProvider {
        DBInMemory dbInMemory;

        public MyDependencyProvider(DBInMemory dbInMemory) {
            this.dbInMemory = dbInMemory;
        }

        @InstanceProvider
        public DBInMemory providesDBInMemory() {
            return dbInMemory;
        }
    }


    public static class DBInMemory {
        private static String userToken;
        private static Map<Integer, String> userIdToLocale = new HashMap<>();

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            DBInMemory.userToken = userToken;
        }
        
        public String getLocale(Update update) {
            return userIdToLocale.getOrDefault(getUid(update), LOCALE_RU);
        }
        
        public void setLocale(Update update, String locale) {
            userIdToLocale.put(getUid(update), locale);
        }
    }
}
