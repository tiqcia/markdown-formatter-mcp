package br.edu.ifpr.mcp_server.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MarkdownFilePathsTest {

    @Test
    void deveValidarEResolverCaminhoDeArquivoExistente(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("nota.md");
        Files.writeString(file, "# Teste");

        Path resolved = MarkdownFilePaths.validateAndResolvePath(file.toString());
        assertEquals(file.toAbsolutePath().normalize(), resolved);
    }

    @Test
    void deveLancarExcecaoParaCaminhoNuloVazioOuInexistente(@TempDir Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> MarkdownFilePaths.validateAndResolvePath(null));
        assertThrows(IllegalArgumentException.class, () -> MarkdownFilePaths.validateAndResolvePath("   "));
        assertThrows(IllegalArgumentException.class, () -> MarkdownFilePaths.validateAndResolvePath(tempDir.resolve("nao_existe.md").toString()));
    }

    @Test
    void deveExtrairExtensaoEMontarCaminhoDeSaida(@TempDir Path tempDir) {
        Path original = tempDir.resolve("relatorio.md");
        assertEquals(".md", MarkdownFilePaths.getExtension(original));
        assertEquals(tempDir.resolve("relatorio_formatado.md"), MarkdownFilePaths.buildOutputPath(original, "_formatado", ".md"));
        assertEquals(tempDir.resolve("relatorio.pdf"), MarkdownFilePaths.buildOutputPath(original, "", ".pdf"));
    }
}