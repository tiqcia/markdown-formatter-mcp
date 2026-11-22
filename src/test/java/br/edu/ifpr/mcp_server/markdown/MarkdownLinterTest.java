package br.edu.ifpr.mcp_server.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarkdownLinterTest {

    private MarkdownLinter linter;

    @BeforeEach
    void setUp() {
        linter = new MarkdownLinter();
    }

    @Test
    void deveDetectarTituloSemEspacoAposCardinal() {
        String input = "#Titulo Incorreto";
        List<LintIssue> issues = linter.lint(input);

        assertEquals(1, issues.size());
        assertEquals("MD018", issues.get(0).rule());
        assertEquals(1, issues.get(0).line());
    }

    @Test
    void deveDetectarLinkVazioSemUrl() {
        String input = "Visite o [IFPR]() para saber mais.";
        List<LintIssue> issues = linter.lint(input);

        assertEquals(1, issues.size());
        assertEquals("MD042", issues.get(0).rule());
    }

    @Test
    void deveDetectarMultiplasLinhasEmBrancoConsecutivas() {
        String input = "Linha 1\n\n\nLinha 2";
        List<LintIssue> issues = linter.lint(input);

        assertEquals(1, issues.size());
        assertEquals("MD012", issues.get(0).rule());
    }

    @Test
    void deveRetornarListaVaziaParaTextoTotalmenteValido() {
        String input = "# Titulo Valido\n\nParagrafo comum com [link](https://ifpr.edu.br).";
        List<LintIssue> issues = linter.lint(input);

        assertTrue(issues.isEmpty());
    }

    @Test
    void deveLidarComEntradaNulaOuVaziaSemLancarExcecao() {
        assertTrue(linter.lint(null).isEmpty());
        assertTrue(linter.lint("   ").isEmpty());
    }
}