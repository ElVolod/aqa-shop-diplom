package ru.netology.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import ru.netology.data.DataHelper;
import ru.netology.data.DbHelper;
import ru.netology.page.MainPage;
import ru.netology.page.PaymentPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DbHelper.getPaymentRecordsCount;

public class PaymentTest {
    private PaymentPage paymentPage;

    @BeforeAll
    static void setUpAll() { SelenideLogger.addListener("allure", new AllureSelenide()); }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setUp() {
        DbHelper.cleanDatabase(); //очищаем БД перед каждым тестом
        MainPage mainPage = open("http://localhost:8085", MainPage.class); // открываем на нужном порте
        paymentPage = mainPage.selectBuyForm();
    }

    //Успешная покупка тура по по APPROVED карте
    @Test
    @DisplayName("1.Should successfully purchase tour with APPROVED card")
    void shouldSuccessWithApprovedCard() {
        var cardInfo = DataHelper.getApprovedCard();
        paymentPage.fillForm(cardInfo);
        paymentPage.waitSuccessNotification();

        // Проверяем, что в СУБД статус транзакции равен APPROVED
        assertEquals("APPROVED", DbHelper.getPaymentStatus());
    }

    //Покупка тура по APPROVED карте текущего срока действия
    @Test
    @DisplayName("2.Should successfully purchase with card current date")
    void shouldSuccessWithCurrentDate() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                DataHelper.getValidMonth(),
                DataHelper.getValidYear(0),
                baseCard.getOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.waitSuccessNotification();

        assertEquals("APPROVED", DbHelper.getPaymentStatus());
    }

    // Отказ в оплате по карте DECLINED
    @Test
    @DisplayName("3. Should decline purchase with DECLINED card")
    void shouldDeclineWithDeclinedCard() {
        var cardInfo = DataHelper.getDeclinedCard();
        paymentPage.fillForm(cardInfo);
        paymentPage.waitErrorNotification();

        // Проверяем, что в СУБД статус транзакции равен DECLINED
        assertEquals("DECLINED", DbHelper.getPaymentStatus());
    }

    // Оплата картой с истекшим сроком действия (прошлый год)
    @Test
    @DisplayName("4. Should show validation error for card with expired year (past year)")
    void shouldShowErrorIfExpiredYear() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                DataHelper.getPastYear(),
                baseCard.getOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Год", "Истёк срок действия карты");

        // Проверяем, что запрос заблокирован на UI и в базу данных ничего не записалось
        assertEquals(0, getPaymentRecordsCount());
    }

    // Ввод невалидного формата CVC/CVV (2 цифры)
    @Test
    @DisplayName("5. Should show validation error for invalid CVC format (2 digits)")
    void shouldShowErrorIfInvalidCvc() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                baseCard.getYear(),
                baseCard.getOwner(),
                DataHelper.getShortCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("CVC/CVV", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }

    // Пустая форма оплаты
    @Test
    @DisplayName("6. Should show validation error for empty form")
    void shouldShowErrorWithEmptyForm() {
        var cardInfo = new DataHelper.CardInfo("", "", "", "", "");
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Номер карты", "Неверный формат");
        paymentPage.checkFieldError("Месяц", "Неверный формат");
        paymentPage.checkFieldError("Год", "Неверный формат");
        paymentPage.checkFieldError("Владелец", "Поле обязательно для заполнения");
        paymentPage.checkFieldError("CVC/CVV", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }

    // Ввод несуществующего месяца (13 месяц)
    @Test
    @DisplayName("7. Should show validation error for non-existent month (month 13)")
    void shouldShowErrorWithMonth13() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                DataHelper.getThirteenMonth(),
                baseCard.getYear(),
                baseCard.getOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Месяц", "Неверно указан срок действия карты");

        assertEquals(0, getPaymentRecordsCount());
    }

    //Ввод граничного невалидного месяца (00 месяц)
    @Test
    @DisplayName("8. Should show error with zero month value")
    void shouldShowErrorWithZeroMonth() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                DataHelper.getZeroMonth(),
                baseCard.getYear(),
                baseCard.getOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Месяц", "Неверно указан срок действия карты");

        assertEquals(0, getPaymentRecordsCount());
    }

    // Ввод имени владельца на кириллице
    @Test
    @DisplayName("9. Should show validation error when owner name is entered in Cyrillic")
    void shouldShowErrorWithCyrillicOwner() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                baseCard.getYear(),
                DataHelper.getCyrillicOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Владелец", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }

    //Ввод слишком короткого имени владельца (одна буква)
    @Test
    @DisplayName("10. Should show validation error for too short owner name (one letter)")
    void shouldShowErrorWithOneLetterOwner() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                baseCard.getYear(),
                DataHelper.getInvalidShortOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Владелец", "Имя должно содержать не менее 2 символов");

        assertEquals(0, getPaymentRecordsCount());
    }

    //Ввод спецсимволов и цифр в поле 'Владелец'
    @Test
    @DisplayName("11. Should show validation error when owner name contains special symbols or digits")
    void shouldShowErrorWithSpecialSymbolsOwner() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                baseCard.getYear(),
                DataHelper.getInvalidSymbolsOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Владелец", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }

    //Отправка формы с пустым полем "Номер карты"
    @Test
    @DisplayName("12. Should show validation error for empty card number field")
    void shouldShowErrorIfCardNumberIsEmpty() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                "",
                baseCard.getMonth(),
                baseCard.getYear(),
                baseCard.getOwner(),
                baseCard.getCvc()
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("Номер карты", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }

    @Test
    @DisplayName("13. Should show validation error for empty CVC field")
    void shouldShowErrorIfCvcIsEmpty() {
        var baseCard = DataHelper.getApprovedCard();
        var cardInfo = new DataHelper.CardInfo(
                baseCard.getCardNumber(),
                baseCard.getMonth(),
                baseCard.getYear(),
                baseCard.getOwner(),
                ""
        );
        paymentPage.fillForm(cardInfo);
        paymentPage.checkFieldError("CVC/CVV", "Неверный формат");

        assertEquals(0, getPaymentRecordsCount());
    }
}
