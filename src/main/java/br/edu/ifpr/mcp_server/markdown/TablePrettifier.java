package br.edu.ifpr.mcp_server.markdown;

import java.util.ArrayList;
import java.util.List;

public class TablePrettifier {

    public List<String> normalizeTableBlock(List<String> rawRows) {
        if (rawRows == null || rawRows.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (String row : rawRows) {
            String line = row.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (!line.startsWith("|")) {
                line = "| " + line;
            }
            if (!line.endsWith("|") || line.endsWith("\\|")) {
                line = line + " |";
            }
            normalized.add(line);
        }

        if (normalized.size() >= 2) {
            String headerInner = normalized.get(0).substring(1, normalized.get(0).length() - 1);
            int colCount = headerInner.split("(?<!\\\\)\\|", -1).length;

            // normaliza a linha separadora (índice 1) garantindo no mínimo 3 traços por coluna
            String secondInner = normalized.get(1).substring(1, normalized.get(1).length() - 1).trim();
            if (secondInner.matches("^[|\\s:-]+$")) {
                String[] sepCells = secondInner.split("\\|", -1);
                StringBuilder sepBuilder = new StringBuilder("|");
                for (int i = 0; i < colCount; i++) {
                    String cell = (i < sepCells.length) ? sepCells[i].trim() : "---";
                    if (!cell.matches("^:?-{3,}:?$")) {
                        boolean left = cell.startsWith(":");
                        boolean right = cell.endsWith(":");
                        if (left && right) {
                            cell = ":---:";
                        } else if (left) {
                            cell = ":---";
                        } else if (right) {
                            cell = "---:";
                        } else {
                            cell = "---";
                        }
                    }
                    sepBuilder.append(" ").append(cell).append(" |");
                }
                normalized.set(1, sepBuilder.toString());
            }

            // garante que todas as linhas de dados tenham exatamente 'colCount' colunas
            for (int r = 2; r < normalized.size(); r++) {
                String rowInner = normalized.get(r).substring(1, normalized.get(r).length() - 1);
                String[] cells = rowInner.split("(?<!\\\\)\\|", -1);
                if (cells.length < colCount) {
                    StringBuilder fixedRow = new StringBuilder("|");
                    for (int c = 0; c < colCount; c++) {
                        String val = (c < cells.length) ? cells[c].trim() : "";
                        fixedRow.append(" ").append(val).append(" |");
                    }
                    normalized.set(r, fixedRow.toString());
                }
            }
        }

        return normalized;
    }
}