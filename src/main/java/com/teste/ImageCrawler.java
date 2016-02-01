package com.teste;

import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class ImageCrawler extends WebCrawler {

    private static final Pattern IGNORE_FILTER = Pattern
            .compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern IMAGE_PATTERN = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");

    private static File storageFolder;
    private static String[] crawlDomains;

    public static void configure(String[] domain, String storageFolderName) {
        crawlDomains = domain;

        storageFolder = new File(storageFolderName);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (IGNORE_FILTER.matcher(href).matches()) {
            return false;
        }

        if (IMAGE_PATTERN.matcher(href).matches()) {
            return true;
        }

        for (String domain : crawlDomains) {
            if (href.startsWith(domain)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();

        if (!IMAGE_PATTERN.matcher(url).matches()
                || !((page.getParseData() instanceof BinaryParseData) || (page.getContentData().length < (15 * 1024)))) {
            return;
        }

        String extension = url.substring(url.lastIndexOf('.'));
        String hashedName = UUID.randomUUID() + extension;

        String filename = storageFolder.getAbsolutePath() + "/" + hashedName;
        try {
            byte[] contentData = page.getContentData();

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(contentData));
            if (image.getWidth() > 400 && image.getHeight() > 400) {
                Files.write(contentData, new File(filename));
                logger.info("Stored: {}", url);
            } else {
                logger.error("Bypassing small image.");
            }
        } catch (IOException iox) {
            logger.error("Failed to write file: " + filename, iox);
        }
    }
}
