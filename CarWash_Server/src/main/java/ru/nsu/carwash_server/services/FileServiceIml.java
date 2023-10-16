package ru.nsu.carwash_server.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import ru.nsu.carwash_server.controllers.FilesController;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.File;
import ru.nsu.carwash_server.repository.FileRepository;
import ru.nsu.carwash_server.services.interfaces.FileService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceIml implements FileService {
    private final Path root = Paths.get("./uploads");

    private final FileRepository fileRepository;

    private final Map<String, Resource> fileCache = new ConcurrentHashMap<>();

    private static final int MAX_CACHE_SIZE = 10;  // максимальный размер кэша, например 10 файлов

    @Autowired
    public FileServiceIml(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    public Optional<File> findByLatestByNameInRepo(String name) {
        return fileRepository.findLatestVersionByName(name);
    }

    public Optional<File> checkByName(String name) {
        return fileRepository.checkByName(name);
    }

    public Optional<File> checkByNameAndStatus(String name, String status) {
        return fileRepository.checkByNameAndStatus(name, status);
    }

    public Optional<File> findLatestByUrlInRepo(String url) {
        return fileRepository.findLatestVersionByURL(url);
    }

    public Optional<File> checkByUrl(String url) {
        return fileRepository.checkByURL(url);
    }

    public Optional<File> checkByUrlAndStatus(String url, String status) {
        return fileRepository.checkByURLAndStatus(url, status);
    }

    public Optional<File> findLatestVersionByStatus(String status) {
        return fileRepository.findLatestVersionByStatus(status);
    }

    public void saveByStatus(MultipartFile file, String description, String status) {
        try {
            int newVersion = 0;
            String originalName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalName);

            Optional<File> previousVersion = findLatestVersionByStatus(status);

            if (previousVersion.isPresent()) {
                newVersion = previousVersion.get().getVersion() + 1;
            }
            String newFileName = status + "_v" + newVersion + "." + extension;

            Path targetPath = this.root.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);

            // Добавляем файл в кэш
            Resource resource = new UrlResource(targetPath.toUri());
            fileCache.put(newFileName, resource);

            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", newFileName.toString()).build().toString();

            // Сохраняем информацию о файле в базе данных
            File entity = new File();
            entity.setDateOfCreation(new Date());
            entity.setName(file.getOriginalFilename());
            entity.setUrl(url);
            entity.setStatus(status);
            entity.setDescription(description);
            entity.setVersion(newVersion);

            fileRepository.save(entity);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Файл с таким именем уже существует.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }


    public void saveByURL(MultipartFile file, String description, String status) {
        try {
            int newVersion = 0;
            String originalName = file.getOriginalFilename();
            String fileNameWithoutExtension = FilenameUtils.getBaseName(originalName);
            String extension = FilenameUtils.getExtension(originalName);

            String nameWithoutExtension = originalName.substring(0, originalName.lastIndexOf('.'));

            Optional<File> previousVersion = checkByUrlAndStatus(nameWithoutExtension, status);


            if (previousVersion.isPresent()) {
                newVersion = previousVersion.get().getVersion() + 1;
            }

            String newFileName = fileNameWithoutExtension + "_v" + newVersion + "." + extension;

            Path targetPath = this.root.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);


            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", newFileName.toString()).build().toString();

            // Сохраняем информацию о файле в базе данных
            File entity = new File();
            entity.setDateOfCreation(new Date());
            entity.setName(file.getOriginalFilename());
            entity.setUrl(url);
            entity.setStatus(status);
            entity.setDescription(description);
            entity.setVersion(newVersion);

            fileRepository.save(entity);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Resource> loadAllFiles() {
        try {
            return Files.list(root)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return new UrlResource(path.toUri());
                        } catch (MalformedURLException e) {
                            throw new RuntimeException("Error loading file", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error loading files", e);
        }
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    @Transactional
    public boolean delete(String filename) {
        try {
            Path file = root.resolve(filename);
            String nameWithoutExtensionAndVersion = filename.substring(0, filename.lastIndexOf('_'));
            fileRepository.deleteAllByName(nameWithoutExtensionAndVersion);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public void clearCache() {
        fileCache.clear();
    }

    @Transactional
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new NotInDataBaseException("файлов не найден файл: ", filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Transactional
    public Resource loadWithCache(String filename) {
        // Проверка кэша
        Resource cachedFile = fileCache.get(filename);

        if (cachedFile != null) {
            return cachedFile;
        }
        // Если файла нет в кэше (например, при первом запуске приложения после его перезагрузки)
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource; // Возврат файла, даже если он не в кэше
            } else {
                throw new NotInDataBaseException("файлов не найден файл: ", filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
