package データベース;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class タスク管理GUI extends JFrame {
    private JTextArea taskListArea;
    private JTextField taskNameField, dueDateField, deleteTaskIdField;
    private JCheckBox completedCheckBox;
    private JButton addButton, loadButton, deleteButton, filterButton;
    private JComboBox<String> yearComboBox, monthComboBox;
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public タスク管理GUI() {
        // データベースとテーブル作成
        createDatabaseAndTable();

        // GUI設定
        setTitle("タスク管理アプリ");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListArea = new JTextArea();
        taskListArea.setEditable(false);
        add(new JScrollPane(taskListArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));

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
        loadButton = new JButton("全タスク表示");

        inputPanel.add(addButton);
        inputPanel.add(loadButton);

        add(inputPanel, BorderLayout.SOUTH);

        // 年と月の選択用パネル
        JPanel filterPanel = new JPanel(new FlowLayout());

       // yearComboBox = new JComboBox<>(getYearOptions());
       // monthComboBox = new JComboBox<>(getMonthOptions());

        filterButton = new JButton("選択した月のタスク表示");

        filterPanel.add(new JLabel("年:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(new JLabel("月:"));
        filterPanel.add(monthComboBox);
        filterPanel.add(filterButton);

        add(filterPanel, BorderLayout.NORTH);

        // タスク削除用のパネル
        JPanel deletePanel = new JPanel(new GridLayout(1, 3));
        deletePanel.add(new JLabel("削除するタスクID:"));
        deleteTaskIdField = new JTextField();
        deletePanel.add(deleteTaskIdField);
        deleteButton = new JButton("タスク削除");
        deletePanel.add(deleteButton);
        add(deletePanel, BorderLayout.EAST);

        // ボタンアクション
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
        deleteButton.addActionListener(e -> deleteTask());
        filterButton.addActionListener(e -> filterTasksByMonth());
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

    // 全タスク読み込み
    private void loadTasks() {
        String query = "SELECT * FROM tasks ORDER BY due_date ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            taskListArea.setText("");

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
        String taskId = deleteTaskIdField.getText();

        if (taskId.isEmpty()) {
            taskListArea.append("エラー: 削除するタスクIDを入力してください。\n");
            return;
        }

        String deleteSQL = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, Integer.parseInt(taskId));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                taskListArea.append("タスク削除成功: ID " + taskId + "\n");
            } else {
                taskListArea.append("エラー: タスクが見つかりません。\n");
            }

        } catch (SQLException e) {
            taskListArea.append("エラー: " + e.getMessage() + "\n");
        }
    }

    // 月ごとのタスクを表示
    private void filterTasksByMonth() {
        String year = (String) yearComboBox.getSelectedItem();
        String month = (String) monthComboBox.getSelectedItem();

        String query = "SELECT * FROM tasks WHERE strftime('%Y-%m', due_date) = ? ORDER BY due_date ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, year + "-" + month);
            ResultSet rs = pstmt.executeQuery();

            taskListArea.setText("");

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new タスク管理GUI().setVisible(true));
    }
}