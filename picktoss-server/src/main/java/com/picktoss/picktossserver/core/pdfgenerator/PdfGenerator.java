package com.picktoss.picktossserver.core.pdfgenerator;

import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class PdfGenerator {
//    public byte[] generateQuizPdf(List<Quiz> quizzes) throws DocumentException, IOException {
//        Document document = new Document();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//        PdfWriter.getInstance(document, out);
//
//        // 사용자 지정 폰트 설정
//        String fontPath = "fonts/NotoSansKR-Regular.ttf";
//        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//        Font titleFont = new Font(baseFont, 16, Font.BOLD, Color.BLUE);
//        Font contentFont = new Font(baseFont, 12, Font.NORMAL, Color.BLACK);
//        Font optionFont = new Font(baseFont, 12, Font.NORMAL, Color.GRAY);
//
//        document.open();
//
//        Paragraph title = new Paragraph("Quiz Collection", titleFont);
//        title.setAlignment(Element.ALIGN_CENTER);
//        title.setSpacingAfter(20);
//        document.add(title);
//
//        for (Quiz quiz : quizzes) {
//            document.add(new Paragraph("Question: " + quiz.getQuestion(), contentFont));
//
//            Set<Option> options = quiz.getOptions();
//            if (!options.isEmpty()) {
//                Paragraph optionsTitle = new Paragraph("Options:", contentFont);
//                optionsTitle.setSpacingBefore(10); // 위쪽 여백
//                document.add(optionsTitle);
//
//                PdfPTable table = new PdfPTable(1);
//                table.setWidthPercentage(90);
//                for (Option option : options) {
//                    PdfPCell cell = new PdfPCell(new Phrase("- " + option.getOption(), optionFont));
//                    cell.setBorder(Rectangle.NO_BORDER);
//                    cell.setPadding(5);
//                    table.addCell(cell);
//                }
//                document.add(table);
//            }
//
//            document.add(new Paragraph("Answer: " + quiz.getAnswer(), contentFont));
//            document.add(new Paragraph("Explanation: " + quiz.getExplanation(), contentFont));
//
//            LineSeparator separator = new LineSeparator();
//            separator.setLineColor(Color.LIGHT_GRAY);
//            separator.setPercentage(100);
//            Paragraph lineBreak = new Paragraph("\n");
//            document.add(lineBreak);
//            document.add(separator);
//            document.add(lineBreak);
//        }
//
//        document.close();
//        return out.toByteArray();
//    }

    public byte[] generateQuizPdf(List<Quiz> quizzes) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        // 사용자 지정 폰트 설정 (일반 텍스트용)
        String fontPath = "fonts/NotoSansKR-Regular.ttf";
        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        // 이모지 지원 폰트 설정
        String emojiFontPath = "fonts/NotoSansSymbols-Regular.ttf";
        BaseFont emojiFont = BaseFont.createFont(emojiFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        // 폰트 스타일 지정
        Font titleFont = new Font(emojiFont, 20, Font.BOLD, Color.DARK_GRAY);
        Font questionFont = new Font(baseFont, 14, Font.BOLD, Color.BLACK);
        Font contentFont = new Font(baseFont, 12, Font.NORMAL, Color.BLACK);
        Font optionFont = new Font(baseFont, 12, Font.NORMAL, Color.GRAY);

        document.open();

        // 문서 제목 추가 (이모지 적용)
        Paragraph title = new Paragraph("📘 Quiz Collection", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(30);
        document.add(title);

        for (Quiz quiz : quizzes) {
            // 문제 추가
            Paragraph question = new Paragraph("Q: " + quiz.getQuestion(), questionFont);
            question.setSpacingBefore(15);
            question.setSpacingAfter(10);
            document.add(question);

            // 선택지 추가
            Set<Option> options = quiz.getOptions();
            if (!options.isEmpty()) {
                PdfPTable table = new PdfPTable(2); // 2열 테이블로 선택지를 정렬
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);
                table.setSpacingAfter(10);

                for (Option option : options) {
                    PdfPCell cell = new PdfPCell(new Phrase(option.getOption(), optionFont));
                    cell.setPadding(8);
                    cell.setBorder(Rectangle.BOX);
                    table.addCell(cell);
                }
                document.add(table);
            }

            // 정답 및 해설 추가 (이모지 포함)
            Paragraph answer = new Paragraph("✔ Answer: " + quiz.getAnswer(), new Font(baseFont, 12, Font.BOLD, Color.BLUE));
            answer.setSpacingBefore(10);
            document.add(answer);

            Paragraph explanation = new Paragraph("📌 Explanation: " + quiz.getExplanation(), new Font(baseFont, 12, Font.NORMAL, Color.BLACK));
            explanation.setSpacingAfter(15);
            document.add(explanation);

            // 구분선 추가
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(Color.LIGHT_GRAY);
            separator.setPercentage(100);
            document.add(new Chunk(separator));

            // 줄바꿈 추가
            document.add(new Paragraph("\n"));
        }

        document.close();
        return out.toByteArray();
    }
}
