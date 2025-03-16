package データベース;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class タスク管理GUI extends JFrame {
    
    private JTable taskTable;
    private JTextField taskNameField, dueDateField;
    private JButton addButton, updateButton, deleteButton;
    private static final String DB_URL = "/Users/genki/tasks.db";

    public タスク管理GUI() {
        // フレーム設定
        setTitle("タスク管理アプリ");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 入力フォームの設定
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2));
        JLabel taskNameLabel = new JLabel("タスク名:");
        taskNameField = new JTextField();
        JLabel dueDateLabel = new JLabel("期日 (YYYY-MM-DD):");
        dueDateField = new JTextField();

        inputPanel.add(taskNameLabel);
        inputPanel.add(taskNameField);
        inputPanel.add(dueDateLabel);
        inputPanel.add(dueDateField);

        // ボタン設定
        addButton = new JButton("タスク追加");
        updateButton = new JButton("タスク更新");
        deleteButton = new JButton("タスク削除");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // テーブル設定
        taskTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(taskTable);
        loadTasks();

        // コンポーネントをフレームに追加
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // ボタンアクション
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTask();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });
    }

    // データベースからタスクを読み込んでテーブルに表示
    private void loadTasks() {
        try {
            // SQLite JDBCドライバのロード
            Class.forName("org.sqlite.JDBC");
            
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:/Users/genki/tasks.db")) {
                String query = "SELECT * FROM tasks";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                // テーブルに表示するデータを格納
                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("ID");
                model.addColumn("タスク名");
                model.addColumn("期日");
                model.addColumn("完了");
                
                while (rs.next()) {
                    model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getString("task_name"),
                        rs.getString("due_date"),
                        rs.getString("completed")
                    });
                }
                
                taskTable.setModel(model);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // タスク追加
    private void addTask() {
        String taskName = taskNameField.getText();
        String dueDate = dueDateField.getText();

        if (taskName.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "タスク名と期日を入力してください");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO tasks (task_name, due_date) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, taskName);
            pstmt.setString(2, dueDate);
            pstmt.executeUpdate();

            loadTasks();  // 更新後に再読み込み
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // タスク更新
    private void updateTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) taskTable.getValueAt(selectedRow, 0);
            String taskName = taskNameField.getText();
            String dueDate = dueDateField.getText();

            if (taskName.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "タスク名と期日を入力してください");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String query = "UPDATE tasks SET task_name = ?, due_date = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, taskName);
                pstmt.setString(2, dueDate);
                pstmt.setInt(3, taskId);
                pstmt.executeUpdate();

                loadTasks();  // 更新後に再読み込み
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "更新するタスクを選択してください");
        }
    }

    // タスク削除
    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) taskTable.getValueAt(selectedRow, 0);

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String query = "DELETE FROM tasks WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, taskId);
                pstmt.executeUpdate();

                loadTasks();  // 更新後に再読み込み
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "削除するタスクを選択してください");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new タスク管理GUI().setVisible(true);
            }
        });
    }
}
