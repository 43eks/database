package データベース;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class 勤怠連携バックアップ {

    // データベースをバックアップするメソッド
    public static void backupDatabase(String sourceDbPath, String backupDbPath) {
        try (Connection sourceConn = DriverManager.getConnection("jdbc:sqlite:" + sourceDbPath);
             Connection backupConn = DriverManager.getConnection("jdbc:sqlite:" + backupDbPath)) {

            // トランザクション開始
            sourceConn.setAutoCommit(false);
            backupConn.setAutoCommit(false);

            // バックアップ用のデータベースを添付
            sourceConn.createStatement().execute("ATTACH DATABASE '" + backupDbPath + "' AS backup");

            // データをバックアップ先のデータベースにエクスポート
            sourceConn.createStatement().execute("SELECT sqlcipher_export('backup')");

            // エクスポート後、バックアップ先のデータベースを切り離す
            sourceConn.createStatement().execute("DETACH DATABASE backup");

            // トランザクションをコミット
            sourceConn.commit();
            backupConn.commit();

            System.out.println("バックアップ成功: " + backupDbPath);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("バックアップ失敗: " + e.getMessage());
        }
    }

    // 実行するメインメソッド
    public static void main(String[] args) {
        String sourceDbPath = "/Users/genki/attendance.db";  // 元のデータベース
        String backupDbPath = "/Users/genki/attendance_backup.db";  // バックアップ先のデータベースファイル

        backupDatabase(sourceDbPath, backupDbPath);  // バックアップ実行
    }
}