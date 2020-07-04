package com.songboxhouse.telegrambot.example;

public class R {
    public static final String LOCALE_RU = "ru";
    public static final String LOCALE_EN = "en";
    
    public enum string {
        LocalizationViewTitle("Localization Example", "Пример локализации"),
        LocalizationViewDescription("Here you see localization, press button below to change language", "Здесь вы видите локализацию, поменяйте язык ниже.");

        public final String en;
        public final String rus;

        private string(String en, String rus) {
            this.en = en;
            this.rus = rus;
        }
        public static string valueOf(int ordinal) {
            return string.values()[ordinal];
        }
    }
}
