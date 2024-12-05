package com.nlu.app.util;

import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeOptionsUtil {
    public static ChromeOptions setupHeadlessChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }
}
