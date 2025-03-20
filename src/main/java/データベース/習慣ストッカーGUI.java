package データベース;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class 習慣ストッカーGUI extends JFrame {
    private JTextField habitNameField;
    private JButton addHabitButton, markCompletedButton, showHabitsButton, deleteHabitButton;
    private JTextArea habitListArea;
    private JComboBox<String> habitDropdown;
    private Connection conn;

    public 習慣ストッカーGUI() {
        setTitle("習慣トラッカー");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // データベースの接続とテーブル作成
        connectToDatabase();
        createTables();

        // GUIの作成
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        habitNameField = new JTextField();
        addHabitButton = new JButton("習慣を追加");
        markCompletedButton = new JButton("達成を記録");
        showHabitsButton = new JButton("習慣一覧を表示");
        deleteHabitButton = new JButton("習慣を削除");

        inputPanel.add(new JLabel("習慣名:"));
        inputPanel.add(habitNameField);
        inputPanel.add(addHabitButton);
        inputPanel.add(markCompletedButton);

        add(inputPanel, BorderLayout.NORTH);

        // 結果表示エリア
        habitListArea = new JTextArea();
        habitListArea.setEditable(false);
        add(new JScrollPane(habitListArea), BorderLayout.CENTER);

        // 達成記録 & 削除用パネル
        JPanel controlPanel = new JPanel();
        habitDropdown = new JComboBox<>();
        controlPanel.add(habitDropdown);
        controlPanel.add(showHabitsButton);
        controlPanel.add(deleteHabitButton);

        add(controlPanel, BorderLayout.SOUTH);

        // ボタンのアクションリスナー
        addHabitButton.addActionListener(this::addHabit);
        markCompletedButton.addActionListener(this::markHabitCompleted);
        showHabitsButton.addActionListener(this::showHabits);
        deleteHabitButton.addActionListener(this::deleteHabit);

        // 初期データ読み込み
        updateHabitDropdown();
    }

    // データベース接続
    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:habits.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // テーブル作成
    private void createTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS habits ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS habit_records ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "habit_id INTEGER NOT NULL, "
                    + "date TEXT NOT NULL, "
                    + "FOREIGN KEY (habit_id) REFERENCES habits(id))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 習慣を追加
    private void addHabit(ActionEvent e) {
        String habitName = habitNameField.getText().trim();
        if (habitName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "習慣名を入力してください。");
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO habits (name) VALUES (?)")) {
            pstmt.setString(1, habitName);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "習慣を追加しました: " + habitName);
            habitNameField.setText("");
            updateHabitDropdown();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 達成記録を追加
    private void markHabitCompleted(ActionEvent e) {
        String selectedHabit = (String) habitDropdown.getSelectedItem();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "習慣がありません。");
            return;
        }

        int habitId = getHabitId(selectedHabit);
        if (habitId == -1) return;

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO habit_records (habit_id, date) VALUES (?, ?)")) {
            pstmt.setInt(1, habitId);
            pstmt.setString(2, date);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "達成記録を追加しました: " + selectedHabit + " (" + date + ")");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 習慣一覧を表示
    private void showHabits(ActionEvent e) {
        habitListArea.setText("");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT h.name, r.date FROM habits h LEFT JOIN habit_records r ON h.id = r.habit_id ORDER BY r.date DESC")) {

            while (rs.next()) {
                String habitName = rs.getString("name");
                String date = rs.getString("date");
                habitListArea.append(habitName + " - 達成日: " + (date != null ? date : "未達成") + "\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 習慣を削除
    private void deleteHabit(ActionEvent e) {
        String selectedHabit = (String) habitDropdown.getSelectedItem();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "削除する習慣がありません。");
            return;
        }

        int habitId = getHabitId(selectedHabit);
        if (habitId == -1) return;

        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM habits WHERE id = ?")) {
            pstmt.setInt(1, habitId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "習慣を削除しました: " + selectedHabit);
            updateHabitDropdown();
            habitListArea.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 習慣IDを取得
    private int getHabitId(String habitName) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM habits WHERE name = ?")) {
            pstmt.setString(1, habitName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    // 習慣リストを更新
    private void updateHabitDropdown() {
        habitDropdown.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM habits")) {

            while (rs.next()) {
                habitDropdown.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new 習慣ストッカーGUI().setVisible(true));
    }
}
