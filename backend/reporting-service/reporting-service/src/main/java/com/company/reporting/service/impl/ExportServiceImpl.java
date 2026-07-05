package com.company.reporting.service.impl;

import com.company.reporting.entity.*;
import com.company.reporting.exception.ExportException;
import com.company.reporting.repository.*;
import com.company.reporting.service.ExportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final PulseSummaryRepository pulseSummaryRepository;
    private final LocationSummaryRepository locationSummaryRepository;
    private final ThemeSummaryRepository themeSummaryRepository;
    private final AIInsightRepository aiInsightRepository;

    @Override
    public byte[] exportPdf(String month) {
        log.info("Generating PDF report for month: {}", month);
        PulseSummary pulse = pulseSummaryRepository.findByMonth(month).orElse(null);
        List<LocationSummary> locations = locationSummaryRepository.findByMonth(month);
        List<ThemeSummary> themes = themeSummaryRepository.findByMonth(month);
        AIInsight insight = aiInsightRepository.findByMonth(month).orElse(null);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD);
            Paragraph title = new Paragraph("Employee Pulse Survey Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Subtitle
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.ITALIC);
            Paragraph subtitle = new Paragraph("Month: " + month, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // Section 1: Overview
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLD);
            Paragraph sec1Header = new Paragraph("1. Executive Summary", sectionHeaderFont);
            sec1Header.setSpacingAfter(10);
            document.add(sec1Header);

            if (pulse != null) {
                document.add(new Paragraph(String.format("Overall Pulse Score: %.1f%%", pulse.getPulseScore())));
                document.add(new Paragraph(String.format("Response Rate: %.1f%% (Total Submissions: %d)", pulse.getParticipationRate(), pulse.getTotalResponses())));
                document.add(new Paragraph(String.format("Sentiment - Positive: %.1f%%, Neutral: %.1f%%, Negative: %.1f%%", 
                        pulse.getPositiveSentiment(), pulse.getNeutralSentiment(), pulse.getNegativeSentiment())));
            } else {
                document.add(new Paragraph("No survey metrics found for the specified month."));
            }

            if (insight != null) {
                document.add(new Paragraph("\nAI Generated Insights:"));
                document.add(new Paragraph(insight.getInsight()));
                document.add(new Paragraph("\nLocation Insights:"));
                document.add(new Paragraph(insight.getLocationAnalysis()));
            }
            document.add(new Paragraph("\n"));

            // Section 2: Location Scores
            Paragraph sec2Header = new Paragraph("2. Location-wise Morale Index", sectionHeaderFont);
            sec2Header.setSpacingAfter(10);
            document.add(sec2Header);

            if (!locations.isEmpty()) {
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.addCell(new PdfPCell(new Phrase("Location Name")));
                table.addCell(new PdfPCell(new Phrase("Type")));
                table.addCell(new PdfPCell(new Phrase("Pulse Score")));
                table.addCell(new PdfPCell(new Phrase("Participation Rate")));

                for (LocationSummary loc : locations) {
                    table.addCell(loc.getLocationName());
                    table.addCell(loc.getLocationType());
                    table.addCell(String.format("%.1f%%", loc.getScore()));
                    table.addCell(String.format("%.1f%%", loc.getParticipationRate()));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No location aggregates available."));
            }
            document.add(new Paragraph("\n"));

            // Section 3: Theme Aggregates
            Paragraph sec3Header = new Paragraph("3. Theme Breakdown", sectionHeaderFont);
            sec3Header.setSpacingAfter(10);
            document.add(sec3Header);

            if (!themes.isEmpty()) {
                PdfPTable themeTable = new PdfPTable(3);
                themeTable.setWidthPercentage(100);
                themeTable.addCell(new PdfPCell(new Phrase("Theme / Category")));
                themeTable.addCell(new PdfPCell(new Phrase("Morale Score")));
                themeTable.addCell(new PdfPCell(new Phrase("Total Responses")));

                for (ThemeSummary th : themes) {
                    themeTable.addCell(th.getTheme());
                    themeTable.addCell(String.format("%.1f%%", th.getScore()));
                    themeTable.addCell(String.valueOf(th.getTotalResponses()));
                }
                document.add(themeTable);
            } else {
                document.add(new Paragraph("No theme aggregates available."));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error creating PDF export: {}", e.getMessage(), e);
            throw new ExportException("Failed to generate PDF report", e);
        }
    }

    @Override
    public byte[] exportCsv(String month) {
        log.info("Generating CSV report for month: {}", month);
        PulseSummary pulse = pulseSummaryRepository.findByMonth(month).orElse(null);
        List<LocationSummary> locations = locationSummaryRepository.findByMonth(month);
        List<ThemeSummary> themes = themeSummaryRepository.findByMonth(month);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out)) {

            // Overview Section
            writer.println("SECTION,METRIC,VALUE");
            if (pulse != null) {
                writer.println(String.format("OVERVIEW,Pulse Score,%.2f%%", pulse.getPulseScore()));
                writer.println(String.format("OVERVIEW,Participation Rate,%.2f%%", pulse.getParticipationRate()));
                writer.println(String.format("OVERVIEW,Positive Sentiment,%.2f%%", pulse.getPositiveSentiment()));
                writer.println(String.format("OVERVIEW,Neutral Sentiment,%.2f%%", pulse.getNeutralSentiment()));
                writer.println(String.format("OVERVIEW,Negative Sentiment,%.2f%%", pulse.getNegativeSentiment()));
                writer.println(String.format("OVERVIEW,Total Responses,%d", pulse.getTotalResponses()));
            }
            writer.println();

            // Location Section
            writer.println("LOCATION,TYPE,SCORE,PARTICIPATION_RATE,TOTAL_RESPONSES");
            for (LocationSummary loc : locations) {
                writer.println(String.format("%s,%s,%.2f%%,%.2f%%,%d", 
                        loc.getLocationName(), loc.getLocationType(), loc.getScore(), loc.getParticipationRate(), loc.getTotalResponses()));
            }
            writer.println();

            // Theme Section
            writer.println("THEME,SCORE,TOTAL_RESPONSES");
            for (ThemeSummary th : themes) {
                writer.println(String.format("%s,%.2f%%,%d", 
                        th.getTheme(), th.getScore(), th.getTotalResponses()));
            }

            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating CSV export: {}", e.getMessage(), e);
            throw new ExportException("Failed to generate CSV report", e);
        }
    }
}
