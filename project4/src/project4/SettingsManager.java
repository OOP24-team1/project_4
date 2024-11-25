package project4;

import java.io.*;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.txt";

    // 상태 저장
    public static void saveDarkModeState(boolean isDarkMode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SETTINGS_FILE))) {
            writer.write(Boolean.toString(isDarkMode));
        } catch (IOException e) {
            System.out.println("설정 저장 중 오류 발생: " + e.getMessage());
        }
    }

    // 상태 불러오기
    public static boolean loadDarkModeState() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String state = reader.readLine();
            return Boolean.parseBoolean(state);
        } catch (IOException e) {
            // 기본 값 (false) 반환
            return false;
        }
    }
}