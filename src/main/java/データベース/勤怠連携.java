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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class 勤怠連携 extends JFrame {

    private JTextField nameField;
    private JTextField timeField;
    private JTextArea logArea;
    private JButton clockInButton;
    private JButton clockOutButton;

    private static final String DB_URL = "jdbc:sqlite:attendance.db"; // SQLiteデータベースのパス

    public 勤怠連携() {
        // フレームの設定
        setTitle("勤怠管理システム");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 名前入力欄
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2));
        JLabel nameLabel = new JLabel("名前:");
        nameField = new JTextField();
        JLabel timeLabel = new JLabel("時間:");
        timeField = new JTextField();
        timeField.setEditable(false); // 時刻フィールドは編集不可
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(timeLabel);
        inputPanel.add(timeField);

        // ボタン設定
        clockInButton = new JButton("出勤");
        clockOutButton = new JButton("退勤");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(clockInButton);
        buttonPanel.add(clockOutButton);

        // ログエリア
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        // コンポーネントをフレームに追加
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // データベース初期化
        initializeDatabase();
        // 履歴を読み込んで表示
        loadAttendanceHistory();

        // 出勤ボタンのアクションリスナー
        clockInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordAttendance("出勤");
            }
        });

        // 退勤ボタンのアクションリスナー
        clockOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recordAttendance("退勤");
            }
        });
    }

    // 勤怠を記録するメソッド（出勤・退勤共通）
    private void recordAttendance(String status) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            logArea.append("エラー: 名前を入力してください。\n");
            return;
        }

        String date = LocalDate.now().toString(); // 今日の日付を取得
        String time = getCurrentTime(); // 現在時刻を取得
        timeField.setText(time); // 現在時刻を表示

        logArea.append(status + ": " + name + " - " + date + " " + time + "\n");
        saveAttendanceRecord(name, date, time, status);
    }

    // 現在の時刻を取得するメソッド
    private String getCurrentTime() {
        LocalTime currentTime = LocalTime.now(); // 現在時刻を取得
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // フォーマット
        return currentTime.format(formatter); // フォーマットした時刻を返す
    }

    // データベース初期化メソッド
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS attendance (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "name TEXT NOT NULL, " +
                                    "date TEXT NOT NULL, " +
                                    "time TEXT NOT NULL, " +
                                    "status TEXT NOT NULL)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL); // テーブル作成
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 勤怠記録をデータベースに保存するメソッド
    private void saveAttendanceRecord(String name, String date, String time, String status) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insertSQL = "INSERT INTO attendance (name, date, time, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, date);
                pstmt.setString(3, time);
                pstmt.setString(4, status);
                pstmt.executeUpdate(); // データ挿入
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 勤怠履歴をデータベースから読み込むメソッド
    private void loadAttendanceHistory() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String selectSQL = "SELECT * FROM attendance ORDER BY id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSQL)) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String date = rs.getString("date");
                    String time = rs.getString("time");
                    String status = rs.getString("status");
                    logArea.append(status + ": " + name + " - " + date + " " + time + "\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // GUIを表示
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new 勤怠連携().setVisible(true);
            }
        });
    }
}