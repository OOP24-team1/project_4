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
        JButton setGoalButton = createButton("학습 목표 설정");
        JButton goalAchievementButton = createGoalAchievementButton(); // 목표 달성 확인 버튼
        JButton backButton = createButton("뒤로가기");

        mainPanel.add(recordButton);
        mainPanel.add(checkButton);
        mainPanel.add(setGoalButton);
        mainPanel.add(goalAchievementButton);
        mainPanel.add(backButton);

        recordButton.addActionListener(e -> showPanel("RecordPanel"));
        checkButton.addActionListener(e -> showPanel("CheckPanel"));
        setGoalButton.addActionListener(e -> showPanel("SetGoalPanel"));
        backButton.addActionListener(e -> goBackToMain());

        add(mainPanel, "MainMenu");
        add(createRecordPanel(), "RecordPanel");
        add(createCheckPanel(), "CheckPanel");
        add(createGoalPanel(), "SetGoalPanel");
        add(createGoalAchievementPanel(), "GoalAchievementPanel");

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
    
    private JPanel createGoalPanel() {
        JPanel goalPanel = new JPanel();
        goalPanel.setLayout(new GridLayout(4, 2, 10, 10));

        // Load existing goals
        StudyGoal existingGoals = loadGoals();

        JTextField dailyGoalField = new JTextField(formatDuration(existingGoals.getDailyGoal()));
        JTextField weeklyGoalField = new JTextField(formatDuration(existingGoals.getWeeklyGoal()));
        JTextField monthlyGoalField = new JTextField(formatDuration(existingGoals.getMonthlyGoal()));
        JButton saveButton = createButton("설정");
        JButton backButton = createButton("뒤로가기");

        goalPanel.add(new JLabel("일별 목표 학습시간:"));
        goalPanel.add(dailyGoalField);
        goalPanel.add(new JLabel("주별 목표 학습시간:"));
        goalPanel.add(weeklyGoalField);
        goalPanel.add(new JLabel("월별 목표 학습시간:"));
        goalPanel.add(monthlyGoalField);
        goalPanel.add(saveButton);
        goalPanel.add(backButton);

        saveButton.addActionListener(e -> {
            String dailyGoal = dailyGoalField.getText();
            String weeklyGoal = weeklyGoalField.getText();
            String monthlyGoal = monthlyGoalField.getText();

            // Convert text fields back to Duration before saving
            StudyGoal newGoals = new StudyGoal(parseDuration(dailyGoal), parseDuration(weeklyGoal), parseDuration(monthlyGoal));
            saveStudyGoalToFile(newGoals);
            JOptionPane.showMessageDialog(null, "학습 목표가 설정되었습니다!");

            dailyGoalField.setText("");
            weeklyGoalField.setText("");
            monthlyGoalField.setText("");
        });

        backButton.addActionListener(e -> showPanel("MainMenu"));

        return goalPanel;
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
    private StudyGoal loadGoals() {
        try (BufferedReader reader = new BufferedReader(new FileReader("study_goals.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split("\\|");
                Duration daily = StudyGoal.parseDuration(parts[0]);
                Duration weekly = StudyGoal.parseDuration(parts[1]);
                Duration monthly = StudyGoal.parseDuration(parts[2]);
                return new StudyGoal(daily, weekly, monthly);
            }
        } catch (IOException e) {
            System.out.println("학습 목표 파일 로드 중 오류 발생: " + e.getMessage());
        }
        // Default empty goals if none exist
        return new StudyGoal(Duration.ZERO, Duration.ZERO, Duration.ZERO);
    }

    private void saveStudyGoalToFile(StudyGoal goals) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("study_goals.txt"))) {
            writer.write(goals.toFileFormat());
        } catch (IOException e) {
            System.out.println("학습 목표 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }

    
    private JButton createGoalAchievementButton() {
        JButton button = createButton("학습 목표 달성 확인");
        button.addActionListener(e -> showPanel("GoalAchievementPanel"));
        return button;
    }

    private JPanel createGoalAchievementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("뒤로가기");
        backButton.addActionListener(e -> showPanel("MainMenu"));
        panel.add(backButton, BorderLayout.SOUTH);

        StudyGoal goals = loadGoals();
        Map<String, Duration> studyTimes = calculateStudyTimesUntilToday();

        String results = "목표 달성률:\n";
        Duration todayDuration = studyTimes.getOrDefault(LocalDate.now().toString(), Duration.ZERO);
        Duration totalWeeklyDuration = calculateTotalWeekly(studyTimes);
        Duration totalMonthlyDuration = calculateTotalMonthly(studyTimes);

        // 일별 목표 달성률
        results += "일별: " + calculateAchievementRate(todayDuration, goals.getDailyGoal()) +
                   "% (오늘: " + formatDuration(todayDuration) + " / 목표: " + formatDuration(goals.getDailyGoal()) + ")\n";
        // 주별 목표 달성률
        results += "주별: " + calculateAchievementRate(totalWeeklyDuration, goals.getWeeklyGoal()) +
                   "% (이번 주: " + formatDuration(totalWeeklyDuration) + " / 목표: " + formatDuration(goals.getWeeklyGoal()) + ")\n";
        // 월별 목표 달성률
        results += "월별: " + calculateAchievementRate(totalMonthlyDuration, goals.getMonthlyGoal()) +
                   "% (이번 달: " + formatDuration(totalMonthlyDuration) + " / 목표: " + formatDuration(goals.getMonthlyGoal()) + ")\n";

        resultArea.setText(results);

        return panel;
    }




    private int calculateAchievementRate(Duration achieved, Duration goal) {
        if (goal.isZero()) return 0;  // Avoid division by zero
        double achievedMinutes = achieved.toMinutes();
        double goalMinutes = goal.toMinutes();
        return (int) ((achievedMinutes / goalMinutes) * 100);  // Calculate percentage
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
        long minutes = duration.toMinutesPart();  // Java 9+ method, use duration.toMinutes() % 60 for earlier versions
        return String.format("%02d:%02d", hours, minutes);
    }
    
    private Duration parseDuration(String time) {
        if (time == null || time.isEmpty()) return Duration.ZERO;
        try {
            String[] parts = time.split(":");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            return Duration.ofHours(hours).plusMinutes(minutes);
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse time: " + e.getMessage());
            return Duration.ZERO;
        }
    }

    
    private Map<String, Duration> calculateStudyTimesUntilToday() {
        Map<String, Duration> dailyTotals = new HashMap<>();
        LocalDate today = LocalDate.now();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String recordDate = parts[0];
                String recordTime = parts[1];  // Assuming recordTime is like "HH:mm"
                LocalDate date = LocalDate.parse(recordDate);
                if (date.isBefore(today) || date.isEqual(today)) {
                    Duration time = parseDuration(recordTime);
                    dailyTotals.merge(recordDate, time, Duration::plus);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the study records file: " + e.getMessage());
        }
        return dailyTotals;
    }

    
    
    private Duration calculateTotalWeekly(Map<String, Duration> dailyTotals) {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);
        Duration weeklyTotal = Duration.ZERO;
        for (Map.Entry<String, Duration> entry : dailyTotals.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            if ((date.isAfter(startOfWeek) || date.isEqual(startOfWeek)) && (date.isBefore(endOfWeek) || date.isEqual(endOfWeek))) {
                weeklyTotal = weeklyTotal.plus(entry.getValue());
            }
        }
        return weeklyTotal;
    }
    
    private Duration calculateTotalMonthly(Map<String, Duration> dailyTotals) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        Duration monthlyTotal = Duration.ZERO;
        for (Map.Entry<String, Duration> entry : dailyTotals.entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            if (date.getMonthValue() == currentMonth && date.getYear() == currentYear) {
                monthlyTotal = monthlyTotal.plus(entry.getValue());
            }
        }
        return monthlyTotal;
    }

    
    public String calculateAchievementDisplay() {
        Map<String, Duration> studyTimes = calculateStudyTimesUntilToday();
        StudyGoal goals = loadGoals();

        LocalDate today = LocalDate.now();
        Duration todayDuration = studyTimes.getOrDefault(today.toString(), Duration.ZERO);
        Duration totalWeeklyDuration = calculateTotalWeekly(studyTimes);
        Duration totalMonthlyDuration = calculateTotalMonthly(studyTimes);

        String goalAchievementText = "<html><div style='text-align: center;'>";
        goalAchievementText += formatAchievement("일별", todayDuration, goals.getDailyGoal());
        goalAchievementText += formatAchievement("주별", totalWeeklyDuration, goals.getWeeklyGoal());
        goalAchievementText += formatAchievement("월별", totalMonthlyDuration, goals.getMonthlyGoal());
        goalAchievementText += "</div></html>";

        return goalAchievementText;
    }

    private String formatAchievement(String period, Duration achieved, Duration goal) {
        int achievementRate = calculateAchievementRate(achieved, goal);
        return String.format("%s 목표 달성률: %d%% (달성: %s / 목표: %s)<br>", period, achievementRate, formatDuration(achieved), formatDuration(goal));
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
