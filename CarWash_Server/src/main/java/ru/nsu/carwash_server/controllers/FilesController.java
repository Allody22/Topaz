package ru.nsu.carwash_server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
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
import ru.nsu.carwash_server.models.FileEntity;
import ru.nsu.carwash_server.payload.response.MessageResponse;
import ru.nsu.carwash_server.services.FileServiceIml;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/api/files")
public class FilesController {

    private final FileServiceIml fileService;

    @Autowired
    public FilesController(FileServiceIml fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload_file_v1")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('ADMINISTRATOR')")
    @Transactional
    public ResponseEntity<?> uploadImage(@Valid @RequestParam("file") MultipartFile file,
                                         @Valid @Size(max = 255) @RequestPart("description") String description,
                                         @Valid @RequestPart("status") String status) {

        String decodedDescription = new String(description.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        String decodedStatus = new String(status.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        Optional<FileEntity> previousVersion = fileService.findLatestVersionByStatus(decodedStatus);
        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);

        if (previousVersion.isPresent()) {
            String url = previousVersion.get().getUrl();
            String actualURL = url.substring(url.lastIndexOf("/") + 1);
            fileService.deleteAndEvict(actualURL);

            int newVersion = previousVersion.get().getVersion() + 1;
            String newFileName = status + "_v" + newVersion + "." + extension;
            fileService.overwriteByStatus(file, decodedDescription, decodedStatus, previousVersion.get(), newFileName);
        } else {
            String newFileName = status + "_v" + 0 + "." + extension;
            fileService.saveByStatus(file, decodedDescription, decodedStatus, newFileName);
        }
        log.info("upload_file_v1.Image with description: '{}' and status: '{}' successfully uploaded", decodedDescription, decodedStatus);

        return ResponseEntity.ok(new MessageResponse("Изображение успешно добавлено"));
    }

    @GetMapping("/get_all")
    @Transactional
    public ResponseEntity<Set<FileEntity>> getListFiles() {

        Set<FileEntity> fileEntityInfos = new HashSet<>();
        fileService.loadAll().forEach(path -> {
            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String nameWithoutExtensionAndVersion = fileName.substring(0, fileName.lastIndexOf('_'));

            fileService.checkByName(nameWithoutExtensionAndVersion).ifPresent(fileEntityInfos::add);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileEntityInfos);
    }

    @GetMapping("/sales/get_all")
    @Transactional
    public ResponseEntity<?> getSales() {
        Set<FileEntity> fileEntityInfos = new HashSet<>();
        fileService.loadAll().forEach(path -> {

            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", path.getFileName().toString()).build().toString();
            String fileName = url.substring(url.lastIndexOf("/") + 1);

            String nameWithoutExtensionAndVersion = fileName.substring(0, fileName.lastIndexOf('_'));

            fileService.checkByUrlAndStatus(nameWithoutExtensionAndVersion, "sale").ifPresent(fileEntityInfos::add);
        });

        return ResponseEntity.status(HttpStatus.OK).body(fileEntityInfos);
    }

    @GetMapping("/get/{filename:.+}")
    @Transactional
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {

        Resource file = fileService.load(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @DeleteMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        fileService.evictAllCacheValues();
        return ResponseEntity.ok(new MessageResponse("Кэш успешно очищен"));
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
