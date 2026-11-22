package br.edu.ifpr.mcp_server.markdown;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MarkdownFilePaths {

    private MarkdownFilePaths() {}

    public static Path validateAndResolvePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("O caminho do arquivo não pode ser vazio.");
        }
        Path inputPath = Paths.get(filePath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(inputPath)) {
            throw new IllegalArgumentException("Arquivo não encontrado: " + inputPath);
        }
        return inputPath;
    }

    public static String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(dotIndex) : ".md";
    }

    public static Path buildOutputPath(Path inputPath, String suffix, String newExtension) {
        String fileName = inputPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
        return inputPath.getParent().resolve(baseName + suffix + newExtension);
    }
}