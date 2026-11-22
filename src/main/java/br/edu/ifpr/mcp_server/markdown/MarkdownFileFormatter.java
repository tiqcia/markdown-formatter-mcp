package br.edu.ifpr.mcp_server.markdown;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MarkdownFileFormatter {

    private final TablePrettifier tablePrettifier;
    private final MarkdownLinter linter;

    public MarkdownFileFormatter() {
        this(new TablePrettifier(), new MarkdownLinter());
    }

    public MarkdownFileFormatter(TablePrettifier tablePrettifier, MarkdownLinter linter) {
        this.tablePrettifier = tablePrettifier;
        this.linter = linter;
    }

    public String formatMarkdownOnly(String filePath) throws Exception {
        Path inputPath = MarkdownFilePaths.validateAndResolvePath(filePath);
        String originalContent = Files.readString(inputPath, StandardCharsets.UTF_8);

        List<LintIssue> issues = linter.lint(originalContent);
        String formattedContent = formatFullDocument(originalContent);

        Path mdOutputPath = MarkdownFilePaths.buildOutputPath(
                inputPath, "_formatado", MarkdownFilePaths.getExtension(inputPath));
        Files.writeString(mdOutputPath, formattedContent, StandardCharsets.UTF_8);

        StringBuilder response = new StringBuilder();
        response.append("Arquivo Markdown formatado com sucesso:\n")
                .append("- Saída: ").append(mdOutputPath).append("\n\n");

        if (issues.isEmpty()) {
            response.append("Relatório de erros (Lint): Nenhum problema encontrado no arquivo original.");
        } else {
            response.append("Relatório de erros corrigidos do arquivo original (")
                    .append(issues.size()).append("):\n");
            for (LintIssue issue : issues) {
                response.append("- Linha ").append(issue.line())
                        .append(" [").append(issue.rule()).append("]: ")
                        .append(issue.message()).append("\n");
            }
        }

        return response.toString().stripTrailing();
    }

    String formatFullDocument(String content) {
        String[] lines = content.replace("\r\n", "\n").split("\n", -1);
        List<String> result = new ArrayList<>();
        List<String> tableBuffer = new ArrayList<>();
        boolean inCodeBlock = false;
        String codeFence = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (!inCodeBlock && (trimmed.startsWith("```") || trimmed.startsWith("~~~"))) {
                if (!tableBuffer.isEmpty()) {
                    result.addAll(tablePrettifier.normalizeTableBlock(tableBuffer));
                    tableBuffer.clear();
                }
                inCodeBlock = true;
                codeFence = trimmed.substring(0, 3);
                result.add(line);
                continue;
            } else if (inCodeBlock) {
                result.add(line);
                if (trimmed.startsWith(codeFence)) {
                    inCodeBlock = false;
                    codeFence = "";
                }
                continue;
            }

            if (isTableLine(trimmed)) {
                tableBuffer.add(trimmed);
            } else if (trimmed.isEmpty() && !tableBuffer.isEmpty() && isContinuationOfSameTable(lines, i + 1)) {
                continue;
            } else {
                if (!tableBuffer.isEmpty()) {
                    result.addAll(tablePrettifier.normalizeTableBlock(tableBuffer));
                    tableBuffer.clear();
                }

                String processedLine = line;
                if (trimmed.matches("^#{1,6}[^#\\s].*")) {
                    processedLine = line.replaceFirst("^(#{1,6})([^#\\s])", "$1 $2");
                }
                result.add(processedLine);
            }
        }

        if (!tableBuffer.isEmpty()) {
            result.addAll(tablePrettifier.normalizeTableBlock(tableBuffer));
        }

        return String.join("\n", result).replaceAll("\n{3,}", "\n\n");
    }

    private boolean isContinuationOfSameTable(String[] lines, int nextIndex) {
        for (int j = nextIndex; j < lines.length; j++) {
            String nextTrimmed = lines[j].trim();
            if (nextTrimmed.isEmpty()) {
                continue;
            }
            if (!isTableLine(nextTrimmed)) {
                return false;
            }
            if (j + 1 < lines.length) {
                String afterNext = lines[j + 1].trim().replace("|", "").trim();
                if (!afterNext.isEmpty() && afterNext.matches("^[:\\s-]+$")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isTableLine(String trimmedLine) {
        if (trimmedLine.isEmpty()) {
            return false;
        }
        return trimmedLine.startsWith("|") || trimmedLine.matches("^[^|]+(\\|[^|]+)+\\|?$");
    }
}