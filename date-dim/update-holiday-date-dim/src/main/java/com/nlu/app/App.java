package com.nlu.app;

import com.nlu.app.service.DateHolidayService;
import com.nlu.app.util.ChoiceCheckerUtil;
import com.nlu.app.util.DateCheckerUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Hello world!
 */
public class App {
    private static final DateHolidayService dateHolidayService = new DateHolidayService();

    public static void main(String[] args) throws Exception {
        System.out.println("Welcome to Update Holiday Date Dim!");
        System.out.println("Valid date for this application: 1-1-2024 to 31-12-2025.");
        System.out.println("----------------------------------------------------");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String nowChoice = getChoiceFromUser(reader);

            if (nowChoice == null) {
                break;      // Exit
            }

            switch (nowChoice) {
                case "1":
                    if (!processDateInput(reader)) {
                        break; // Quay lại vòng lặp nhập lựa chọn
                    }
                    break;
                case "2":
                    if (!checkDate(reader)) {
                        break; // Quay lại vòng lặp nhập lựa chọn
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please select '1' or '2'.\n");
            }
        }
        System.out.println("Program terminated.");
    }

    private static String getChoiceFromUser(BufferedReader reader) throws Exception {
        while (true) {
            displayOption();
            System.out.print("-> Enter your choice (type 'rules' for help or 'exit' to quit): ");
            String choiceInput = reader.readLine().trim();

            if (choiceInput.equalsIgnoreCase("exit")) {
                System.out.println("Application is exited.");
                return null; // Kết thúc toàn bộ chương trình
            }

            if (choiceInput.equalsIgnoreCase("rules")) {
                displayTotalRules();
                continue;
            }

            if (choiceInput.isEmpty()) {
                System.out.println("Please provide a valid choice.\n");
                continue;
            }

            if (!ChoiceCheckerUtil.isValidChoice(choiceInput)) {
                System.out.println("Please input a valid choice (1 or 2).\n");
                continue;
            }

            return choiceInput;
        }
    }

    private static boolean processDateInput(BufferedReader reader) throws Exception {
        while (true) {
            System.out.print("-> Enter a date (dd-MM-yyyy flag), annual date (dd-MM-yyyy flag) or range of dates (dd-MM-yyyy dd-MM-yyyy flag) " +
                    "(Type 'rules' for help, 'back' to back to menu function or 'exit' to quit): ");
            String updateInput = reader.readLine().trim();

            if (updateInput.equalsIgnoreCase("exit")) {
                System.out.println("Program terminated.");
                System.exit(0);
            }

            if (updateInput.equalsIgnoreCase("rules")) {
                displayTotalRules();
                continue;
            }

            if (updateInput.equalsIgnoreCase("back")) {
                System.out.println("Back to choice menu.\n");
                return true; // Quay lại lựa chọn chức năng
            }

            if (updateInput.isEmpty()) {
                System.out.println("Please provide a valid date/range of dates.\n");
                continue;
            }

            if (!DateCheckerUtil.isValidUpdateHoliday(updateInput)) {
                System.out.println("Please provide a valid date/range of dates.\n");
                continue;
            }

            // Đã qua được bước kiểm tra thì dữ liệu hợp lệ để lưu
            String[] template = updateInput.split(" ");
            // Có 2 element thì có dạng "dd-MM-yyyy flag" hoặc "dd-MM flag". flag là true hoặc false của is_holiday
            if (template.length == 2) {
                // Đếm số lượng ký tự '-' trong chuỗi input
                long count = template[0].chars()
                        .filter(ch -> ch == '-')
                        .count();

                if (count == 1) {
                    dateHolidayService.updateAnnualHolidays(updateInput);
                } else if (count == 2)  {
                    dateHolidayService.updateFullDateHoliday(updateInput);
                }
            } else if (template.length == 3) {
                // Có 3 element thì có dạng "dd-MM-yyyy dd-MM-yyyy flag"
                dateHolidayService.updateRangeHolidays(updateInput);
            }
        }
    }

