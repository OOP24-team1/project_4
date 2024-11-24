package project4;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudyManagementPanel extends JPanel {
    private CardLayout cardLayout;
    private final String FILE_NAME = "study_records.txt";

    public StudyManagementPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JButton recordButton = createButton("학습 시간 기록");
        JButton checkButton = createButton("학습 시간 확인");
        JButton backButton = createButton("뒤로가기");

        mainPanel.add(recordButton);
        mainPanel.add(checkButton);
        mainPanel.add(backButton);

        recordButton.addActionListener(e -> showPanel("RecordPanel"));
        checkButton.addActionListener(e -> showPanel("CheckPanel"));
        backButton.addActionListener(e -> goBackToMain());

        add(mainPanel, "MainMenu");
        add(createRecordPanel(), "RecordPanel");
        add(createCheckPanel(), "CheckPanel");

        cardLayout.show(this, "MainMenu");
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // 학습 시간 기록 패널
    private JPanel createRecordPanel() {
        JPanel recordPanel = new JPanel();
        recordPanel.setLayout(new GridLayout(3, 2, 5, 5)); // 항목 간격 조정

        JTextField dateField = new JTextField();
        JTextField timeField = new JTextField();
        JButton saveButton = createButton("저장");
        JButton backButton = createButton("뒤로가기");

        recordPanel.add(new JLabel("날짜 (YYYY-MM-DD):"));
        recordPanel.add(dateField);
        recordPanel.add(new JLabel("공부 시간 (HH:mm):"));
        recordPanel.add(timeField);
        recordPanel.add(saveButton);
        recordPanel.add(backButton);

        saveButton.addActionListener(e -> {
            String date = dateField.getText();
            String time = timeField.getText();

            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(null, "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력하세요.");
                return;
            }

            if (!isValidTime(time)) {
                JOptionPane.showMessageDialog(null, "시간 형식이 잘못되었습니다. HH:mm 형식으로 입력하세요.");
                return;
            }

            saveStudyRecord(date, time);
            JOptionPane.showMessageDialog(null, "학습 기록이 저장되었습니다!");

            dateField.setText("");
            timeField.setText("");
        });

        backButton.addActionListener(e -> showPanel("MainMenu"));

        return recordPanel;
    }

    // 학습 시간 확인 패널
    private JPanel createCheckPanel() {
        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BorderLayout());

        JLabel dateLabel = new JLabel("기준 날짜 선택:");
        JTextField dateField = new JTextField();
        JButton checkButton = createButton("확인");
        JButton backButton = createButton("뒤로가기");
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 1, 5, 5)); // 입력 필드 간 간격 조정
        inputPanel.add(dateLabel);
        inputPanel.add(dateField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 5, 5)); // 버튼 간 간격 조정
        buttonPanel.add(checkButton);
        buttonPanel.add(backButton);

        checkPanel.add(inputPanel, BorderLayout.NORTH);
        checkPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        checkPanel.add(buttonPanel, BorderLayout.SOUTH);

        checkButton.addActionListener(e -> {
            String date = dateField.getText();

            if (!isValidDate(date)) {
                JOptionPane.showMessageDialog(null, "날짜 형식이 잘못되었습니다. YYYY-MM-DD 형식으로 입력하세요.");
                return;
            }

            String results = calculateStudyTimes(date);
            resultArea.setText(results);
        });

        backButton.addActionListener(e -> showPanel("MainMenu"));

        return checkPanel;
    }

    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveStudyRecord(String date, String time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(date + "|" + time);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String calculateStudyTimes(String date) {
        Map<String, Duration> dailyTotals = new HashMap<>();
        LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String recordDate = parts[0];
                String recordTime = parts[1];
                LocalTime time = LocalTime.parse(recordTime, DateTimeFormatter.ofPattern("HH:mm"));

                dailyTotals.putIfAbsent(recordDate, Duration.ZERO);
                dailyTotals.put(recordDate, dailyTotals.get(recordDate).plusHours(time.getHour()).plusMinutes(time.getMinute()));
            }
        } catch (IOException e) {
            return "파일 읽기 중 오류가 발생했습니다: " + e.getMessage();
        }

        StringBuilder results = new StringBuilder();
        Duration dailyTotal = dailyTotals.getOrDefault(date, Duration.ZERO);

        results.append("일별 총 학습 시간: ").append(formatDuration(dailyTotal)).append("\n");

        Duration weeklyTotal = Duration.ZERO;
        Duration monthlyTotal = Duration.ZERO;

        for (Map.Entry<String, Duration> entry : dailyTotals.entrySet()) {
            LocalDate recordDate = LocalDate.parse(entry.getKey(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (isSameWeek(targetDate, recordDate)) {
                weeklyTotal = weeklyTotal.plus(entry.getValue());
            }
            if (isSameMonth(targetDate, recordDate)) {
                monthlyTotal = monthlyTotal.plus(entry.getValue());
            }
        }

        results.append("주별 총 학습 시간: ").append(formatDuration(weeklyTotal)).append("\n");
        results.append("월별 총 학습 시간: ").append(formatDuration(monthlyTotal)).append("\n");

        return results.toString();
    }

    private boolean isSameWeek(LocalDate baseDate, LocalDate compareDate) {
        LocalDate baseStart = baseDate.with(DayOfWeek.MONDAY);
        LocalDate baseEnd = baseDate.with(DayOfWeek.SUNDAY);
        return !compareDate.isBefore(baseStart) && !compareDate.isAfter(baseEnd);
    }

    private boolean isSameMonth(LocalDate baseDate, LocalDate compareDate) {
        return baseDate.getMonth() == compareDate.getMonth() && baseDate.getYear() == compareDate.getYear();
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%d시간 %d분", hours, minutes);
    }

    private void showPanel(String name) {
        cardLayout.show(this, name);
    }

    private void goBackToMain() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // 현재 창 닫기
        new MainFrame(); // MainFrame 열기
    }
}
