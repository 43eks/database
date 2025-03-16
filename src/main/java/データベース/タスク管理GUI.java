package データベース;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class タスク管理GUI extends JFrame {
    private JTextArea taskListArea;
    private JTextField taskNameField, dueDateField, completedField;
    private JButton addButton, loadButton;

    public タスク管理GUI() {
        // データベースとテーブルの作成
        createDatabaseAndTable();

        // GUI設定
        setTitle("タスク管理アプリ");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListArea = new JTextArea();
        taskListArea.setEditable(false);
        add(new JScrollPane(taskListArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        inputPanel.add(new JLabel("タスク名:"));
        taskNameField = new JTextField();
        inputPanel.add(taskNameField);

        inputPanel.add(new JLabel("期限日:"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel("完了ステータス:"));
        completedField = new JTextField();
        inputPanel.add(completedField);

        addButton = new JButton("タスク追加");
        inputPanel.add(addButton);

        loadButton = new JButton("タスク一覧読み込み");
        inputPanel.add(loadButton);

        add(inputPanel, BorderLayout.SOUTH);

        // ボタンアクション
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
    }

    // データベースとテーブル作成
    private void createDatabaseAndTable() {
        String url = "jdbc:sqlite:/Users/genki/tasks.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // テーブル作成SQL文
                String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks2 ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "task_name TEXT NOT NULL, "
                        + "due_date TEXT NOT NULL, "
                        + "completed TEXT NOT NULL);";

                // テーブル作成のためのステートメント実行
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL);
                    System.out.println("テーブル 'tasks2' が作成されました。");
                }
            }
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // タスクをデータベースに追加
    private void addTask() {
        String taskName = taskNameField.getText();
        String dueDate = dueDateField.getText();
        String completed = completedField.getText();

        String url = "jdbc:sqlite:/Users/genki/tasks.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            String insertSQL = "INSERT INTO tasks2 (task_name, due_date, completed) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, taskName);
                pstmt.setString(2, dueDate);
                pstmt.setString(3, completed);
                pstmt.executeUpdate();
                taskListArea.append("タスク追加成功: " + taskName + "\n");
            }
        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスクをデータベースから読み込んで表示
    private void loadTasks() {
        String url = "jdbc:sqlite:/Users/genki/tasks.db";
        String query = "SELECT * FROM tasks2";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            taskListArea.setText(""); // 現在の表示内容をクリア

            while (rs.next()) {
                String taskName = rs.getString("task_name");
                String dueDate = rs.getString("due_date");
                String completed = rs.getString("completed");
                taskListArea.append("タスク名: " + taskName + ", 期限日: " + dueDate + ", 完了: " + completed + "\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            タスク管理GUI app = new タスク管理GUI();
            app.setVisible(true);
        });
    }
}