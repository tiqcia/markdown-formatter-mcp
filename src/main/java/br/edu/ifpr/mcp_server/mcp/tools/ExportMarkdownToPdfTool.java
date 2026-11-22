package br.edu.ifpr.mcp_server.mcp.tools;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.edu.ifpr.mcp_server.markdown.MarkdownPdfExporter;
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

public class ExportMarkdownToPdfTool implements McpToolDefinition {
   private static final Logger LOG = LoggerFactory.getLogger(ExportMarkdownToPdfTool.class);

   private final MarkdownPdfExporter pdfExporter = new MarkdownPdfExporter();

   public ExportMarkdownToPdfTool() {}

   @Override
   public void register(SyncSpecification<SingleSessionSyncSpecification> mcp)
               throws Exception {
      // define o esquema para troca de dados
      Map<String, Object> inputSchema =
                     Map.of("type", "object",
                            "properties", Map.of("filePath",
                                                 Map.of("type", "string",
                                                        "description", "Caminho completo do arquivo Markdown (.md) a ser convertido para PDF"
                                                       )
                                                ),
                            "required", List.of("filePath"),
                            "additionalProperties", false
                           );

      // define a ferramenta
      Tool tool = Tool.builder("export_markdown_to_pdf", inputSchema)
                      .title("Exportar Markdown para PDF")
                      .description("Converte um arquivo Markdown (.md) em um documento PDF estilizado (.pdf) no mesmo diretório")
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
         String msg = pdfExporter.exportFileToPdf(filePath);

         // prepara o texto de retorno ao cliente
         TextContent content = TextContent.builder(msg)
                                          .annotations(annotations)
                                          .build();

         // prepara uma notificação ao cliente (opcional)
         LoggingMessageNotification lmn =
              LoggingMessageNotification.builder(LoggingLevel.INFO,
                                                 "PDF exportado a partir de: " + filePath)
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
         LOG.error("Erro ao exportar PDF", ex);
         throw new RuntimeException("ERRO EXECUTANDO FERRAMENTA:\n" +
                                    ex.getMessage());
      }
   }
}