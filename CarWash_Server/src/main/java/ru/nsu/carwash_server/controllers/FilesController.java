package ru.nsu.carwash_server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.nsu.carwash_server.models.File;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.services.interfaces.FileService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/api/files")
public class FilesController {

    private final FileService fileService;

    @Autowired
    public FilesController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload_file_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> uploadImage(@Valid @RequestParam("file") MultipartFile file,
                                         @Valid @RequestPart("description") String description,
                                         @Valid @RequestPart("status") String status) {

        String decodedDescription = new String(description.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        String decodedStatus = new String(status.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        fileService.saveByStatus(file, decodedDescription, decodedStatus);

        log.info("upload_file_v1.Image with description: '{}' and status: '{}' successfully uploaded", decodedDescription, decodedStatus);

        return ResponseEntity.ok(new MessageResponse("Изображение успешно добавлено"));
    }

    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<Set<File>> getListFiles() {

        Set<File> fileInfos = new HashSet<>();
        fileService.loadAll().forEach(path -> {
            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String nameWithoutExtensionAndVersion = fileName.substring(0, fileName.lastIndexOf('_'));

            fileService.checkByName(nameWithoutExtensionAndVersion).ifPresent(fileInfos::add);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @GetMapping("/sales/get_all")
    @Transactional
    public ResponseEntity<?> getSales() {
        Set<File> fileInfos = new HashSet<>();
        fileService.loadAll().forEach(path -> {

            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
            String fileName = url.substring(url.lastIndexOf("/") + 1);

            String nameWithoutExtensionAndVersion = fileName.substring(0, fileName.lastIndexOf('_'));

            fileService.checkByUrlAndStatus(nameWithoutExtensionAndVersion, "sale").ifPresent(fileInfos::add);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @GetMapping("/get/{filename:.+}")
    @Transactional
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = fileService.load(filename);

        String mimeType;
        try {
            mimeType = Files.probeContentType(Paths.get(file.getURI()));
        } catch (IOException e) {
            throw new RuntimeException("Could not determine file type.", e);
        }

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

        @GetMapping("/test/multipart")
        public ResponseEntity<StreamingResponseBody> getFilesAsMultipart() {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("multipart/mixed"))
                    .body(os -> {
                        try {
                            List<Resource> resources = fileService.loadAllFiles(); // Метод для загрузки всех файлов
                            for (Resource resource : resources) {
                                writePart(resource, os);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing multipart response", e);
                        }
                    });
        }

        private void writePart(Resource resource, OutputStream os) throws IOException {
            String header = "--someBoundary\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + resource.getFilename() + "\"\r\n" +
                    "Content-Type: " + Files.probeContentType(Paths.get(resource.getURI())) + "\r\n\r\n";

            os.write(header.getBytes());
            StreamUtils.copy(resource.getInputStream(), os);
            os.write("\r\n".getBytes());
        }



    @GetMapping("/test/get_all")
    public ResponseEntity<Resource> getAllFiles() {
        try {
            // 1. Создание временного ZIP-файла
            Path zipPath = Files.createTempFile("files", ".zip");
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Stream<Path> paths = fileService.loadAll();

                paths.forEach(path -> {
                    try {
                        Resource resource = fileService.load(path.getFileName().toString());
                        ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(resource.getFilename()));
                        zipOut.putNextEntry(zipEntry);
                        StreamUtils.copy(resource.getInputStream(), zipOut);
                        zipOut.closeEntry();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e); // Преобразование в непроверяемое исключение
                    }
                });
            }

            // 2. Возврат ZIP-файла клиенту
            Resource zipResource = new UrlResource(zipPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipResource.getFilename() + "\"")
                    .body(zipResource);

        } catch (IOException e) {
            throw new RuntimeException("Could not create ZIP file.", e);
        }
    }





    @DeleteMapping("/delete/{filename:.+}")
    @Transactional
    public ResponseEntity<MessageResponse> deleteFile(@Valid @PathVariable String filename) {
        String message;

        try {
            boolean existed = fileService.delete(filename);

            if (existed) {
                message = "Удаление файла прошло успешно: " + filename;
                return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(message));
            }

            message = "Такой файл не существует!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(message));
        } catch (Exception e) {
            message = "Невозможно удалить файл: " + filename + ". Ошибка: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(message));
        }
    }
}
