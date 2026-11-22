package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import tools.jackson.databind.json.JsonMapper;

class McpServer01IT {

    @Test
    void deveListarEExecutarTodasAsFerramentasMarkdown(@TempDir Path tempDir) throws Exception {
        Path testMd = tempDir.resolve("documento_teste.md");
        Files.writeString(
                testMd,
                "#Titulo sem espaco\n\nColuna A | Coluna B\n- | -\nValor 1 | Valor 2\n",
                StandardCharsets.UTF_8
        );

        ServerParameters params = ServerParameters.builder("java")
                .args("-cp", System.getProperty("java.class.path"), "br.edu.ifpr.mcp_server.main.McpServer01SyncStdio")
                .build();

        StdioClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new JsonMapper()));
        try (McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(15))
                .build()) {

            client.initialize();

            ListToolsResult toolsResult = client.listTools();
            assertTrue(toolsResult.tools().stream().anyMatch(t -> t.name().equals("lint_markdown")));
            assertTrue(toolsResult.tools().stream().anyMatch(t -> t.name().equals("format_markdown_file")));
            assertTrue(toolsResult.tools().stream().anyMatch(t -> t.name().equals("export_markdown_to_pdf")));

            // testa lint_markdown
            CallToolResult lintResult = client.callTool(
                    CallToolRequest.builder("lint_markdown")
                                   .arguments(Map.of("filePath", testMd.toString()))
                                   .build()
            );
            assertFalse(Boolean.TRUE.equals(lintResult.isError()));
            String lintOutput = ((TextContent) lintResult.content().get(0)).text();
            assertTrue(lintOutput.contains("MD018"));

            // testa format_markdown_file
            CallToolResult formatResult = client.callTool(
                    CallToolRequest.builder("format_markdown_file")
                                   .arguments(Map.of("filePath", testMd.toString()))
                                   .build()
            );
            assertFalse(Boolean.TRUE.equals(formatResult.isError()));
            Path formattedMd = tempDir.resolve("documento_teste_formatado.md");
            assertTrue(Files.isRegularFile(formattedMd));

            // testa export_markdown_to_pdf
            CallToolResult pdfResult = client.callTool(
                    CallToolRequest.builder("export_markdown_to_pdf")
                                   .arguments(Map.of("filePath", formattedMd.toString()))
                                   .build()
            );
            assertFalse(Boolean.TRUE.equals(pdfResult.isError()));
            Path exportedPdf = tempDir.resolve("documento_teste_formatado.pdf");
            assertTrue(Files.isRegularFile(exportedPdf));
            assertTrue(Files.size(exportedPdf) > 0);
        }
    }

    @Test
    void deveRetornarErroQuandoArquivoNaoExistir(@TempDir Path tempDir) throws Exception {
        Path missingFile = tempDir.resolve("inexistente.md");

        ServerParameters params = ServerParameters.builder("java")
                .args("-cp", System.getProperty("java.class.path"), "br.edu.ifpr.mcp_server.main.McpServer01SyncStdio")
                .build();

        StdioClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new JsonMapper()));
        try (McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(15))
                .build()) {

            client.initialize();

            assertThrows(Exception.class, () -> client.callTool(
                    CallToolRequest.builder("lint_markdown")
                                   .arguments(Map.of("filePath", missingFile.toString()))
                                   .build()
            ));
        }
    }
}