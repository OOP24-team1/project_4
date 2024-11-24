package project4;

public class ClassSchedule {
    private String subjectName;  // 과목명
    private String dayOfWeek;   // 요일
    private String startTime;   // 시작 시간 (HH:mm)
    private String endTime;     // 종료 시간 (HH:mm)

    public ClassSchedule(String subjectName, String dayOfWeek, String startTime, String endTime) {
        this.subjectName = subjectName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String toFileFormat() {
        return String.format("%s|%s|%s|%s", subjectName, dayOfWeek, startTime, endTime);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s-%s)", dayOfWeek, subjectName, startTime, endTime);
    }
}
