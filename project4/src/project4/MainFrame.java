package project4;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
//ScheduledExecutorService를 위한 라이브러리 
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFrame extends JFrame {

    private boolean isDarkMode = false; // 다크 모드 상태

    private TimeTablePanel timeTablePanel;
    private Timer alertTimer; // 수업 10분 전 알림을 할 타이머
    private JLabel assignmentInfoLabel; // 과제 정보를 표시할 레이블
    private JLabel goalAchievementLabel; // 학습 목표 달성률을 표시할 레이블
    private JPanel infoPanel; // 과제 정보와 학습 목표 달성률을 표시할 패널
    private JPanel assignmentInfoPanel; // 과제 정보 패널을 클래스 멤버로 선언
    private ScheduledExecutorService scheduler;
    
    public MainFrame() {
    	isDarkMode = SettingsManager.loadDarkModeState(); // 다크 모드 상태 로드
        setLookAndFeel(isDarkMode);
        setTitle("스마트 학습 도우미");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout()); // BorderLayout 설정 (패널 위치 지정 가능)

        timeTablePanel = new TimeTablePanel(); // 시간표 관리
        setupInfoPanel(); // 정보 패널 설정
        setupButtonPanel(); // 버튼 패널 설정
        
     // 다크 모드 스위치 추가
        setupDarkModeToggle();

        setVisible(true);
     // 스케줄러 초기화
        scheduler = Executors.newScheduledThreadPool(1);

        startAlertTask(); // 알림 작업 시작
    }
    
    // 다른 메뉴에서 메인프레임으로 돌아올 때 사용할 생성
    public MainFrame(boolean isDarkMode) {
    	this.isDarkMode = isDarkMode; // 전달받은 다크 모드 상태 저장
        setLookAndFeel(isDarkMode);
        setTitle("스마트 학습 도우미");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout()); // BorderLayout 설정 (패널 위치 지정 가능)

        timeTablePanel = new TimeTablePanel(); // 시간표 관리
        setupInfoPanel(); // 정보 패널 설정
        setupButtonPanel(); // 버튼 패널 설정
        
     // 다크 모드 스위치 추가
        setupDarkModeToggle();

        setVisible(true);
     // 스케줄러 초기화
        scheduler = Executors.newScheduledThreadPool(1);

        startAlertTask(); // 알림 작업 시작
    }
    
    private void setupInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // 마진 설정

        assignmentInfoLabel = new JLabel();
        goalAchievementLabel = new JLabel();

        assignmentInfoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        goalAchievementLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        updateAssignmentInfo(); // 과제 정보 초기화
        updateGoalAchievementInfo(); // 학습 목표 달성률 업데이트

        JSeparator separator = new JSeparator(); // 구분선 추가
        separator.setForeground(Color.DARK_GRAY);

        // 레이블 추가
        infoPanel.add(assignmentInfoLabel, gbc);
        infoPanel.add(separator, gbc); // 구분선 위치 조정
        infoPanel.add(goalAchievementLabel, gbc);

        // 공백 추가: 추가적인 공간을 만들기 위해 vertical strut을 사용
        Component verticalStrut = Box.createVerticalStrut(20); // 20픽셀의 수직 공백
        gbc = new GridBagConstraints(); // 새 GridBagConstraints 객체 초기화
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0; 
        infoPanel.add(verticalStrut, gbc);  // 공백 패널에 추가

        add(infoPanel, BorderLayout.NORTH);
    }



    private void setupButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton homeworkButton = createButton("과제 관리");
        JButton studyButton = createButton("학습 관리");
        JButton timetableButton = createButton("수업 관리");
        JButton exitButton = createButton("종료");

        buttonPanel.add(homeworkButton);
        buttonPanel.add(studyButton);
        buttonPanel.add(timetableButton);
        buttonPanel.add(exitButton);

        homeworkButton.addActionListener(e -> showAssignmentManagement());
        studyButton.addActionListener(e -> showStudyManagement());
        timetableButton.addActionListener(e -> showTimeTableManagement());
        exitButton.addActionListener(e -> exitApplication()); // 람다식 대신 메서드 참조 수정

        add(buttonPanel, BorderLayout.CENTER);
    }



    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }


    private void updateGoalAchievementInfo() {
        StudyManagementPanel studyPanel = new StudyManagementPanel();
        String goalAchievementText = studyPanel.calculateAchievementDisplay();
        goalAchievementLabel.setText(goalAchievementText);
        goalAchievementLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
    }

    private void showAssignmentManagement() {
        dispose(); // 현재 메인 프레임 닫기
        ArrayList<String> subjects = timeTablePanel.getSubjectList();
        JFrame assignmentFrame = new JFrame("과제 관리");
        assignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        assignmentFrame.setSize(600, 400);
        assignmentFrame.add(new AssignmentPanel(subjects));
        assignmentFrame.setVisible(true);
    }

    private void showStudyManagement() {
        dispose(); // 현재 메인 프레임 닫기
        JFrame studyManagementFrame = new JFrame("학습 관리");
        studyManagementFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        studyManagementFrame.setSize(600, 400);
        studyManagementFrame.add(new StudyManagementPanel());
        studyManagementFrame.setVisible(true);
    }

    private void showTimeTableManagement() {
        dispose(); // 현재 메인 프레임 닫기
        JFrame timetableFrame = new JFrame("수업 관리");
        timetableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        timetableFrame.setSize(600, 400);
        timetableFrame.add(new TimeTablePanel());
        timetableFrame.setVisible(true);
    }


    private void exitApplication() {
        if (alertTimer != null) alertTimer.cancel();
        System.exit(0);
    }
    
    
    // 수업 10분 전 알림 실행
    private void startAlertTask() {
        scheduler.scheduleAtFixedRate(() -> {
            ArrayList<ClassSchedule> schedules = timeTablePanel.getSchedules();
            String currentTime = getCurrentTime();
            String currentDay = getCurrentDay();

            for (ClassSchedule schedule : schedules) {
                String alertTime = calculateAlertTime(schedule.getStartTime());
                if (currentTime.equals(alertTime) && currentDay.equals(schedule.getDayOfWeek())) {
                    SwingUtilities.invokeLater(() -> showAlert(schedule)); // UI 작업은 SwingUtilities 사용
                }
            }
        }, 0, 1, TimeUnit.MINUTES); // 1분마다 실행
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (scheduler != null) {
            scheduler.shutdown(); // 스케줄러 종료
        }
    }
    
    // 현재 시간 구하는 메소드
    private String getCurrentTime() {
        return java.time.LocalTime.now().toString().substring(0, 5); // HH:mm 형식
    }

    // 해당 요일의 수업에 대해 10분 전 알림을 하기 위해 현재 요일 구하는 메소드
    private String getCurrentDay() {
        java.time.DayOfWeek dayOfWeek = java.time.LocalDate.now().getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "월요일";
            case TUESDAY: return "화요일";
            case WEDNESDAY: return "수요일";
            case THURSDAY: return "목요일";
            case FRIDAY: return "금요일";
            case SATURDAY: return "토요일";
            case SUNDAY: return "일요일";
            default: return "";
        }
    }
    
    // 수업 10분 전 알림 시간 구하는 메소드
    private String calculateAlertTime(String startTime) {
        int hour = Integer.parseInt(startTime.split(":")[0]);
        int minute = Integer.parseInt(startTime.split(":")[1]) - 10;
        if (minute < 0) {
            hour -= 1;
            minute += 60;
        }
        return String.format("%02d:%02d", hour, minute);
    }
    
    
    // 수업 10분 전 알림 창 띄우는 메소드
    private void showAlert(ClassSchedule schedule) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "수업 시작 10분 전입니다!\n" +
                "과목명: " + schedule.getSubjectName() + "\n" +
                "시간: " + schedule.getStartTime(),
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    // 메인프레임 상단 과제 정보 출력하는 메소드
    private void updateAssignmentInfo() {
        AssignmentPanel assignmentPanel = new AssignmentPanel(timeTablePanel.getSubjectList()); // 이 부분은 필요에 따라 조정
        int totalAssignments = assignmentPanel.getTotalAssignments(); // 과제 총 개수 가져오기
        Assignment nearestAssignment = assignmentPanel.getNearestAssignment(); // 가장 마감기한이 임박한 과제 가져오기

        // 이전 정보가 있는 경우 UI에서 제거
        if (assignmentInfoPanel != null) {
            assignmentInfoPanel.removeAll();
        } else {
            assignmentInfoPanel = new JPanel();
            assignmentInfoPanel.setLayout(new BoxLayout(assignmentInfoPanel, BoxLayout.Y_AXIS));
            assignmentInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        JLabel totalLabel = new JLabel(String.format("          %d개의 과제가 있습니다", totalAssignments));
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        totalLabel.setForeground(Color.RED);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nearestLabel = new JLabel();
        nearestLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nearestLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (nearestAssignment != null) {
            nearestLabel.setText(String.format("<html><div style='text-align: center;'>"
                                               + "<b>%s</b> 과제가 <b>%s</b>까지입니다!</div></html>",
                                               nearestAssignment.getAssignmentName(),
                                               nearestAssignment.getDueDate()));
        } else {
            nearestLabel.setText("남은 과제가 없습니다.");
        }

        assignmentInfoPanel.add(totalLabel);
        assignmentInfoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        assignmentInfoPanel.add(nearestLabel);

        infoPanel.add(assignmentInfoPanel);
        revalidate();
        repaint();
    }
    
    
    
 // 다크모드 관
    private void setupDarkModeToggle() {
        JPanel togglePanel = new JPanel();
        togglePanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // 가운데 정렬

        JButton toggleModeButton = new JButton("모드 전환");
        toggleModeButton.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            setLookAndFeel(isDarkMode);

            // 상태 저장
            SettingsManager.saveDarkModeState(isDarkMode);
        });

        togglePanel.add(toggleModeButton);

        // SOUTH 대신 CENTER에 추가
        add(togglePanel, BorderLayout.SOUTH);
    }

    
    private void setLookAndFeel(boolean isDarkMode) {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            } else {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

}
