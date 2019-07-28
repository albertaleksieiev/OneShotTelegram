package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.anotations.DependencyProvider;
import com.songboxhouse.telegrambot.anotations.InstanceProvider;
import com.songboxhouse.telegrambot.BotCenter;

public class Main {
    private static final String BOT_TOKEN = "803965316:AAEBx-TRlNA-eJkEzbedcjtqdSlBCIdYHKk"; // Retrieve it from here https://telegram.me/BotFather
    private static final String BOT_NAME = "OneShot bot";

    private static DBInMemory db = new DBInMemory();

    public static void main(String[] args) {
        if (BOT_TOKEN == null) {
            System.out.println("PLEASE PROVIDE BOT TOKEN  https://telegram.me/BotFather");
            throw new IllegalArgumentException();
        }

        BotCenter botCenter = new BotCenter.Builder(BOT_NAME, BOT_TOKEN, HomeView.class /*Initial View*/)
                .setDependecyProvider(new MyDependencyProvider(db))
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

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            DBInMemory.userToken = userToken;
        }
    }
}
