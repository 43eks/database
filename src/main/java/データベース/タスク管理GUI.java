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
import java.time.LocalDate;
import java.util.Vector;

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
    private JTextField taskNameField, dueDateField;
    private JCheckBox completedCheckBox;
    private JButton addButton, loadButton, deleteButton, updateButton, filterButton;
    private JComboBox<String> yearComboBox, monthComboBox;
    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    public タスク管理GUI() {
        // データベースとテーブル作成
        createDatabaseAndTable();

        // GUI設定
        setTitle("タスク管理アプリ");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        taskListArea = new JTextArea();
        taskListArea.setEditable(false);
        add(new JScrollPane(taskListArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));

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
        deleteButton = new JButton("選択タスク削除");
        updateButton = new JButton("タスク更新");

        inputPanel.add(addButton);
        inputPanel.add(loadButton);
        inputPanel.add(deleteButton);
        inputPanel.add(updateButton);

        add(inputPanel, BorderLayout.SOUTH);

        // 年と月の選択用パネル
        JPanel filterPanel = new JPanel(new FlowLayout());

        yearComboBox = new JComboBox<>(getYearOptions());
        monthComboBox = new JComboBox<>(getMonthOptions());

        filterButton = new JButton("選択した月のタスク表示");

        filterPanel.add(new JLabel("年:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(new JLabel("月:"));
        filterPanel.add(monthComboBox);
        filterPanel.add(filterButton);

        add(filterPanel, BorderLayout.NORTH);

        // ボタンアクション
        addButton.addActionListener(e -> addTask());
        loadButton.addActionListener(e -> loadTasks());
        deleteButton.addActionListener(e -> deleteTask());
        updateButton.addActionListener(e -> updateTask());
        filterButton.addActionListener(e -> filterTasksByMonth());
    }

    private Object updateTask() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private Object deleteTask() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
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

    // 年の選択肢を作成
    private String[] getYearOptions() {
        int currentYear = LocalDate.now().getYear();
        Vector<String> years = new Vector<>();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(String.valueOf(i));
        }
        return years.toArray(new String[0]);
    }

    // 月の選択肢を作成
    private String[] getMonthOptions() {
        Vector<String> months = new Vector<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.format("%02d", i));
        }
        return months.toArray(new String[0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new タスク管理GUI().setVisible(true));
    }
}