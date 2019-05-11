package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.BotCenter;
import com.songboxhouse.telegrambot.DependecyProvider;

public class Main {
    private static final String BOT_TOKEN = null; // Retrieve it from here https://telegram.me/BotFather
    private static final String BOT_NAME = "OneShot bot";

    private static DBInMemory db = new DBInMemory();

    public static void main(String[] args) {
        if (BOT_TOKEN == null) {
            System.out.println("PLEASE PROVIDE BOT TOKEN  https://telegram.me/BotFather");
            throw new IllegalArgumentException();
        }

        BotCenter botCenter = new BotCenter(BOT_NAME,
                BOT_TOKEN,
                new MyDependencyProvider(db),
                HomeView.class /*Initial View*/);
        botCenter.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static class MyDependencyProvider extends DependecyProvider {
        DBInMemory dbInMemory;

        public MyDependencyProvider(DBInMemory dbInMemory) {
            this.dbInMemory = dbInMemory;
        }

        public DBInMemory provideDBInMemory() {
            return dbInMemory;
        }
    }


    public static class DBInMemory {
        private static String userToken;

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            DBInMemory.userToken = userToken;
        }
    }
}
