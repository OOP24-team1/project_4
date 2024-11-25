package project4;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StudyGoal {
    private Duration dailyGoal;
    private Duration weeklyGoal;
    private Duration monthlyGoal;

    // Constructor accepting Duration objects for each type of goal.
    public StudyGoal(Duration dailyGoal, Duration weeklyGoal, Duration monthlyGoal) {
        this.dailyGoal = dailyGoal;
        this.weeklyGoal = weeklyGoal;
        this.monthlyGoal = monthlyGoal;
    }

    public Duration getDailyGoal() {
        return dailyGoal;
    }

    public Duration getWeeklyGoal() {
        return weeklyGoal;
    }

    public Duration getMonthlyGoal() {
        return monthlyGoal;
    }

    public String toFileFormat() {
        // Convert Duration to a string format that can be saved to file.
        return formatDuration(dailyGoal) + "|" + formatDuration(weeklyGoal) + "|" + formatDuration(monthlyGoal);
    }

    // Helper method to format Duration to a string (e.g., "HH:mm")
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart(); // Java 9+ method, use duration.toMinutes() % 60 for earlier versions
        return String.format("%02d:%02d", hours, minutes);
    }

    // Static method to parse a formatted duration string back into a Duration object.
    public static Duration parseDuration(String time) {
        if (time.isEmpty()) return Duration.ZERO;
        try {
            String[] parts = time.split(":");
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            return Duration.ofHours(hours).plusMinutes(minutes);
        } catch (Exception e) {
            return Duration.ZERO;  // Return Duration.ZERO if parsing fails
        }
    }
}
