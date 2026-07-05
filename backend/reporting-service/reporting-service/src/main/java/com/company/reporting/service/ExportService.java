package com.company.reporting.service;

public interface ExportService {
    byte[] exportPdf(String month);
    byte[] exportCsv(String month);
}
