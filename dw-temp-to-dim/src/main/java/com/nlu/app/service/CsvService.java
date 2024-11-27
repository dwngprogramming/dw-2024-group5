package com.nlu.app.service;

import com.nlu.app.util.ChromeOptionsUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public class CsvService {
    private final DataCrawlService dataCrawlService = new DataCrawlService();

    public boolean writeCrawlHtmlToCsvFile(String csvPath) {
        ChromeOptions options = ChromeOptionsUtil.setupHeadlessChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        Actions actions = new Actions(driver);
        driver.get("https://cellphones.com.vn/phu-kien/chuot-ban-phim-may-tinh/chuot.html");

        // Khởi tạo WebDriverWait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Bấm liên tục nút Show More để hiển thị ra tất cả các sản phẩm
        this.showAllDataToCrawl(driver, wait, actions);

        List<WebElement> products = driver.findElements(By.cssSelector(".product-info"));

        // Sử dụng OutputStreamWriter để đảm bảo mã hóa UTF-8
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(osw)) {

            // Thêm BOM (Byte Order Mark) vào đầu file để đánh dấu UTF-8
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            String[] header = {"nameHtml", "imgHtml", "infoHtml", "priceHtml"};
            csvWriter.writeNext(header);
            String nameHtml;
            String imgHtml;
            String infoHtml;
            String priceHtml;
            int count = 0;
            for (WebElement product : products) {
                try {
                    System.out.println("Product: " + count);
                    var href = product.findElement(By.cssSelector("a[href]"));
                    var name = product.findElement(By.cssSelector(".product__name"));
                    nameHtml = name.getAttribute("outerHTML");

                    var imageLink = product.findElement(By.cssSelector(".product__image img"));
                    imgHtml = imageLink.getAttribute("outerHTML");

                    var price = product.findElement(By.cssSelector(".product__price--show"));
                    priceHtml = price.getAttribute("outerHTML");

                    String productLink = href.getAttribute("href");
                    System.out.println("Product link: " + productLink);
                    infoHtml = dataCrawlService.crawlInfoHtml(productLink);

                    System.out.println("-------------------------------------------");
                    csvWriter.writeNext(new String[]{nameHtml, imgHtml, infoHtml, priceHtml});
                    count++;
                } catch (Exception e) {
                    e.printStackTrace(); // skip product bị lỗi
                }
            }
            System.out.println("Tổng số sản phẩm crawl được: " + count);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            driver.quit();
        }
    }

    public int csvHtmlToCsvData(String csvHtmlPath, String csvDataPath) throws IOException, CsvValidationException {
        int recordCount = 0;

        try (CSVReader csvReader = new CSVReader(new FileReader(csvHtmlPath));
             FileOutputStream fos = new FileOutputStream(csvDataPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(osw)) {

            // Thêm BOM (Byte Order Mark) vào đầu file để đánh dấu UTF-8
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            String[] nextLine;

            // Write the header to the output CSV file
            String[] header = { "product_name", "image_url", "size", "weight", "resolution", "sensor",
                    "buttons", "connection", "battery", "compatibility", "utility", "manufacturer", "price" };
            csvWriter.writeNext(header);

            csvReader.readNext();  // Đọc và bỏ qua dòng đầu tiên
            // Read each line from the input CSV
            while ((nextLine = csvReader.readNext()) != null) {
                // Check if the row has the expected number of columns (at least 4)
                if (nextLine.length < 4) {
                    System.out.println("Skipping row due to insufficient columns: " + String.join(", ", nextLine));
                    continue;
                }

                ++recordCount;

                // Columns in the input CSV: nameHtml, imgHtml, infoHtml, priceHtml
                String htmlContentName = nextLine[0];
                String htmlContentImg = nextLine[1];
                String htmlContent = nextLine[2];
                String priceHtml = nextLine[3];

                // Parse product name from the first column
                Document docName = Jsoup.parse(htmlContentName);
                String productName = docName.select("div.product__name h3").text();
                System.out.println("Product Name: " + productName);  // Debugging

                // Parse image URL from the second column
                Document docImg = Jsoup.parse(htmlContentImg);
                String img = docImg.select("img.product__img").attr("src");
                System.out.println("Image URL: " + img);  // Debugging

                // Parse other details from the infoHtml column
                Document docInfo = Jsoup.parse(htmlContent);
                String size = extractAttribute(docInfo, "Kích thước");
                String weight = extractAttribute(docInfo, "Trọng lượng");
                String resolution = extractAttribute(docInfo, "Độ phân giải");
                String sensor = extractAttribute(docInfo, "Cảm biến");
                String buttons = extractAttribute(docInfo, "Nút nhấn");
                String connection = extractAttribute(docInfo, "Kết nối");
                String battery = extractAttribute(docInfo, "Pin");
                String compatibility = extractAttribute(docInfo, "Tương thích");
                String utility = extractAttribute(docInfo, "Tiện ích");
                String manufacturer = extractAttribute(docInfo, "Hãng sản xuất");

                // Parse price from priceHtml column
                Document docPrice = Jsoup.parse(priceHtml);
                String price = docPrice.select(".product__price--show").text();  // Adjust selector as needed
                System.out.println("Price: " + price);  // Debugging

                // Write extracted data to the output CSV file
                String[] data = { productName, img, size, weight, resolution, sensor,
                        buttons, connection, battery, compatibility, utility, manufacturer, price };
                csvWriter.writeNext(data);
            }


            System.out.println("Data has been extracted and written to " + csvDataPath);
            return recordCount;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void showAllDataToCrawl(WebDriver driver, WebDriverWait wait, Actions actions) {
        while (true) {
            try {
                // Kiểm tra xem nút "Show More" có tồn tại không
                List<WebElement> showMoreButtonList = driver.findElements(By.cssSelector(".btn-show-more"));
                if (showMoreButtonList.isEmpty()) {
                    System.out.println("Nút Show More không còn tồn tại.");
                    break; // Thoát vòng lặp khi nút không còn tồn tại
                }

                WebElement showMoreButton = showMoreButtonList.get(0);

                // Kiểm tra xem nút có thể bấm được hay không
                if (!showMoreButton.isEnabled() || !showMoreButton.isDisplayed()) {
                    break; // Thoát vòng lặp khi nút không còn khả năng bấm
                }

                // Chờ cho nút ".btn-show-more" không có class "is-loading" và "is-large"
                wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(showMoreButton, "class", "is-loading")));
                wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(showMoreButton, "class", "is-large")));

                // Bấm vào nút để load thêm dữ liệu
                actions.moveToElement(showMoreButton).click().perform();

                Thread.sleep(500); // Tạm dừng để đảm bảo dữ liệu mới được tải

                // Chờ dữ liệu mới xuất hiện
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product-info")));
            } catch (Exception e) {
                e.printStackTrace();
                // Thoát vòng lặp nếu có bất kỳ lỗi gì khác
                break;
            }
        }
    }

    private static String extractAttribute(Document doc, String attributeName) {
        for (Element item : doc.select("div.modal-item-description")) {
            // Iterate over all <p> and <div> pairs containing information
            for (Element row : item.select("div.is-flex")) {
                String label = row.select("p").text(); // Label (e.g., Size)
                String value = row.select("div").last().text(); // Only take the last value from div

                // Return value if label contains the desired attribute name
                if (label.contains(attributeName)) {
                    System.out.println("Extracted " + attributeName + ": " + value);  // Debugging
                    return value;
                }
            }
        }
        return "None"; // Return "None" if no matching attribute is found
    }
}
