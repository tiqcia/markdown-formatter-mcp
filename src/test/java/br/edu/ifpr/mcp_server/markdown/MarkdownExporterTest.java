package br.edu.ifpr.mcp_server.markdown;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownPdfExporterTest {

    @Test
    void deveGerarArquivoPdfValidoAPartirDeMarkdown(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("documento.md");
        Files.writeString(input, "# Título Principal\n\n| Coluna 1 | Coluna 2 |\n| --- | --- |\n| Dado A | Dado B |\n", StandardCharsets.UTF_8);

        MarkdownPdfExporter exporter = new MarkdownPdfExporter();
        String response = exporter.exportFileToPdf(input.toString());

        Path expectedPdf = tempDir.resolve("documento.pdf");
        assertTrue(Files.isRegularFile(expectedPdf));
        assertTrue(Files.size(expectedPdf) > 0);
        assertTrue(response.contains("PDF exportado com sucesso"));
    }
}