package ru.nsu.carwash_server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
import ru.nsu.carwash_server.models.File;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.services.interfaces.FileService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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

        log.info("upload_file_v1.Image with description: '" + decodedDescription +
                "'. And status: '" + decodedStatus + " 'successfully uploaded");

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


    @DeleteMapping("/delete/{filename:.+}")
    @Transactional
    public ResponseEntity<MessageResponse> deleteFile(@Valid @PathVariable String filename) {
        String message;

        try {
            boolean existed = fileService.delete(filename);

            if (existed) {
                message = "Delete the file successfully: " + filename;
                return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(message));
            }

            message = "The file does not exist!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(message));
        } catch (Exception e) {
            message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(message));
        }
    }
}
