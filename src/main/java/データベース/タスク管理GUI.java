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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class タスク管理GUI extends JFrame {
    private JTextArea taskListArea;
    private JTextField taskNameField, dueDateField, completedField;
    private JTextField editIdField, editNameField, editDueDateField, editCompletedField;
    private JButton addButton, loadButton, updateButton;

    public タスク管理GUI() {
        // データベースとテーブルの作成
        createDatabaseAndTable();

        // GUI設定
        setTitle("タスク管理アプリ");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListArea = new JTextArea();
        taskListArea.setEditable(false);
        add(new JScrollPane(taskListArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2));

        // タスク追加用フィールド
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

        add(inputPanel, BorderLayout.NORTH);

        // タスク編集用フィールド
        JPanel editPanel = new JPanel();
        editPanel.setLayout(new GridLayout(5, 2));

        editPanel.add(new JLabel("編集対象のタスクID:"));
        editIdField = new JTextField();
        editPanel.add(editIdField);

        editPanel.add(new JLabel("新しいタスク名:"));
        editNameField = new JTextField();
        editPanel.add(editNameField);

        editPanel.add(new JLabel("新しい期限日 (YYYY-MM-DD):"));
        editDueDateField = new JTextField();
        editPanel.add(editDueDateField);

        editPanel.add(new JLabel("新しい完了ステータス (0:未完了, 1:完了):"));
        editCompletedField = new JTextField();
        editPanel.add(editCompletedField);

        updateButton = new JButton("タスクを更新");
        editPanel.add(updateButton);

        add(editPanel, BorderLayout.SOUTH);

        // ボタンのアクション設定
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
        updateButton.addActionListener(e -> updateTask());
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

    // タスクを読み込む
    private void loadTasks() {
        String url = "jdbc:sqlite:tasks.db";
        String query = "SELECT * FROM tasks ORDER BY due_date ASC";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            taskListArea.setText(""); // 表示リセット

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

    // タスクを更新
    private void updateTask() {
        String idText = editIdField.getText();
        String newName = editNameField.getText();
        String newDueDate = editDueDateField.getText();
        String newCompleted = editCompletedField.getText();

        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "更新するタスクのIDを入力してください！");
            return;
        }

        int id = Integer.parseInt(idText);
        String url = "jdbc:sqlite:tasks.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            String updateSQL = "UPDATE tasks SET task_name = ?, due_date = ?, completed = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newDueDate);
                pstmt.setInt(3, Integer.parseInt(newCompleted));
                pstmt.setInt(4, id);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "タスク更新成功！");
                    loadTasks();
                } else {
                    JOptionPane.showMessageDialog(this, "タスクが見つかりませんでした。");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "エラー: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            タスク管理GUI app = new タスク管理GUI();
            app.setVisible(true);
        });
    }
}