    private static boolean checkDate(BufferedReader reader) throws Exception {
        while (true) {
            System.out.print("-> Enter 'all_holidays', a date (dd-MM-yyyy), annual date (dd-MM-yyyy) or range of dates (dd-MM-yyyy dd-MM-yyyy) " +
                    "(Type 'rules' for help, 'back' to back to menu function or 'exit' to quit): ");
            String checkInput = reader.readLine().trim();

            if (checkInput.equalsIgnoreCase("exit")) {
                System.out.println("Program terminated.");
                System.exit(0);
            }

            if (checkInput.equalsIgnoreCase("rules")) {
                displayTotalRules();
                continue;
            }

            if (checkInput.equalsIgnoreCase("back")) {
                System.out.println("Back to choice menu.\n");
                return true; // Quay lại lựa chọn chức năng
            }

            if (checkInput.equalsIgnoreCase("all_holidays")) {
                dateHolidayService.getAllHolidays();
                continue;
            }

            if (checkInput.isEmpty()) {
                System.out.println("Please provide a valid date/range of dates.\n");
                continue;
            }

            if (!DateCheckerUtil.isValidCheckHoliday(checkInput)) {
                System.out.println("Please provide a valid date/range of dates.\n");
                continue;
            }
            // Đã qua được bước kiểm tra thì dữ liệu hợp lệ để kiểm tra
            String[] template = checkInput.split(" ");
            // Có 1 phần thì có dạng "dd-MM-yyyy" hoặc "dd-MM".
            if (template.length == 1) {
                // Đếm số lượng ký tự '-' trong chuỗi input
                long count = template[0].chars()
                        .filter(ch -> ch == '-')
                        .count();

                if (count == 1) {
                    dateHolidayService.getAnnualHoliday(checkInput);
                } else if (count == 2)  {
                    dateHolidayService.getFullDateHoliday(checkInput);
                }
            } else if (template.length == 2) {
                // Có 2 phần thì có dạng "dd-MM-yyyy dd-MM-yyyy"
                dateHolidayService.getRangeHolidays(checkInput);
            }

        }
    }


    private static void displayTotalRules() {
        System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Rules: " +
                "\n1. Choose function" +
                "\n\t1.1. 1 to update holiday dates" +
                "\n\t1.2. 2 to check holiday dates" +
                "\n----------------------------------------------------------------------------------------" +
                "\n2. Update holiday dates" +
                "\nNote 1: flag is true/false. true is holiday, false is not holiday" +
                "\nNote 2: Valid date for this application: 1-1-2024 to 31-12-2025. Out of this date is not accepted." +
                "\n\n\t2.1. Input date by format: 'dd-MM-yyyy flag' (not contain '') to set this date is/is not holiday. Ex: 01-01-2024 true is valid, 1-1-2024 true is not valid." +
                "\n\t2.2. Input date by format: 'dd-MM-yyyy dd-MM-yyyy flag' (not contain '', first date before last date) to set range of dates are holiday. Ex: 01-01-2024 05-01-2024 true is valid." +
                "\n\t2.3. Input date by format: 'dd-MM flag' (not contain '') to set this date is holiday for each year. Ex: 01-01 true is valid, 1-1 true is not valid." +
                "\n----------------------------------------------------------------------------------------" +
                "\n3. Check dates:" +
                "\nNote: Valid date for this application: 1-1-2024 to 31-12-2025. Out of this date is not accepted." +
                "\n\t3.1. Input 'all_holidays' to get all holiday dates (split by year, not contain '', not check case)" +
                "\n\t3.2. Input date by format: 'dd-MM-yyyy' (not contain '') to get this date information. Ex: 01-01-2024 is valid, 1-1-2024 is not valid." +
                "\n\t3.3. Input date by format: 'dd-MM-yyyy dd-MM-yyyy' (not contain '', first date before last date) to get range of dates information. Ex: 01-01-2024 05-01-2024 is valid." +
                "\n\t3.4. Input date by format: 'dd-MM' (not contain '') to get this date information for each year. Ex: 01-01 is valid, 1-1 is not valid." +
                "\n----------------------------------------------------------------------------------------" +
                "\n4. Input 'exit' to exit program. (not contain '', not check case)" +
                "\n5. Input 'rules' to display rules. (not contain '', not check case)" +
                "\n6. When in choice 1 or 2, input 'back' to go back to menu choice function. (not contain '', not check case)");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
    }

    private static void displayOption() {
        System.out.println("Input 1: Update holiday dates.");
        System.out.println("Input 2: Check holiday dates.");
    }
}
