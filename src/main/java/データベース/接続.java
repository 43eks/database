package データベース;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class 接続 {
    public static void main(String[] args) {
        try {
            // SQLite JDBCドライバをロード
            Class.forName("org.sqlite.JDBC");

            // SQLiteデータベースに接続
            Connection connection = DriverManager.getConnection("jdbc:sqlite:your-database.db");
            Statement statement = connection.createStatement();
            
            // テーブルが存在しない場合、作成する
            String createTableSQL = "CREATE TABLE IF NOT EXISTS your_table (id INTEGER PRIMARY KEY, name TEXT)";
            statement.executeUpdate(createTableSQL);
            
            // サンプルデータを挿入
            String insertSQL = "INSERT INTO your_table (name) VALUES ('DETABAESE')";
            statement.executeUpdate(insertSQL);
            
            // テーブルの内容を確認するクエリ
            String sql = "SELECT * FROM your_table";
            ResultSet resultSet = statement.executeQuery(sql);
            
            // 結果を表示
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println(new java.io.File("your-database.db").getAbsolutePath());
            }
            
            // リソースを閉じる
            resultSet.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBCドライバが見つかりません");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}