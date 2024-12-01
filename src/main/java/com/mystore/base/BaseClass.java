package com.mystore.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.mystore.utility.ExtentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * BaseClass is used to load the config file and Initialize WebDriver
 */
public class BaseClass {
    public static Properties prop;

    // Declare ThreadLocal Driver
    public static ThreadLocal<RemoteWebDriver> driver = new ThreadLocal<>();

    // Initialize Logger
    private static final Logger logger = LogManager.getLogger(BaseClass.class);

    // loadConfig method is to load the configuration
    @BeforeSuite(groups = { "Smoke", "Sanity", "Regression" })
    public void loadConfig() {
        ExtentManager.setExtent();

        // Specify the path to your Log4j 2 configuration file
        System.setProperty("log4j.configurationFile", System.getProperty("user.dir") + "\\Configuration\\log4j2.xml");

        try {
            prop = new Properties();
            FileInputStream ip = new FileInputStream(
                    System.getProperty("user.dir") + "\\Configuration\\config.properties");
            prop.load(ip);
        } catch (FileNotFoundException e) {
            logger.error("Configuration file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error loading configuration: " + e.getMessage());
        }
    }

    public static WebDriver getDriver() {
        // Get Driver from ThreadLocalMap
        return driver.get();
    }

    public void launchApp(String browserName) {
        if (browserName.equalsIgnoreCase("Chrome")) {
            WebDriverManager.chromedriver().setup();
            driver.set(new ChromeDriver());
        } else if (browserName.equalsIgnoreCase("Firefox")) { // Use consistent case
            WebDriverManager.firefoxdriver().setup();
            driver.set(new FirefoxDriver());
        } else if (browserName.equalsIgnoreCase("IE")) {
            WebDriverManager.iedriver().setup();
            driver.set(new InternetExplorerDriver());
        } else {
            logger.error("Browser not supported: " + browserName);
            throw new IllegalArgumentException("Browser not supported: " + browserName);
        }
        
        // Maximize the screen
        getDriver().manage().window().maximize();
        // Delete all the cookies
        getDriver().manage().deleteAllCookies();
        // Implicit TimeOuts
        getDriver().manage().timeouts().implicitlyWait(
            Integer.parseInt(prop.getProperty("implicitWait")), TimeUnit.SECONDS);
        // PageLoad TimeOuts
        getDriver().manage().timeouts().pageLoadTimeout(
            Integer.parseInt(prop.getProperty("pageLoadTimeOut")), TimeUnit.SECONDS);
        // Launching the URL
        getDriver().get(prop.getProperty("url"));
    }

    @AfterSuite(groups = { "Smoke", "Regression", "Sanity" })
    public void afterSuite() {
        ExtentManager.endReport();
    }
}
