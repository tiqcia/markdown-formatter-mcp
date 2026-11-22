package br.edu.ifpr.mcp_server.mcp.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.markdown.LintIssue;
import br.edu.ifpr.mcp_server.markdown.MarkdownLinter;
import br.edu.ifpr.mcp_server.markdown.MarkdownFilePaths;
import br.edu.ifpr.mcp_server.mcp.utils.copy.McpToolDefinition;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class LintMarkdownTool implements McpToolDefinition {
   private static final Logger LOG = LoggerFactory.getLogger(LintMarkdownTool.class);

   private final MarkdownLinter linter = new MarkdownLinter();

   public LintMarkdownTool() {}

   @Override
   public void register(SyncSpecification<SingleSessionSyncSpecification> mcp)
               throws Exception {
      // define o esquema para troca de dados
      Map<String, Object> inputSchema =
                     Map.of("type", "object",
                            "properties", Map.of("filePath",
                                                 Map.of("type", "string",
                                                        "description", "Caminho completo do arquivo Markdown (.md) a ser auditado"
                                                       )
                                                ),
                            "required", List.of("filePath"),
                            "additionalProperties", false
                           );

      // define a ferramenta
      Tool tool = Tool.builder("lint_markdown", inputSchema)
                      .title("Auditar arquivo Markdown")
                      .description("Analisa um arquivo Markdown (.md) no disco e retorna o relatório de erros de sintaxe e estrutura sem modificar o arquivo")
                      .build();

      // vincula o objeto ferramenta à implementação da ferramenta
      SyncToolSpecification toolSpec =
                            SyncToolSpecification.builder()
                                                 .tool(tool)
                                                 .callHandler(this::run)
                                                 .build();

      // acrescenta a ferramenta ao servidor
      mcp.tools(toolSpec);
   }

   @Override
   public CallToolResult run(McpSyncServerExchange exchange,
                             CallToolRequest request) {
      try {
         List<Role>  roles       = List.of(McpSchema.Role.USER);
         Annotations annotations = Annotations.builder()
                                              .audience(roles)
                                              .priority(1.0)
                                              .build();

         String filePath = String.valueOf(request.arguments().get("filePath"));
         Path inputPath = MarkdownFilePaths.validateAndResolvePath(filePath);
         String originalContent = Files.readString(inputPath, StandardCharsets.UTF_8);
         List<LintIssue> issues = linter.lint(originalContent);

         String msg;
         if (issues.isEmpty()) {
            msg = "Relatório de erros (Lint) para '" + inputPath.getFileName() + "': Nenhum problema encontrado.";
         } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Relatório de erros (Lint) para '").append(inputPath.getFileName())
              .append("' (").append(issues.size()).append(" problema(s)):\n");
            for (LintIssue issue : issues) {
               sb.append("- Linha ").append(issue.line())
                 .append(" [").append(issue.rule()).append("]: ")
                 .append(issue.message()).append("\n");
            }
            msg = sb.toString().stripTrailing();
         }

         // prepara o texto de retorno ao cliente
         TextContent content = TextContent.builder(msg)
                                          .annotations(annotations)
                                          .build();

         // prepara uma notificação ao cliente (opcional)
         LoggingMessageNotification lmn =
              LoggingMessageNotification.builder(LoggingLevel.INFO,
                                                 "Lint executado em: " + inputPath.getFileName())
                                        .logger(this.getClass().getSimpleName())
                                        .build();

         // envia uma notificação ao cliente
         exchange.loggingNotification(lmn);

         // retorna o resultado da ferramenta ao cliente
         return CallToolResult.builder()
                              .content(List.of(content))
                              .build();
      }
      catch (Exception ex) {
         LOG.error("Erro ao auditar arquivo Markdown", ex);
         throw new RuntimeException("ERRO EXECUTANDO FERRAMENTA:\n" +
                                    ex.getMessage());
      }
   }
}