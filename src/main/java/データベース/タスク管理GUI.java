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
import javax.swing.JCheckBox;
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
    private JTextField taskNameField, dueDateField;
    private JCheckBox completedCheckBox;
    private JButton addButton, loadButton, deleteButton, updateButton;
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public タスク管理GUI() {
        // データベースとテーブル作成
        createDatabaseAndTable();

        // GUI設定
        setTitle("タスク管理アプリ");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListArea = new JTextArea();
        taskListArea.setEditable(false);
        add(new JScrollPane(taskListArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));

        inputPanel.add(new JLabel("タスク名:"));
        taskNameField = new JTextField();
        inputPanel.add(taskNameField);

        inputPanel.add(new JLabel("期限日 (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel("完了:"));
        completedCheckBox = new JCheckBox();
        inputPanel.add(completedCheckBox);

        addButton = new JButton("タスク追加");
        loadButton = new JButton("タスク一覧");
        deleteButton = new JButton("選択タスク削除");
        updateButton = new JButton("タスク更新");

        inputPanel.add(addButton);
        inputPanel.add(loadButton);
        inputPanel.add(deleteButton);
        inputPanel.add(updateButton);

        add(inputPanel, BorderLayout.SOUTH);

        // ボタンアクション
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
        deleteButton.addActionListener(e -> deleteTask());
        updateButton.addActionListener(e -> updateTask());
    }

    // データベースとテーブル作成
    private void createDatabaseAndTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "task_name TEXT NOT NULL, "
                    + "due_date DATE NOT NULL, "
                    + "completed INTEGER NOT NULL CHECK (completed IN (0, 1)));";

            stmt.execute(createTableSQL);
            System.out.println("テーブル 'tasks' が作成されました。");
        } catch (SQLException e) {
            System.out.println("エラー: " + e.getMessage());
        }
    }

    // タスク追加
    private void addTask() {
        String taskName = taskNameField.getText();
        String dueDate = dueDateField.getText();
        int completed = completedCheckBox.isSelected() ? 1 : 0;

        String insertSQL = "INSERT INTO tasks (task_name, due_date, completed) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, taskName);
            pstmt.setString(2, dueDate);
            pstmt.setInt(3, completed);
            pstmt.executeUpdate();
            taskListArea.append("タスク追加成功: " + taskName + "\n");
        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスク一覧読み込み (期限順)
    private void loadTasks() {
        String query = "SELECT * FROM tasks ORDER BY due_date ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            taskListArea.setText(""); // 現在の表示をクリア

            while (rs.next()) {
                int id = rs.getInt("id");
                String taskName = rs.getString("task_name");
                String dueDate = rs.getString("due_date");
                boolean completed = rs.getInt("completed") == 1;
                taskListArea.append("ID: " + id + " | " + taskName + " | 期限: " + dueDate + " | 完了: " + (completed ? "✔" : "×") + "\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスク削除
    private void deleteTask() {
        String taskIdStr = JOptionPane.showInputDialog("削除するタスクのIDを入力:");
        if (taskIdStr == null || taskIdStr.trim().isEmpty()) return;

        int taskId = Integer.parseInt(taskIdStr);
        String deleteSQL = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, taskId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                taskListArea.append("タスク削除成功 (ID: " + taskId + ")\n");
            } else {
                taskListArea.append("タスクが見つかりません (ID: " + taskId + ")\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // タスク更新 (完了ステータス変更)
    private void updateTask() {
        String taskIdStr = JOptionPane.showInputDialog("更新するタスクのIDを入力:");
        if (taskIdStr == null || taskIdStr.trim().isEmpty()) return;

        int taskId = Integer.parseInt(taskIdStr);
        int newCompleted = JOptionPane.showConfirmDialog(null, "タスクを完了しますか？", "タスク更新", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ? 1 : 0;

        String updateSQL = "UPDATE tasks SET completed = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, newCompleted);
            pstmt.setInt(2, taskId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                taskListArea.append("タスク更新成功 (ID: " + taskId + ")\n");
            } else {
                taskListArea.append("タスクが見つかりません (ID: " + taskId + ")\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new タスク管理GUI().setVisible(true));
    }
}