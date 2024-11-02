package com.picktoss.picktossserver.core.pdfgenerator;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class PdfGenerator {
    public byte[] generateQuizPdf(List<Quiz> quizzes) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        String fontPath = "fonts/NotoSansKR-Regular.ttf";
        BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(baseFont, 12);
        document.open();

        for (Quiz quiz : quizzes) {
            document.add(new Paragraph("Quiz Question: " + quiz.getQuestion(), font));
            document.add(new Paragraph("Options: " + quiz.getOptions().toString(), font));
            document.add(new Paragraph("Answer: " + quiz.getAnswer(), font));
            document.add(new Paragraph("Explanation: " + quiz.getExplanation(), font));
            document.add(new Paragraph("\n"));
        }

        document.close();
        return out.toByteArray();
    }
}
