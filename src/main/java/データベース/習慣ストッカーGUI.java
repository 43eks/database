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

    private Object exportHabitsToCSV() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private Object searchHabits() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private Object recordHabitCompletion() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private Object addHabit() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
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
                    + "completed_at TEXT DEFAULT NULL)";
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

    /** 📌 習慣をロード */
    private void loadHabits() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM habits")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("completed_at")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "習慣取得エラー: " + e.getMessage());
        }
    }

    /** 📌 カテゴリの追加 */
    private void addCategory() {
        String newCategory = newCategoryField.getText().trim();
        if (newCategory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "カテゴリ名を入力してください。");
            return;
        }

        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCategory);
            pstmt.executeUpdate();
            loadCategories();
            newCategoryField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "カテゴリ追加エラー: " + e.getMessage());
        }
    }

    /** 📌 カテゴリの削除 */
    private void deleteCategory() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "削除するカテゴリを選択してください。");
            return;
        }

        String sql = "DELETE FROM categories WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedCategory);
            pstmt.executeUpdate();
            loadCategories();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "カテゴリ削除エラー: " + e.getMessage());
        }
    }

    /** 📌 メインメソッド */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new 習慣ストッカーGUI().setVisible(true);
        });
    }
}