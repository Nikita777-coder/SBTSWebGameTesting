package com.example;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleTests {
  private WebDriver driver;
  private boolean acceptNextAlert = true;
  private final StringBuffer verificationErrors = new StringBuffer();
  JavascriptExecutor js;
  @BeforeEach
  public void setUp() {
    ChromeDriverService cds = ChromeDriverService.createDefaultService();
      String PATH_TO_CHROMEDRIVER = "<path to chromedriver>";
      cds.setExecutable(PATH_TO_CHROMEDRIVER);
    ChromeOptions cdo = new ChromeOptions();
      String PATH_TO_CHROME = "<path to chrome or Google Chrome browser>";
      cdo.setBinary(PATH_TO_CHROME);
    cdo.addArguments("--no-sandbox");

    driver = new ChromeDriver(cds, cdo);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    js = (JavascriptExecutor) driver;

    //Open page
      String HOST = "http://ruswizard.ddns.net";
      driver.get(HOST +":8091/");
    //Click on session id
    clickLoginAndSession();
  }

  private void clickLoginAndSession() {
    driver.findElement(By.id("sessionId")).click();
    driver.findElement(By.id("sessionId")).clear();
    //Set specific session (Be aware that moneys on session are not endless)
      String SESSION = "DHil9g6IMrNErWMUee";
      driver.findElement(By.id("sessionId")).sendKeys(SESSION);
    driver.findElement(By.id("login-btn")).click();
    waitOnlineStatus();
  }
  private void waitOnlineStatus() {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(2) );
    String statusOnline = "//div[@id='status' and contains(., 'Online')]";
    dwd.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(statusOnline)));
  }
  private void waitMoveOver() {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(2) );
    dwd.until(ExpectedConditions.invisibilityOfElementLocated(By.className("moveAnimationContent")));
  }

  private void waitTimerZero(){
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(2) );
    String statusOnline = "//div[contains(@class, 'timer') and contains(., '0')]";
    dwd.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(statusOnline)));
  }

  @Test
  public void test5minute() throws Exception {
    Thread.sleep(300_000);

    assertTrue(isElementPresent(By.id("login-btn")), "Login button should be present after auto logout");
    assertFalse(isElementPresent(By.id("logout-btn")), "Logout button should not be present after auto logout");
  }

  @Test
  public void testThereIsNoObjectsFromMinus20AndCanShipGoOutOfBorder() {
    String loc = driver.findElement(By.id("place")).getText();
    long position = Long.parseLong(loc.substring(loc.indexOf('[')+1, loc.indexOf(']')));

    if (position > -20) {
      moveShipToNCoordinates(true, 20 - position);
      // move to -21
      moveShipToNCoordinates(true, 1);
    }
    boolean isDockOutOfBorder = false;

    long count = Math.abs(position);

    for (long i = 22; i < 71; ++i) {
      moveShipToNCoordinates(true, 1);
      count++;
      try {
        driver.findElement(By.id("act-0-0"));
        isDockOutOfBorder = true;
        break;
      } catch (NoSuchElementException ignored) {
      }
    }

    moveShipToNCoordinates(false, count);
    waitTimerZero();
    waitMoveOver();
    waitOnlineStatus();

    assertFalse(isDockOutOfBorder);
  }

  @Test
  public void test1moveMeasureMinimum2Seconds() {
    long startTime = System.nanoTime();
    moveShipToNCoordinates(true, 1);
    long endTime = System.nanoTime();

    moveShipToNCoordinates(false, 1);
    assertTrue((endTime - startTime) / (1000000 * 1000) >= 2);
  }

  @Test
  public void checkGoToDockTitle() {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(10) );
    dwd.until(ExpectedConditions.elementToBeClickable(By.id("act-0-0")));
    assertEquals("Зайти в док", driver.findElement(By.id("act-0-0")).getText());
  }

  @Test
  public void checkGoFromDockTitle() {
    driver.findElement(By.id("act-0-0")).click();
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(10) );
    dwd.until(ExpectedConditions.elementToBeClickable(By.id("act-0-1")));
    String s = driver.findElement(By.id("act-0-1")).getText();

    var we = driver.findElement(By.id("act-0-1"));
    js.executeScript("arguments[0].scrollIntoView();", we);
    dwd.until(ExpectedConditions.elementToBeClickable(we));
    we.click();

    assertEquals("Вернуться в док", s);
  }

  @Test
  public void testDecreaseOfProductCostAndIncreaseOfSaleCostWhenSellProduct() throws InterruptedException {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(30) );
    dwd.until(ExpectedConditions.elementToBeClickable(By.id("act-0-0")));
    //click on dock func
    driver.findElement(By.id("act-0-0")).click();
    //mode changes also requires some time to load the page
    waitOnlineStatus();
    Thread.sleep(2000);
    //buy item 1008
    driver.findElement(By.id("item1008buy")).click();
    //load for sure for mode change
    waitOnlineStatus();
    Thread.sleep(2000);

    // get item 1008 costs before sell
    WebElement table = driver.findElement(By.id("tradeTable"));
    WebElement firstRow = table.findElements(By.tagName("tr")).getFirst();
    WebElement costs = firstRow.findElements(By.tagName("td")).get(3);
    String s = costs.getText();
    String[] nums = s.split("/");
    var num1_before = Double.parseDouble(nums[0]);
    var num2_before = Double.parseDouble(nums[1]);

    // sell product
    driver.findElement(By.id("item1008sell")).click();
    //load for sure for mode change
    waitOnlineStatus();
    Thread.sleep(2000);

    // get item 1008 costs before sell
    table = driver.findElement(By.id("tradeTable"));
    firstRow = table.findElements(By.tagName("tr")).getFirst();
    costs = firstRow.findElements(By.tagName("td")).get(3);
    s = costs.getText();
    nums = s.split("/");
    var num1 = Double.parseDouble(nums[0]);
    var num2 = Double.parseDouble(nums[1]);

    var we = driver.findElement(By.id("act-0-1"));
    js.executeScript("arguments[0].scrollIntoView();", we);
    dwd.until(ExpectedConditions.elementToBeClickable(we));
    we.click();

    assertTrue(num1_before > num1 && num2_before < num2);
  }

  @Test
  @DisplayName("тест не учитывает ситуацию, когда другой человек успел купить в течение минуты товар")
  public void testPriceReturningToDefaultFrom1Minute() throws InterruptedException {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(10) );
    //click on dock func
    driver.findElement(By.id("act-0-0")).click();
    //mode changes also requires some time to load the page
    waitOnlineStatus();
    Thread.sleep(2000);
    //buy item 1008
    driver.findElement(By.id("item1008buy")).click();
    //load for sure for mode change
    waitOnlineStatus();
    Thread.sleep(2000);

    // get item 1008 costs before sell
    WebElement table = driver.findElement(By.id("tradeTable"));
    WebElement firstRow = table.findElements(By.tagName("tr")).getFirst();
    WebElement costs = firstRow.findElements(By.tagName("td")).get(3);
    String s = costs.getText();
    String[] nums = s.split("/");
    var num1_before = Double.parseDouble(nums[0]);
    var num2_before = Double.parseDouble(nums[1]);

    // sleep for 1 minute
    Thread.sleep(60_000);

    // get costs
    table = driver.findElement(By.id("tradeTable"));
    firstRow = table.findElements(By.tagName("tr")).getFirst();
    costs = firstRow.findElements(By.tagName("td")).get(3);
    s = costs.getText();
    nums = s.split("/");
    var num1 = Double.parseDouble(nums[0]);
    var num2 = Double.parseDouble(nums[1]);
    Thread.sleep(2000);

    var we = driver.findElement(By.id("act-0-1"));
    js.executeScript("arguments[0].scrollIntoView();", we);
    dwd.until(ExpectedConditions.elementToBeClickable(we));
    we.click();

    assertTrue(num1_before != num1 && num2 != num2_before);
  }

  @Test
  public void testConstCostsDuringNotAvailableOperation() throws InterruptedException {
    WebDriverWait dwd = new WebDriverWait(driver,Duration.ofSeconds(30) );
    dwd.until(ExpectedConditions.elementToBeClickable(By.id("act-0-0")));
    //click on dock func
    driver.findElement(By.id("act-0-0")).click();
    //mode changes also requires some time to load the page
    waitOnlineStatus();
    Thread.sleep(2000);

    // get item 1008 costs before press not available sell button
    WebElement table = driver.findElement(By.id("tradeTable"));
    WebElement firstRow = table.findElements(By.tagName("tr")).get(1);
    WebElement costs = firstRow.findElements(By.tagName("td")).get(3);
    String s = costs.getText();
    String[] nums = s.split("/");
    var num1_before = Double.parseDouble(nums[0]);
    var num2_before = Double.parseDouble(nums[1]);

    // click sell button to 1001 product
    driver.findElement(By.id("item1001sell")).click();
    Thread.sleep(2000);

    // get costs
    table = driver.findElement(By.id("tradeTable"));
    firstRow = table.findElements(By.tagName("tr")).get(1);
    costs = firstRow.findElements(By.tagName("td")).get(3);
    s = costs.getText();
    nums = s.split("/");
    var num1 = Double.parseDouble(nums[0]);
    var num2 = Double.parseDouble(nums[1]);
    Thread.sleep(2000);

    var we = driver.findElement(By.id("act-0-1"));
    js.executeScript("arguments[0].scrollIntoView();", we);
    dwd.until(ExpectedConditions.elementToBeClickable(we));
    we.click();

    assertTrue(num1_before == num1 && num2 == num2_before);
  }

  @Test
  public void testSavingOfSessionIdInLocalStorage() {
    JavascriptExecutor js = (JavascriptExecutor) driver;

    // get sessionId from local storage
    String localStorageData = (String) js.executeScript("return window.localStorage.getItem('sessionId');");
    assertNotNull(localStorageData);
  }

  @AfterEach
  public void tearDown() throws Exception {
    //logout
    driver.findElement(By.id("logout-btn")).click();

    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }

  private void moveShipToNCoordinates(boolean left, long n) {
    WebElement wl;

    if (left) {
      wl = driver.findElement(By.id("arrowLeft"));
    } else {
      wl = driver.findElement(By.id("arrowRight"));
    }

    for (int i = 0; i < n; ++i) {
      try {
        wl.click();
      } catch (UnhandledAlertException ex) {
        driver.findElement(By.id("act-5-0")).click();
      }
    }
  }
}
