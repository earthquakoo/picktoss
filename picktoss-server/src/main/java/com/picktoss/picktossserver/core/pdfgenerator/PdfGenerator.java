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
    public byte[] generateQuizPdf(List<Quiz> quizzes) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);

        // 사용자 지정 폰트 설정
        String fontPath = "fonts/NotoSansKR-Regular.ttf";
        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font titleFont = new Font(baseFont, 16, Font.BOLD, Color.BLUE);
        Font contentFont = new Font(baseFont, 12, Font.NORMAL, Color.BLACK);
        Font optionFont = new Font(baseFont, 12, Font.NORMAL, Color.GRAY);

        document.open();

        Paragraph title = new Paragraph("Quiz Collection", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        for (Quiz quiz : quizzes) {
            document.add(new Paragraph("Question: " + quiz.getQuestion(), contentFont));

            Set<Option> options = quiz.getOptions();
            if (!options.isEmpty()) {
                Paragraph optionsTitle = new Paragraph("Options:", contentFont);
                optionsTitle.setSpacingBefore(10); // 위쪽 여백
                document.add(optionsTitle);

                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(90);
                for (Option option : options) {
                    PdfPCell cell = new PdfPCell(new Phrase("- " + option.getOption(), optionFont));
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
                document.add(table);
            }

            document.add(new Paragraph("Answer: " + quiz.getAnswer(), contentFont));
            document.add(new Paragraph("Explanation: " + quiz.getExplanation(), contentFont));

            LineSeparator separator = new LineSeparator();
            separator.setLineColor(Color.LIGHT_GRAY);
            separator.setPercentage(100);
            Paragraph lineBreak = new Paragraph("\n");
            document.add(lineBreak);
            document.add(separator);
            document.add(lineBreak);
        }

        document.close();
        return out.toByteArray();
    }
}
