package br.edu.ifpr.mcp_server.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TablePrettifierTest {

    private TablePrettifier prettifier;

    @BeforeEach
    void setUp() {
        prettifier = new TablePrettifier();
    }

    @Test
    void deveNormalizarBordasESeparadorPreservandoAlinhamento() {
        List<String> input = List.of(
            "Item | Qtd | Valor",
            ":--|--:",
            "Caneta | 2 | 5,00"
        );

        List<String> esperado = List.of(
            "| Item | Qtd | Valor |",
            "| :--- | ---: | --- |",
            "| Caneta | 2 | 5,00 |"
        );

        assertEquals(esperado, prettifier.normalizeTableBlock(input));
    }
}