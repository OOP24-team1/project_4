package project4;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.awt.event.*;

public class TimeTablePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ArrayList<ClassSchedule> schedules;
    private final String FILE_NAME = "timetable.txt";

    public TimeTablePanel() {
        schedules = new ArrayList<>();
        loadSchedulesFromFile();

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JButton addButton = createButton("수업 추가");
        JButton viewButton = createButton("시간표 확인");
        JButton backButton = createButton("뒤로가기");

        mainPanel.add(addButton);
        mainPanel.add(viewButton);
        mainPanel.add(backButton);

        addButton.addActionListener(e -> showPanel("AddPanel"));
        viewButton.addActionListener(e -> showPanel("ViewPanel"));
        backButton.addActionListener(e -> goBackToMain());

        add(mainPanel, "MainMenu");
        add(createAddPanel(), "AddPanel");
        add(createViewPanel(), "ViewPanel");

        cardLayout.show(this, "MainMenu");
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField subjectField = new JTextField();
        JComboBox<String> dayCombo = createDayDropdown();
        JComboBox<String> startTimeCombo = createTimeDropdown();
        JComboBox<String> endTimeCombo = createTimeDropdown();
        JButton saveButton = createButton("저장");
        JButton backButton = createButton("뒤로가기");

        addPanel.add(new JLabel("과목명:"));
        addPanel.add(subjectField);
        addPanel.add(new JLabel("요일:"));
        addPanel.add(dayCombo);
        addPanel.add(new JLabel("시작 시간:"));
        addPanel.add(startTimeCombo);
        addPanel.add(new JLabel("종료 시간:"));
        addPanel.add(endTimeCombo);
        addPanel.add(saveButton);
        addPanel.add(backButton);

        saveButton.addActionListener(e -> {
            try {
                String subjectName = subjectField.getText();
                String dayOfWeek = (String) dayCombo.getSelectedItem();
                String startTime = (String) startTimeCombo.getSelectedItem();
                String endTime = (String) endTimeCombo.getSelectedItem();

                if (!isValidTimeRange(startTime, endTime)) {
                    JOptionPane.showMessageDialog(null, "종료 시간은 시작 시간 이후여야 합니다.");
                    return;
                }

                if (isOverlapping(dayOfWeek, startTime, endTime)) {
                    JOptionPane.showMessageDialog(null, "같은 요일에 시간이 겹치는 수업이 있습니다.");
                    return;
                }

                schedules.add(new ClassSchedule(subjectName, dayOfWeek, startTime, endTime));
                saveSchedulesToFile();
                JOptionPane.showMessageDialog(null, "수업이 추가되었습니다!");

                subjectField.setText("");
                dayCombo.setSelectedIndex(0);
                startTimeCombo.setSelectedIndex(0);
                endTimeCombo.setSelectedIndex(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "올바른 입력 값을 확인하세요.");
            }
        });

        backButton.addActionListener(e -> showPanel("MainMenu"));

        return addPanel;
    }

    private JPanel createViewPanel() {
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());

        String[] columnNames = {"시간", "월요일", "화요일", "수요일", "목요일", "금요일"};
        String[][] data = new String[10][6];
        JTable table = new JTable(data, columnNames);
        table.setRowHeight(40);
        table.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton backButton = createButton("뒤로가기");
        backButton.addActionListener(e -> showPanel("MainMenu"));

        viewPanel.add(scrollPane, BorderLayout.CENTER);
        viewPanel.add(backButton, BorderLayout.SOUTH);

        viewPanel.addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED && viewPanel.isShowing()) {
                updateTableData(table);
            }
        });

        // 셀 클릭 이벤트 추가
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());

                if (row >= 0 && col > 0) {
                    String dayOfWeek = getDayOfWeekFromColumn(col);
                    String time = table.getValueAt(row, 0).toString();

                    // 셀 값 확인 (수업이 있는지 없는지 판단)
                    if (table.getValueAt(row, col) == null) { // 빈 칸
                        int choice = JOptionPane.showConfirmDialog(
                                null,
                                "이 시간에 수업을 추가하시겠습니까?",
                                "수업 추가",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            navigateToAddPanel(dayOfWeek, time);
                        }
                    } else { // 수업이 있는 칸
                        ClassSchedule selectedSchedule = findSchedule(dayOfWeek, time);
                        if (selectedSchedule != null) {
                            int choice = JOptionPane.showConfirmDialog(
                                    null,
                                    "수업 [" + selectedSchedule.getSubjectName() + "]을(를) 삭제하시겠습니까?",
                                    "수업 삭제",
                                    JOptionPane.YES_NO_OPTION
                            );
                            if (choice == JOptionPane.YES_OPTION) {
                                schedules.remove(selectedSchedule);
                                saveSchedulesToFile();
                                updateTableData(table);
                                JOptionPane.showMessageDialog(null, "수업이 삭제되었습니다.");
                            }
                        }
                    }
                }
            }
        });

        return viewPanel;
    }


    private JComboBox<String> createDayDropdown() {
        return new JComboBox<>(new String[]{"월요일", "화요일", "수요일", "목요일", "금요일"});
    }

    private JComboBox<String> createTimeDropdown() {
        JComboBox<String> comboBox = new JComboBox<>();
        for (int i = 9; i <= 18; i++) {
            comboBox.addItem(String.format("%02d:00", i));
        }
        return comboBox;
    }

    private boolean isValidTimeRange(String startTime, String endTime) {
        return startTime.compareTo(endTime) < 0;
    }

    private boolean isOverlapping(String day, String start, String end) {
        for (ClassSchedule schedule : schedules) {
            if (schedule.getDayOfWeek().equalsIgnoreCase(day)) {
                if (!(end.compareTo(schedule.getStartTime()) <= 0 || start.compareTo(schedule.getEndTime()) >= 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateTableData(JTable table) {
        String[][] updatedData = new String[10][6];
        for (int i = 0; i < 10; i++) {
            updatedData[i][0] = String.format("%02d:00", 9 + i);
        }

        for (ClassSchedule schedule : schedules) {
            int startRow = Integer.parseInt(schedule.getStartTime().split(":")[0]) - 9;
            int endRow = Integer.parseInt(schedule.getEndTime().split(":")[0]) - 9;
            int col = getColumnIndex(schedule.getDayOfWeek());

            for (int row = startRow; row < endRow; row++) {
                if (row >= 0 && row < 10 && col > 0) {
                    updatedData[row][col] = schedule.getSubjectName();
                }
            }
        }

        table.setModel(new javax.swing.table.DefaultTableModel(updatedData, new String[]{"시간", "월요일", "화요일", "수요일", "목요일", "금요일"}));
    }
    
    // 요일 문자열을 열 인덱스로 변환하는 메서드
    private int getColumnIndex(String dayOfWeek) {
        switch (dayOfWeek) {
            case "월요일": return 1;
            case "화요일": return 2;
            case "수요일": return 3;
            case "목요일": return 4;
            case "금요일": return 5;
            default: return -1; // 없는 요일에 대한 기본값
        }
    }


    private String getDayOfWeekFromColumn(int col) {
        switch (col) {
            case 1: return "월요일";
            case 2: return "화요일";
            case 3: return "수요일";
            case 4: return "목요일";
            case 5: return "금요일";
            default: return null;
        }
    }

    private ClassSchedule findSchedule(String dayOfWeek, String time) {
        for (ClassSchedule schedule : schedules) {
            if (schedule.getDayOfWeek().equals(dayOfWeek) && schedule.getStartTime().equals(time)) {
                return schedule;
            }
        }
        return null;
    }

    private void saveSchedulesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (ClassSchedule schedule : schedules) {
                writer.write(schedule.toFileFormat());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void loadSchedulesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                schedules.add(new ClassSchedule(parts[0], parts[1], parts[2], parts[3]));
            }
        } catch (IOException e) {
            System.out.println("파일 로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void showPanel(String name) {
        cardLayout.show(this, name);
    }

    private void goBackToMain() {
    	JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // 현재 창 닫기

        // 다크 모드 상태 전달
        boolean isDarkMode = SettingsManager.loadDarkModeState();
        new MainFrame(isDarkMode); // 메인 프레임 열기
    }
    
    private void navigateToAddPanel(String dayOfWeek, String startTime) {
        JPanel addPanel = (JPanel) getComponent(1); // AddPanel 가져오기
        JComboBox<String> dayCombo = (JComboBox<String>) addPanel.getComponent(3); // 요일 드롭다운
        JComboBox<String> startTimeCombo = (JComboBox<String>) addPanel.getComponent(5); // 시작 시간 드롭다운

        // 요일과 시작 시간 설정
        dayCombo.setSelectedItem(dayOfWeek);
        startTimeCombo.setSelectedItem(startTime);

        // 종료 시간 기본값 설정 (1시간 후)
        JComboBox<String> endTimeCombo = (JComboBox<String>) addPanel.getComponent(7);
        int nextHour = Integer.parseInt(startTime.split(":")[0]) + 1;
        if (nextHour <= 18) {
            endTimeCombo.setSelectedItem(String.format("%02d:00", nextHour));
        }

        // AddPanel로 이동
        showPanel("AddPanel");
    }
    
    // assignment에서 과목명을 시간표에서 가져오기 위한 함수
    public ArrayList<String> getSubjectList() {
        ArrayList<String> subjects = new ArrayList<>();
        for (ClassSchedule schedule : schedules) { // schedules는 TimeTablePanel의 수업 리스트
            if (!subjects.contains(schedule.getSubjectName())) {
                subjects.add(schedule.getSubjectName());
            }
        }
        return subjects;
    }

    public ArrayList<ClassSchedule> getSchedules() {
        return schedules; // 모든 수업 정보를 반환
    }


}
