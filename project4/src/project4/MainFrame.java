package project4;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame extends JFrame {

    private TimeTablePanel timeTablePanel;
    private Timer alertTimer;
    private JLabel assignmentInfoLabel; // 과제 정보를 표시할 레이블
    private JPanel assignmentInfoPanel;

    public MainFrame() {
        setTitle("스마트 학습 도우미");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout()); // BorderLayout 설정 (패널 위치 지정 가능)

        timeTablePanel = new TimeTablePanel(); // 시간표 관리

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

        homeworkButton.addActionListener(e -> {
            setVisible(false);
            ArrayList<String> subjects = timeTablePanel.getSubjectList();
            JFrame assignmentFrame = new JFrame("과제 관리");
            assignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            assignmentFrame.setSize(600, 400);
            assignmentFrame.add(new AssignmentPanel(subjects));
            assignmentFrame.setVisible(true);
        });

        studyButton.addActionListener(e -> {
            setVisible(false);
            JFrame studyManagementFrame = new JFrame("학습 관리");
            studyManagementFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            studyManagementFrame.setSize(600, 400);
            studyManagementFrame.add(new StudyManagementPanel());
            studyManagementFrame.setVisible(true);
        });

        timetableButton.addActionListener(e -> {
            setVisible(false);
            JFrame timetableFrame = new JFrame("수업 관리");
            timetableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            timetableFrame.setSize(600, 400);
            timetableFrame.add(timeTablePanel);
            timetableFrame.setVisible(true);
        });

        exitButton.addActionListener(e -> {
            if (alertTimer != null) alertTimer.cancel(); // 타이머 종료
            System.exit(0);
        });

        // 버튼 패널과 과제 정보 패널 추가
        add(buttonPanel, BorderLayout.CENTER);
        updateAssignmentInfo(); // 과제 정보 초기화

        setVisible(true);
        startAlertTimer(); // 알림 타이머 시작
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // 과제 정보를 업데이트
    private void updateAssignmentInfo() {
        // AssignmentPanel에서 데이터 가져오기
        AssignmentPanel assignmentPanel = new AssignmentPanel(timeTablePanel.getSubjectList());
        int totalAssignments = assignmentPanel.getTotalAssignments();
        Assignment nearestAssignment = assignmentPanel.getNearestAssignment(); // 가장 가까운 과제 객체 반환

        // 기존 패널 초기화
        if (assignmentInfoPanel != null) {
            remove(assignmentInfoPanel); // 이전 패널 제거
        }

        // 새로운 패널 구성
        assignmentInfoPanel = new JPanel();
        assignmentInfoPanel.setLayout(new BoxLayout(assignmentInfoPanel, BoxLayout.Y_AXIS));
        assignmentInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 여백 추가

        JLabel totalLabel = new JLabel(String.format("전체 과제 개수: %d", totalAssignments));
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        totalLabel.setForeground(Color.BLUE);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nearestLabel = new JLabel();
        nearestLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        nearestLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (nearestAssignment != null) {
            // 과제명과 마감일을 표시
            nearestLabel.setText(String.format(
                "<html><div style='text-align: center;'>"
                + "<b>%s</b> 과제가 <b>%s</b>까지입니다!"
                + "</div></html>",
                nearestAssignment.getAssignmentName(),
                nearestAssignment.getDueDate()
            ));
        } else {
            nearestLabel.setText("남은 과제가 없습니다.");
        }

        // 패널에 추가
        assignmentInfoPanel.add(totalLabel);
        assignmentInfoPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 간격 추가
        assignmentInfoPanel.add(nearestLabel);

        // 메인 프레임에 패널 추가
        add(assignmentInfoPanel, BorderLayout.NORTH); // 상단에 추가
        revalidate(); // 레이아웃 갱신
        repaint(); // 화면 다시 그리기
    }



    // 수업 시작 10분 전 알림 타이머
    private void startAlertTimer() {
        alertTimer = new Timer();
        alertTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ArrayList<ClassSchedule> schedules = timeTablePanel.getSchedules();
                String currentTime = getCurrentTime();
                String currentDay = getCurrentDay();

                for (ClassSchedule schedule : schedules) {
                    String alertTime = calculateAlertTime(schedule.getStartTime());
                    if (currentTime.equals(alertTime) && currentDay.equals(schedule.getDayOfWeek())) {
                        showAlert(schedule);
                    }
                }
            }
        }, 0, 60 * 1000); // 1분마다 실행
    }

    private String getCurrentTime() {
        return java.time.LocalTime.now().toString().substring(0, 5); // HH:mm 형식
    }

    private String calculateAlertTime(String startTime) {
        int hour = Integer.parseInt(startTime.split(":")[0]);
        int minute = Integer.parseInt(startTime.split(":")[1]) - 10;
        if (minute < 0) {
            hour -= 1;
            minute += 60;
        }
        return String.format("%02d:%02d", hour, minute);
    }

    private void showAlert(ClassSchedule schedule) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    "수업 시작 10분 전입니다!\n" +
                    "과목명: " + schedule.getSubjectName() + "\n" +
                    "시간: " + schedule.getStartTime(),
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
    
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

    
}
