package com.nlu.app.service;

import com.nlu.app.util.ChromeOptionsUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DataCrawlService {
    public String crawlInfoHtml(String productLink) throws InterruptedException {
        ChromeOptions options = ChromeOptionsUtil.setupHeadlessChromeOptions();
        ChromeDriver driver = new ChromeDriver(options);

        try {
            // Khởi tạo ChromeDriver với ChromeOptions
            driver.get(productLink);
            Actions actions = new Actions(driver);

            // Cuộn trang xuống bằng phím Page Down
            for (int i = 0; i < 5; i++) { // Cuộn xuống 5 lần
                actions.sendKeys(Keys.PAGE_DOWN).perform();
                Thread.sleep(500); // Tạm dừng một chút để trang kịp tải nội dung mới
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            WebElement buttonBlock = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cps-block-technicalInfo")));
            actions.scrollToElement(buttonBlock);
            var button = driver.findElements(By.cssSelector(".cps-block-technicalInfo .button__show-modal-technical")).get(0);
            actions.moveToElement(button).click().perform();
            var modalContent = driver.findElement(By.cssSelector(".technical-content-modal"));
            return modalContent.getAttribute("outerHTML");
        } finally {
            driver.quit();
        }
    }
}