package ru.netology.page;


import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

//страница ввода данных для оплаты
public class PaymentPage {
    // элементы формы ввода
    private final SelenideElement cardNumberField = $$(".input").findBy(text("Номер карты")).$("input");
    private final SelenideElement monthField = $$(".input").findBy(text("Месяц")).$("input");
    private final SelenideElement yearField = $$(".input").findBy(text("Год")).$("input");
    private final SelenideElement ownerField = $$(".input").findBy(text("Владелец")).$("input");
    private final SelenideElement cvcField = $$(".input").findBy(text("CVC/CVV")).$("input");
    private final SelenideElement continueButton = $$("button").findBy(text("Продолжить"));

    // уведомления от банковской системы
    private final SelenideElement successNotification = $(".notification_status_ok");
    private final SelenideElement errorNotification = $(".notification_status_error");

    public PaymentPage() {
        $$("h3").findBy(text("Оплата по карте")).shouldBe(visible, Duration.ofSeconds(5));
    }

    // заполнение полей формы и отправка данных
    public void fillForm(DataHelper.CardInfo card) {
        cardNumberField.setValue(card.getCardNumber());
        monthField.setValue(card.getMonth());
        yearField.setValue(card.getYear());
        ownerField.setValue(card.getOwner());
        cvcField.setValue(card.getCvc());
        continueButton.click();
    }

    // ожидание успешного всплывающего окна
    public void waitSuccessNotification() {
        successNotification.shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Успешно"), text("Операция одобрена Банком."));
    }

    // ожидание всплывающего окна об ошибке/отказе
    public void waitErrorNotification() {
        errorNotification.shouldBe(visible, Duration.ofSeconds(15))
                .shouldHave(text("Ошибка"));
    }

    // проверка текста ошибки валидации под конкретным полем
    public void checkFieldError(String fieldName, String expectedText) {
        $$(".input__sub")
                .findBy(text(expectedText))
                .shouldBe(visible, Duration.ofSeconds(4));

    }
}
