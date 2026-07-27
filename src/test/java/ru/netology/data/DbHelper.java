package ru.netology.data;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.DriverManager;
import java.sql.Connection;

//DbHelper отвечает за отправку SQL-запросов в PostgreSQL, очистку таблиц и проверку статусов транзакций

public class DbHelper {
    private static final QueryRunner runner = new QueryRunner();

    // получаем URL из Gradle, а логин и пароль берем строго из docker-compose
    private static final String url = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/aqa-shop-diplom");
    private static final String user = "aqa_shop";
    private static final String password = "secret";

    private DbHelper() {
    }

    @SneakyThrows
    private static Connection getConnection() {
        return DriverManager.getConnection(url, user, password);
    }

    // метод очистки базы перед каждым тестом (учитывает foreign keys)
    @SneakyThrows
    public static void cleanDatabase() {
        String deleteOrder = "DELETE FROM order_entity;";
        String deletePayment = "DELETE FROM payment_entity;";
        String deleteCredit = "DELETE FROM credit_request_entity;";

        try (Connection conn = getConnection()) {
            runner.execute(conn, deleteOrder);
            runner.execute(conn, deletePayment);
            runner.execute(conn, deleteCredit);
        }
    }

    // метод для получения статуса последней дебетовой оплаты (APPROVED / DECLINED)
    @SneakyThrows
    public static String getPaymentStatus() {
        String sql = "SELECT status FROM payment_entity ORDER BY created DESC LIMIT 1;";
        try (Connection conn = getConnection()) {
            String status = runner.query(conn, sql, new ScalarHandler<>());
            return status != null ? status : ""; // Возвращаем пустую строку вместо null для защиты от NPE в тестах
        }
    }

    // метод проверки количества записей (для негативных тестов, должно возвращать 0)
    @SneakyThrows
    public static long getPaymentRecordsCount() {
        String sql = "SELECT COUNT(*) FROM payment_entity;";
        try (Connection conn = getConnection()) {
            Number count = runner.query(conn, sql, new ScalarHandler<>());
            return count != null ? count.longValue() : 0L; // Безопасное приведение любого числового типа БД к long
        }
    }
}
