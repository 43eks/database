package データベース;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class タスク管理 {
    private static final String DB_URL = "jdbc:sqlite:/Users/genki/tasks.db";

    public static void main(String[] args) {
        initializeDatabase();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== タスク管理システム ===");
            System.out.println("1. タスク追加");
            System.out.println("2. タスク一覧表示");
            System.out.println("3. タスク完了更新");
            System.out.println("4. 終了");
            System.out.print("選択してください: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 改行を消費

            switch (choice) {
                case 1:
                    addTask(scanner);
                    break;
                case 2:
                    displayTasks();
                    break;
                case 3:
                    updateTaskStatus(scanner);
                    break;
                case 4:
                    System.out.println("アプリケーションを終了します。");
                    scanner.close();
                    return;
                default:
                    System.out.println("無効な入力です。もう一度選択してください。");
            }
        }
    }

    // **データベースを初期化**
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("データベース接続成功: " + DB_URL);
            } else {
                System.err.println("データベース接続に失敗");
                return;
            }

            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "title TEXT NOT NULL, " +
                                    "description TEXT, " +
                                    "status TEXT DEFAULT '未完了')";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("テーブル作成成功");
            } catch (SQLException e) {
                System.err.println("テーブル作成エラー: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("データベース接続エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // **タスクを追加**
    private static void addTask(Scanner scanner) {
        System.out.print("タスク名: ");
        String title = scanner.nextLine();
        System.out.print("説明 (省略可): ");
        String description = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insertSQL = "INSERT INTO tasks (title, description, status) VALUES (?, ?, '未完了')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, title);
                pstmt.setString(2, description.isEmpty() ? null : description);
                pstmt.executeUpdate();
                System.out.println("タスクを追加しました: " + title);
            }
        } catch (SQLException e) {
            System.err.println("タスク追加エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // **タスク一覧を表示**
    private static void displayTasks() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String selectSQL = "SELECT * FROM tasks";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSQL)) {

                System.out.println("\n=== タスク一覧 ===");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String description = rs.getString("description");
                    String status = rs.getString("status");
                    System.out.printf("ID: %d | タイトル: %s | 説明: %s | 状態: %s%n",
                                      id, title, (description != null ? description : "なし"), status);
                }
            }
        } catch (SQLException e) {
            System.err.println("タスク一覧取得エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // **タスクのステータスを更新**
    private static void updateTaskStatus(Scanner scanner) {
        System.out.print("完了にするタスクのIDを入力: ");
        int taskId = scanner.nextInt();
        scanner.nextLine(); // 改行を消費

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String updateSQL = "UPDATE tasks SET status = '完了' WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setInt(1, taskId);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("タスクID " + taskId + " の状態を '完了' に更新しました。");
                } else {
                    System.out.println("タスクID " + taskId + " は見つかりませんでした。");
                }
            }
        } catch (SQLException e) {
            System.err.println("タスク更新エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}