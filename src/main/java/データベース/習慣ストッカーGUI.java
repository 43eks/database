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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class 習慣ストッカーGUI extends JFrame {
    private JTextField habitNameField, newCategoryField, searchField;
    private JComboBox<String> categoryComboBox;
    private JButton addButton, recordButton, searchButton, exportButton, addCategoryButton, deleteCategoryButton;
    private JTable habitTable;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:sqlite:habits.db";

    public 習慣ストッカーGUI() {
        setTitle("習慣トラッカー");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createDatabaseAndTables();

        // 📌 上部入力パネル
        JPanel inputPanel = new JPanel(new GridLayout(3, 3));
        inputPanel.add(new JLabel("習慣名:"));
        habitNameField = new JTextField();
        inputPanel.add(habitNameField);
        inputPanel.add(new JLabel("カテゴリ:"));

        categoryComboBox = new JComboBox<>();
        loadCategories();
        inputPanel.add(categoryComboBox);

        addButton = new JButton("習慣追加");
        inputPanel.add(addButton);
        recordButton = new JButton("達成記録");
        inputPanel.add(recordButton);
        add(inputPanel, BorderLayout.NORTH);

        // 📌 中央テーブル
        tableModel = new DefaultTableModel(new String[]{"ID", "習慣名", "カテゴリ", "達成日"}, 0);
        habitTable = new JTable(tableModel);
        add(new JScrollPane(habitTable), BorderLayout.CENTER);

        // 📌 下部検索・カテゴリ管理パネル
        JPanel bottomPanel = new JPanel();
        searchField = new JTextField(15);
        searchButton = new JButton("検索");
        exportButton = new JButton("エクスポート");

        newCategoryField = new JTextField(10);
        addCategoryButton = new JButton("カテゴリ追加");
        deleteCategoryButton = new JButton("カテゴリ削除");

        bottomPanel.add(new JLabel("検索:"));
        bottomPanel.add(searchField);
        bottomPanel.add(searchButton);
        bottomPanel.add(exportButton);
        bottomPanel.add(new JLabel("新カテゴリ:"));
        bottomPanel.add(newCategoryField);
        bottomPanel.add(addCategoryButton);
        bottomPanel.add(deleteCategoryButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 📌 ボタンアクション
        addButton.addActionListener(e -> addHabit());
        recordButton.addActionListener(e -> recordHabitCompletion());
        searchButton.addActionListener(e -> searchHabits());
        exportButton.addActionListener(e -> exportHabitsToCSV());
        addCategoryButton.addActionListener(e -> addCategory());
        deleteCategoryButton.addActionListener(e -> deleteCategory());

        loadHabits();
    }

    private Object deleteCategory() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private Object addCategory() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private void loadHabits() {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	/** 📌 データベースとテーブル作成 */
    private void createDatabaseAndTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // 習慣テーブル
            String createHabitsTableSQL = "CREATE TABLE IF NOT EXISTS habits ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "completed_at TEXT DEFAULT NULL"
                    + ")";
            stmt.execute(createHabitsTableSQL);

            // カテゴリテーブル
            String createCategoriesTableSQL = "CREATE TABLE IF NOT EXISTS categories ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL UNIQUE)";
            stmt.execute(createCategoriesTableSQL);

            // デフォルトカテゴリ追加
            stmt.execute("INSERT OR IGNORE INTO categories (name) VALUES ('運動'), ('読書'), ('学習'), ('健康')");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DBエラー: " + e.getMessage());
        }
    }

    /** 📌 カテゴリをコンボボックスにロード */
    private void loadCategories() {
        categoryComboBox.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM categories")) {

            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "カテゴリ取得エラー: " + e.getMessage());
        }
    }

    /** 📌 習慣をデータベースに追加 */
    private void addHabit() {
        String name = habitNameField.getText().trim();
        String category = (String) categoryComboBox.getSelectedItem();

        if (name.isEmpty() || category == null) {
            JOptionPane.showMessageDialog(this, "習慣名とカテゴリを入力してください。");
            return;
        }

        String sql = "INSERT INTO habits (name, category) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
            loadHabits();
            habitNameField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "追加エラー: " + e.getMessage());
        }
    }

    /** 📌 習慣の達成記録 */
    private void recordHabitCompletion() {
        int selectedRow = habitTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "記録する習慣を選択してください。");
            return;
        }

        int habitId = (int) tableModel.getValueAt(selectedRow, 0);
        String sql = "UPDATE habits SET completed_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.executeUpdate();
            loadHabits();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "記録エラー: " + e.getMessage());
        }
    }

    /** 📌 習慣を検索 */
    private void searchHabits() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadHabits();
            return;
        }

        String sql = "SELECT * FROM habits WHERE name LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                tableModel.setRowCount(0);
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("completed_at")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "検索エラー: " + e.getMessage());
        }
    }

    /** 📌 CSVエクスポート */
    private void exportHabitsToCSV() {
        try (FileWriter writer = new FileWriter("habits.csv")) {
            writer.write("ID,習慣名,カテゴリ,達成日\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write(tableModel.getValueAt(i, 0) + "," +
                        tableModel.getValueAt(i, 1) + "," +
                        tableModel.getValueAt(i, 2) + "," +
                        tableModel.getValueAt(i, 3) + "\n");
            }

            JOptionPane.showMessageDialog(this, "CSVエクスポート完了!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "CSVエクスポートエラー: " + e.getMessage());
        }
    }

    /** 📌 メインメソッド */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new 習慣ストッカーGUI().setVisible(true);
        });
    }
}