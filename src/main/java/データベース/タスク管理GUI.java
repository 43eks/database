package データベース;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class タスク管理GUI extends JFrame {
    private JTextArea taskListArea;
    private JTextField taskNameField, dueDateField, completedField;
    private JButton addButton, loadButton, exportButton;

    public タスク管理GUI() {
        createDatabaseAndTable();

        setTitle("タスク管理アプリ");
        setSize(500, 400);
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

        inputPanel.add(new JLabel("期限日 (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel("完了ステータス (0:未完了, 1:完了):"));
        completedField = new JTextField();
        inputPanel.add(completedField);

        addButton = new JButton("タスク追加");
        inputPanel.add(addButton);

        loadButton = new JButton("タスク一覧読み込み");
        inputPanel.add(loadButton);

        exportButton = new JButton("CSVエクスポート");
        inputPanel.add(exportButton);

        add(inputPanel, BorderLayout.SOUTH);

        // ボタンのアクション設定
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
        exportButton.addActionListener(e -> exportTasksToCSV());
    }

    // データベースとテーブル作成
    private void createDatabaseAndTable() {
        String url = "jdbc:sqlite:tasks.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "task_name TEXT NOT NULL, "
                        + "due_date TEXT, "
                        + "completed INTEGER DEFAULT 0);";

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSQL);
                    System.out.println("テーブル 'tasks' が作成されました。");
                }
            }
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // タスクを追加
    private void addTask() {
        String taskName = taskNameField.getText();
        String dueDate = dueDateField.getText();
        String completed = completedField.getText();

        if (taskName.isEmpty() || completed.isEmpty()) {
            JOptionPane.showMessageDialog(this, "タスク名と完了ステータスは必須です！");
            return;
        }

        String url = "jdbc:sqlite:tasks.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            String insertSQL = "INSERT INTO tasks (task_name, due_date, completed) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, taskName);
                pstmt.setString(2, dueDate);
                pstmt.setInt(3, Integer.parseInt(completed));
                pstmt.executeUpdate();
                taskListArea.append("タスク追加成功: " + taskName + "\n");
            }
        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスク一覧を表示
    private void loadTasks() {
        String url = "jdbc:sqlite:tasks.db";
        String query = "SELECT * FROM tasks ORDER BY due_date ASC";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            taskListArea.setText("");

            while (rs.next()) {
                int id = rs.getInt("id");
                String taskName = rs.getString("task_name");
                String dueDate = rs.getString("due_date");
                int completed = rs.getInt("completed");

                String status = (completed == 1) ? "完了" : "未完了";
                taskListArea.append("ID: " + id + " | タスク名: " + taskName +
                        " | 期限日: " + (dueDate != null ? dueDate : "なし") +
                        " | ステータス: " + status + "\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスクをCSVファイルにエクスポート
    private void exportTasksToCSV() {
        String url = "jdbc:sqlite:tasks.db";
        String query = "SELECT * FROM tasks ORDER BY due_date ASC";
        String csvFile = "tasks.csv";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter writer = new FileWriter(csvFile)) {

            // CSVヘッダーを書く
            writer.append("ID,タスク名,期限日,完了ステータス\n");

            // データを書き出し
            while (rs.next()) {
                int id = rs.getInt("id");
                String taskName = rs.getString("task_name");
                String dueDate = rs.getString("due_date");
                int completed = rs.getInt("completed");

                writer.append(id + "," + taskName + "," + (dueDate != null ? dueDate : "なし") + "," + completed + "\n");
            }

            JOptionPane.showMessageDialog(this, "CSVエクスポート成功！\n保存先: " + csvFile);
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, "エクスポートエラー: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            タスク管理GUI app = new タスク管理GUI();
            app.setVisible(true);
        });
    }
}