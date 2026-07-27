package ru.netology.page;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;


//главная страница сервиса
public class MainPage {
    // ищем среди всех кнопок ту, у которой текст "Купить" и кликаем
    public ru.netology.page.PaymentPage selectBuyForm() {
        $(byText("Купить")).click();
        return new ru.netology.page.PaymentPage();
    }
}