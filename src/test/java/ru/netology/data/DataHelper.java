package ru.netology.data;


import com.github.javafaker.Faker;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

//DataHelper - это генерация и хранение всех тестовых данных
public class DataHelper {
    private static final Faker FAKER = new Faker(new Locale("en"));

    private DataHelper() {
    }

    // APPROVED карта
    public static CardInfo getApprovedCard() {
        return new CardInfo("1111 2222 3333 4444", getValidMonth(), getValidYear(1), getValidOwner(), getValidCvc());
    }

    // DECLINED карта
    public static CardInfo getDeclinedCard() {
        return new CardInfo("5555 6666 7777 8888", getValidMonth(), getValidYear(1), getValidOwner(), getValidCvc());
    }

    public static String getValidMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));
    }

    public static String getValidYear(int shiftYears) {
        return LocalDate.now().plusYears(shiftYears).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getPastYear() {
        return LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yy"));
    }

    public static String getValidOwner() {
        return FAKER.name().firstName().toUpperCase() + " " + FAKER.name().lastName().toUpperCase();
    }

    public static String getCyrillicOwner() {
        return "ИВАН ПЕТРОВ";
    }

    public static String getInvalidSymbolsOwner() {
        return "JOHN321!";
    }

    public static String getInvalidShortOwner() {
        return "J";
    }

    public static String getValidCvc() {
        return FAKER.number().digits(3);
    }

    public static String getShortCvc() {
        return FAKER.number().digits(2);
    }

    public static String getThirteenMonth() {
        return "13";
    }

    public static String getZeroMonth() {
        return "00";
    }

    @Value
    public static class CardInfo {
        String cardNumber;
        String month;
        String year;
        String owner;
        String cvc;
    }
}
