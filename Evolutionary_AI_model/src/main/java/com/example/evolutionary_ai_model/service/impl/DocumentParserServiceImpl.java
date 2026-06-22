package com.example.evolutionary_ai_model.service.impl;

import com.example.evolutionary_ai_model.service.DocumentParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档解析服务实现类，支持PDF、Word、TXT格式。
 */
@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentParserServiceImpl.class);

    // 支持的文件类型
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("pdf", "docx", "doc", "txt");

    @Override
    public String parseDocument(MultipartFile file) {
        String fileType = getFileType(file.getOriginalFilename());
        if (!isSupported(fileType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }

        try {
            return parseDocument(file.getInputStream(), fileType);
        } catch (Exception e) {
            logger.error("解析文档失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("解析文档失败: " + e.getMessage());
        }
    }

    @Override
    public String parseDocument(InputStream inputStream, String fileType) {
        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return parsePdf(inputStream);
                case "docx":
                case "doc":
                    return parseWord(inputStream);
                case "txt":
                    return parseTxt(inputStream);
                default:
                    throw new IllegalArgumentException("不支持的文件类型: " + fileType);
            }
        } catch (Exception e) {
            logger.error("解析文档失败，文件类型: {}", fileType, e);
            throw new RuntimeException("解析文档失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    @Override
    public boolean isSupported(String fileType) {
        return fileType != null && SUPPORTED_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) throws Exception {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            logger.info("PDF解析成功，文本长度: {}", text.length());
            return text;
        }
    }

    /**
     * 解析Word文件（.docx）
     */
    private String parseWord(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            String text = paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
            logger.info("Word解析成功，文本长度: {}", text.length());
            return text;
        }
    }

    /**
     * 解析TXT文件
     */
    private String parseTxt(InputStream inputStream) throws Exception {
        String text = new String(inputStream.readAllBytes());
        logger.info("TXT解析成功，文本长度: {}", text.length());
        return text;
    }
}
