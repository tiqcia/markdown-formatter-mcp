package br.edu.ifpr.mcp_server.markdown;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownPdfExporter {

    public String exportFileToPdf(String filePath) throws Exception {
        Path inputPath = MarkdownFilePaths.validateAndResolvePath(filePath);
        String markdownContent = Files.readString(inputPath, StandardCharsets.UTF_8);

        Path pdfOutputPath = MarkdownFilePaths.buildOutputPath(inputPath, "", ".pdf");
        renderPdf(markdownContent, pdfOutputPath, inputPath.getParent().toUri().toString());

        return "PDF exportado com sucesso:\n- Saída: " + pdfOutputPath;
    }

    private void renderPdf(String markdown, Path pdfPath, String baseUri) throws Exception {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        options.set(HtmlRenderer.ESCAPE_HTML, true);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(markdown);
        String bodyHtml = renderer.render(document);

        String fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8" />
            <style>
                body { font-family: sans-serif; font-size: 11pt; line-height: 1.5; color: #1f1b24; margin: 20px; }
                h1, h2, h3 { color: #4c1d95; margin-top: 18px; }
                h1 { border-bottom: 2px solid #ddd6fe; padding-bottom: 4px; }
                table { border-collapse: collapse; width: 100%; margin: 14px 0; }
                th, td { border: 1px solid #d8b4fe; padding: 8px 10px; text-align: left; }
                th { background-color: #f3e8ff; color: #3b0764; font-weight: bold; }
                code { background-color: #faf5ff; color: #581c87; padding: 2px 5px; border-radius: 3px; font-family: monospace; }
                pre { background-color: #faf5ff; border: 1px solid #e9d5ff; padding: 10px; border-radius: 4px; }
                blockquote { border-left: 4px solid #7e22ce; padding-left: 12px; color: #4a3b5c; margin: 12px 0; }
            </style>
            </head>
            <body>
            """ + bodyHtml + """
            </body>
            </html>
            """;

        try (OutputStream os = new FileOutputStream(pdfPath.toFile())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(fullHtml, baseUri);
            builder.toStream(os);
            builder.run();
        }
    }
}