package project4;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.awt.event.*;

public class AssignmentPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ArrayList<Assignment> assignments;
    private final String FILE_NAME = "assignment.txt";
    private JComboBox<String> subjectComboBox; // 과목명 드롭다운

    public AssignmentPanel(ArrayList<String> subjects) {
        assignments = new ArrayList<>();
        loadAssignmentsFromFile();

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JButton addButton = createButton("과제 추가");
        JButton listButton = createButton("과제 목록 보기");
        JButton backButton = createButton("뒤로가기");

        mainPanel.add(addButton);
        mainPanel.add(listButton);
        mainPanel.add(backButton);

        addButton.addActionListener(e -> showPanel("AddPanel"));
        listButton.addActionListener(e -> showPanel("ListPanel"));
        backButton.addActionListener(e -> goBackToMain());

        add(mainPanel, "MainMenu");
        add(createAddPanel(subjects), "AddPanel");
        add(createListPanel(), "ListPanel");

        cardLayout.show(this, "MainMenu");
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JPanel createAddPanel(ArrayList<String> subjects) {
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(5, 2, 10, 10));

        // 과목명 드롭다운 초기화
        subjectComboBox = new JComboBox<>(subjects.toArray(new String[0]));

        JTextField assignmentField = new JTextField();
        JTextField dueDateField = new JTextField();
        JTextField priorityField = new JTextField();
        JButton saveButton = createButton("저장");
        JButton backButton = createButton("뒤로가기");

        addPanel.add(new JLabel("과목명:"));
        addPanel.add(subjectComboBox);
        addPanel.add(new JLabel("과제명:"));
        addPanel.add(assignmentField);
        addPanel.add(new JLabel("마감기한 (YYYY-MM-DD):"));
        addPanel.add(dueDateField);
        addPanel.add(new JLabel("중요도 (1~5):"));
        addPanel.add(priorityField);
        addPanel.add(saveButton);
        addPanel.add(backButton);

        saveButton.addActionListener(e -> {
            try {
                String subjectName = (String) subjectComboBox.getSelectedItem();
                String assignmentName = assignmentField.getText();
                String dueDate = dueDateField.getText();
                int priority = Integer.parseInt(priorityField.getText());

                if (assignmentName.isEmpty() || dueDate.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "모든 필드를 입력해주세요!");
                    return;
                }

                if (!isValidDate(dueDate)) {
                    JOptionPane.showMessageDialog(null, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요.");
                    return;
                }

                if (priority < 1 || priority > 5) {
                    JOptionPane.showMessageDialog(null, "중요도는 1에서 5 사이의 숫자여야 합니다.");
                    return;
                }

                assignments.add(new Assignment(subjectName, assignmentName, dueDate, priority));
                saveAssignmentsToFile();
                JOptionPane.showMessageDialog(null, "과제가 추가되었습니다!");

                assignmentField.setText("");
                dueDateField.setText("");
                priorityField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "중요도는 1에서 5 사이의 숫자로 입력해주세요.");
            }
        });

        backButton.addActionListener(e -> showPanel("MainMenu"));

        return addPanel;
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("과제 목록:");
        JList<String> assignmentList = new JList<>(new DefaultListModel<>());
        JScrollPane scrollPane = new JScrollPane(assignmentList);
        JButton backButton = createButton("뒤로가기");

        listPanel.add(titleLabel, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(backButton, BorderLayout.SOUTH);

        backButton.addActionListener(e -> showPanel("MainMenu"));

        listPanel.addHierarchyListener(e -> {
            if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED && listPanel.isShowing()) {
                updateList(assignmentList);
            }
        });

        assignmentList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // 더블 클릭
                    String selected = assignmentList.getSelectedValue();
                    if (selected != null) {
                        int choice = JOptionPane.showConfirmDialog(
                                null,
                                "선택한 과제 [" + selected + "]를 완료 처리하시겠습니까?",
                                "과제 완료 처리",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            assignments.removeIf(a -> a.toString().equals(selected));
                            saveAssignmentsToFile();
                            JOptionPane.showMessageDialog(null, "과제가 완료 처리되었습니다.");
                            updateList(assignmentList);
                        }
                    }
                }
            }
        });

        return listPanel;
    }

    private void updateList(JList<String> list) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (Assignment assignment : assignments) {
            model.addElement(assignment.toString());
        }
        list.setModel(model);
    }
    
 // 전체 과제 개수를 반환
    public int getTotalAssignments() {
        return assignments.size();
    }
    
 // 마감기한이 가장 가까운 과제를 반환 (Assignment 객체)
    public Assignment getNearestAssignment() {
        if (assignments.isEmpty()) {
            return null; // 과제가 없으면 null 반환
        }

        Date now = new Date(); // 현재 날짜 및 시간

        // 마감기한이 지나지 않은 과제 중 가장 가까운 마감기한 찾기
        return assignments.stream()
                .filter(assignment -> parseDate(assignment).after(now) || isSameDay(parseDate(assignment), now)) // 마감기한이 지나지 않은 과제만 필터
                .min(Comparator.comparing(this::parseDate)) // 마감기한 기준으로 정렬
                .orElse(null);
    }

    


    // 같은 날인지 비교
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date1).equals(dateFormat.format(date2));
    }


    // 문자열 형태의 날짜를 Date 객체로 변환
    private Date parseDate(Assignment assignment) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(assignment.getDueDate());
        } catch (ParseException e) {
            // 날짜 형식이 올바르지 않은 경우 현재 날짜 반환 (비정상 데이터 처리)
            return new Date();
        }
    }
    
    private void saveAssignmentsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Assignment assignment : assignments) {
                writer.write(assignment.toFileFormat());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void loadAssignmentsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                String subjectName = parts[0];
                String assignmentName = parts[1];
                String dueDate = parts[2];
                int priority = Integer.parseInt(parts[3]);
                assignments.add(new Assignment(subjectName, assignmentName, dueDate, priority));
            }
        } catch (IOException e) {
            System.out.println("파일을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void showPanel(String name) {
        cardLayout.show(this, name);
    }

    private void goBackToMain() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose();
        new MainFrame();
    }
}
