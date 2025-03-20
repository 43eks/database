package データベース;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class 習慣トラッカー {
    private static final String DB_URL = "jdbc:sqlite:habits.db";

    public static void main(String[] args) {
        createTable(); // データベーステーブルを作成
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== 習慣トラッカー ===");
            System.out.println("1. 習慣を追加");
            System.out.println("2. 習慣を一覧表示");
            System.out.println("3. 習慣を削除");
            System.out.println("4. 終了");
            System.out.print("選択: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 改行を消費

            switch (choice) {
                case 1:
                    System.out.print("習慣名を入力: ");
                    String name = scanner.nextLine();
                    System.out.print("カテゴリを入力（例: 健康, 学習, 仕事）: ");
                    String category = scanner.nextLine();
                    addHabit(name, category);
                    break;
                case 2:
                    listHabits();
                    break;
                case 3:
                    System.out.print("削除する習慣のIDを入力: ");
                    int id = scanner.nextInt();
                    deleteHabit(id);
                    break;
                case 4:
                    System.out.println("終了します。");
                    scanner.close();
                    return;
                default:
                    System.out.println("無効な選択です。");
            }
        }
    }

    // 📌 データベースのテーブルを作成
    private static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS habits (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "name TEXT NOT NULL, " +
                     "category TEXT NOT NULL);";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // 📌 習慣をデータベースに追加
    private static void addHabit(String name, String category) {
        String sql = "INSERT INTO habits (name, category) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
            System.out.println("習慣を追加しました: " + name);
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // 📌 習慣の一覧表示
    private static void listHabits() {
        String sql = "SELECT * FROM habits";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== 登録された習慣一覧 ===");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("name") + " (カテゴリ: " + rs.getString("category") + ")");
            }
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // 📌 習慣を削除
    private static void deleteHabit(int id) {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("習慣を削除しました。");
            } else {
                System.out.println("IDが見つかりません。");
            }
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }
}