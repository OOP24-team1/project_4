package project4;

public class Assignment {
    private String subjectName;
    private String assignmentName;
    private String dueDate; // YYYY-MM-DD 형식
    private int priority; // 1 ~ 5

    public Assignment(String subjectName, String assignmentName, String dueDate, int priority) {
        this.subjectName = subjectName;
        this.assignmentName = assignmentName;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // 필드 접근을 위한 Getter 메서드
    public String getSubjectName() {
        return subjectName;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    // 과제 데이터를 파일 형식으로 반환
    public String toFileFormat() {
        return String.format("%s|%s|%s|%d", subjectName, assignmentName, dueDate, priority);
    }

    @Override
    public String toString() {
        // 출력 형식: [과목명] - 과제명 (마감기한) <중요도>
        return String.format("[%s] - %s (%s) <%d>", subjectName, assignmentName, dueDate, priority);
    }
}
