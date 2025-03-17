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
            System.out.println("\n===== タスク管理アプリ =====");
            System.out.println("1. タスクを追加");
            System.out.println("2. タスクを一覧表示");
            System.out.println("3. タスクを完了にする");
            System.out.println("4. タスクを削除");
            System.out.println("5. 終了");
            System.out.print("選択してください: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // 改行を消費

            switch (choice) {
                case 1:
                    addTask(scanner);
                    break;
                case 2:
                    listTasks();
                    break;
                case 3:
                    updateTaskStatus(scanner);
                    break;
                case 4:
                    deleteTask(scanner);
                    break;
                case 5:
                    System.out.println("アプリを終了します。");
                    scanner.close();
                    return;
                default:
                    System.out.println("無効な選択です。もう一度選んでください。");
            }
        }
    }

    // データベースの初期化
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String checkColumnSQL = "PRAGMA table_info(tasks)";
            boolean hasDueDateColumn = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumnSQL)) {
                while (rs.next()) {
                    if ("due_date".equalsIgnoreCase(rs.getString("name"))) {
                        hasDueDateColumn = true;
                        break;
                    }
                }
            }

            if (!hasDueDateColumn) {
                String addColumnSQL = "ALTER TABLE tasks ADD COLUMN due_date TEXT";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(addColumnSQL);
                    System.out.println("due_dateカラムを追加しました。");
                }
            }
        } catch (SQLException e) {
            System.err.println("データベースの初期化エラー: " + e.getMessage());
        }
    }

    // タスクの追加
    private static void addTask(Scanner scanner) {
        System.out.print("タスクのタイトルを入力: ");
        String title = scanner.nextLine();
        System.out.print("タスクの説明を入力: ");
        String description = scanner.nextLine();
        
        System.out.println("優先度を選択してください (1: 高, 2: 中, 3: 低): ");
        int priority;
        while (true) {
            try {
                priority = Integer.parseInt(scanner.nextLine());
                if (priority >= 1 && priority <= 3) break;
            } catch (NumberFormatException ignored) {}
            System.out.println("無効な入力です。1〜3の値を入力してください。");
        }

        String insertSQL = "INSERT INTO tasks (title, description, priority) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, priority);
            pstmt.executeUpdate();
            System.out.println("タスクを追加しました。");
        } catch (SQLException e) {
            System.err.println("タスク追加エラー: " + e.getMessage());
        }
    }

    // タスクの一覧表示
    private static void listTasks() {
        String selectSQL = "SELECT * FROM tasks ORDER BY priority ASC, id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            System.out.println("\n===== タスク一覧 (優先度順) =====");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String status = rs.getString("status");
                int priority = rs.getInt("priority");
                
                String priorityStr = switch (priority) {
                    case 1 -> "高";
                    case 2 -> "中";
                    default -> "低";
                };
                
                System.out.println(id + ". [" + status + "] " + title + " (優先度: " + priorityStr + ") - " + description);
            }
        } catch (SQLException e) {
            System.err.println("タスク一覧取得エラー: " + e.getMessage());
        }
    }

    // タスクの完了更新
    private static void updateTaskStatus(Scanner scanner) {
        System.out.print("完了するタスクのIDを入力: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // 改行を消費

        String updateSQL = "UPDATE tasks SET status = '完了' WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("タスクを完了しました。");
            } else {
                System.out.println("指定されたIDのタスクが見つかりません。");
            }
        } catch (SQLException e) {
            System.err.println("タスク更新エラー: " + e.getMessage());
        }
    }

    // タスクの削除
    private static void deleteTask(Scanner scanner) {
        System.out.print("削除するタスクのIDを入力: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // 改行を消費

        String deleteSQL = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("タスクを削除しました。");
            } else {
                System.out.println("指定されたIDのタスクが見つかりません。");
            }
        } catch (SQLException e) {
            System.err.println("タスク削除エラー: " + e.getMessage());
        }
    }
}