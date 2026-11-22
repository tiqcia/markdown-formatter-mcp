package br.edu.ifpr.mcp_server.markdown;

import java.util.ArrayList;
import java.util.List;

public class MarkdownLinter {

    public List<LintIssue> lint(String content) {
        List<LintIssue> issues = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return issues;
        }

        String[] lines = content.replace("\r\n", "\n").split("\n", -1);
        boolean inCodeBlock = false;
        String codeFence = "";
        int consecutiveBlankLines = 0;

        int expectedTableCols = -1;
        int tableRowIndex = 0;

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            String line = lines[i];
            String trimmed = line.trim();

            if (!inCodeBlock && (trimmed.startsWith("```") || trimmed.startsWith("~~~"))) {
                inCodeBlock = true;
                codeFence = trimmed.substring(0, 3);
                consecutiveBlankLines = 0;
                expectedTableCols = -1;
                tableRowIndex = 0;
                continue;
            } else if (inCodeBlock) {
                if (trimmed.startsWith(codeFence)) {
                    inCodeBlock = false;
                    codeFence = "";
                }
                continue;
            }

            // MD012: múltiplas linhas em branco consecutivas
            if (trimmed.isEmpty()) {
                consecutiveBlankLines++;
                if (consecutiveBlankLines > 1) {
                    issues.add(new LintIssue(lineNumber, "MD012", "Múltiplas linhas em branco consecutivas."));
                }
                // verifica se a linha em branco interrompe uma tabela em andamento
                if (expectedTableCols > 0 && hasFollowingTableRow(lines, i + 1)) {
                    issues.add(new LintIssue(lineNumber, "MD056", "Linha em branco interrompendo a tabela."));
                } else {
                    expectedTableCols = -1;
                    tableRowIndex = 0;
                }
                continue;
            } else {
                consecutiveBlankLines = 0;
            }

            // MD018: falta espaço após '#' no cabeçalho
            if (trimmed.matches("^#{1,6}[^#\\s].*")) {
                issues.add(new LintIssue(lineNumber, "MD018", "Falta espaço após '#' no cabeçalho."));
            }

            // MD042: link sem URL de destino
            if (trimmed.matches(".*\\[[^\\]]*\\]\\(\\s*\\).*")) {
                issues.add(new LintIssue(lineNumber, "MD042", "Link sem URL de destino '[]( )'."));
            }

            // MD055 / MD056: validação de estrutura de tabela
            if (isTableLine(trimmed)) {
                boolean missingLeadingPipe = !trimmed.startsWith("|");
                boolean missingTrailingPipe = !trimmed.endsWith("|") || trimmed.endsWith("\\|");

                if (missingLeadingPipe || missingTrailingPipe) {
                    issues.add(new LintIssue(lineNumber, "MD055", "Linha de tabela sem delimitador '|' no início ou no fim."));
                }

                String inner = trimmed;
                if (inner.startsWith("|")) inner = inner.substring(1);
                if (inner.endsWith("|") && !inner.endsWith("\\|")) inner = inner.substring(0, inner.length() - 1);
                String[] cells = inner.split("(?<!\\\\)\\|", -1);

                if (tableRowIndex == 0) {
                    expectedTableCols = cells.length;
                } else if (tableRowIndex == 1) {
                    if (cells.length != expectedTableCols) {
                        issues.add(new LintIssue(lineNumber, "MD056",
                            "Linha separadora com " + cells.length + " colunas (esperado: " + expectedTableCols + ")."));
                    }
                    for (String cell : cells) {
                        String c = cell.trim();
                        if (!c.matches("^:?-{3,}:?$")) {
                            issues.add(new LintIssue(lineNumber, "MD055",
                                "Separador de tabela inválido ('" + c + "'): deve conter no mínimo 3 traços ('---')."));
                            break;
                        }
                    }
                } else if (cells.length != expectedTableCols) {
                    issues.add(new LintIssue(lineNumber, "MD056",
                        "Linha de tabela com " + cells.length + " colunas (esperado: " + expectedTableCols + ")."));
                }
                tableRowIndex++;
            } else {
                expectedTableCols = -1;
                tableRowIndex = 0;
            }
        }

        return issues;
    }

    private boolean isTableLine(String trimmedLine) {
        return trimmedLine.startsWith("|") || trimmedLine.matches("^[^|]+(\\|[^|]+)+\\|?$");
    }

    private boolean hasFollowingTableRow(String[] lines, int nextIndex) {
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
}