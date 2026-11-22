package br.edu.ifpr.mcp_server.markdown;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownFileFormatterTest {

    @Test
    void deveFormatarArquivoComErrosESalvarCopiaFormatada(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("quebrado.md");
        Files.writeString(input, "#Titulo\n\nCol1 | Col2\n- | -\nA | B\n", StandardCharsets.UTF_8);

        MarkdownFileFormatter formatter = new MarkdownFileFormatter();
        String response = formatter.formatMarkdownOnly(input.toString());

        Path expectedOutput = tempDir.resolve("quebrado_formatado.md");
        assertTrue(Files.isRegularFile(expectedOutput));
        assertTrue(response.contains("Relatório de erros corrigidos"));

        String formattedContent = Files.readString(expectedOutput, StandardCharsets.UTF_8);
        assertTrue(formattedContent.contains("# Titulo"));
        assertTrue(formattedContent.contains("| --- | --- |"));
    }

    @Test
    void deveInformarQuandoArquivoOriginalNaoPossuiErros(@TempDir Path tempDir) throws Exception {
        Path input = tempDir.resolve("limpo.md");
        Files.writeString(input, "# Titulo Limpo\n\nTexto normal.\n", StandardCharsets.UTF_8);

        MarkdownFileFormatter formatter = new MarkdownFileFormatter();
        String response = formatter.formatMarkdownOnly(input.toString());

        assertTrue(response.contains("Nenhum problema encontrado no arquivo original"));
    }
}