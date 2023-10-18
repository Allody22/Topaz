package ru.nsu.carwash_server.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import ru.nsu.carwash_server.controllers.FilesController;
import ru.nsu.carwash_server.exceptions.NotInDataBaseException;
import ru.nsu.carwash_server.models.FileEntity;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceIml implements FileService {
    private final Path root = Paths.get("./uploads");

    private final FileRepository fileRepository;


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

    @Transactional
    public FileEntity saveByStatus(MultipartFile file, String description, String status, String newFileName) {
        try {
            Path targetPath = this.root.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);

            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", newFileName.toString()).build().toString();

            FileEntity entity = new FileEntity();
            entity.setDateOfCreation(new Date());
            entity.setName(file.getOriginalFilename());
            entity.setUrl(url);
            entity.setStatus(status);
            entity.setDescription(description);
            entity.setVersion(0);

            return fileRepository.save(entity);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Файл с таким именем уже существует.");
            }
            throw new RuntimeException(e.getMessage());
        }
    }


    @Transactional
    public FileEntity overwriteByStatus(MultipartFile file, String description, String status, FileEntity previousVersion, String newFileName) {
        try {
            int newVersion = previousVersion.getVersion() + 1;

            Path targetPath = this.root.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);


            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", newFileName.toString()).build().toString();

            FileEntity entity = new FileEntity();
            entity.setDateOfCreation(new Date());
            entity.setName(file.getOriginalFilename());
            entity.setUrl(url);
            entity.setStatus(status);
            entity.setDescription(description);
            entity.setVersion(newVersion);

            return fileRepository.save(entity);
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Файл с таким именем уже существует.");
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    @CacheEvict("fileResources")
    public void deleteAndEvict(String filename) {
    }


    @CacheEvict(value = "fileResources", allEntries = true)
    public void evictAllCacheValues() {
    }

    @Transactional
    @Cacheable(value = "fileResources", key = "#filename")
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);

            if (Files.exists(file)) {
                byte[] fileBytes = Files.readAllBytes(file);
                return new ByteArrayResource(fileBytes);
            } else {
                throw new NotInDataBaseException("файлов не найден файл: ", filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }


    public Optional<FileEntity> findByLatestByNameInRepo(String name) {
        return fileRepository.findLatestVersionByName(name);
    }

    public Optional<FileEntity> checkByName(String name) {
        return fileRepository.checkByName(name);
    }

    public Optional<FileEntity> checkByNameAndStatus(String name, String status) {
        return fileRepository.checkByNameAndStatus(name, status);
    }

    public Optional<FileEntity> findLatestByUrlInRepo(String url) {
        return fileRepository.findLatestVersionByURL(url);
    }

    public Optional<FileEntity> checkByUrl(String url) {
        return fileRepository.checkByURL(url);
    }

    public Optional<FileEntity> checkByUrlAndStatus(String url, String status) {
        return fileRepository.checkByURLAndStatus(url, status);
    }

    public Optional<FileEntity> findLatestVersionByStatus(String status) {
        return fileRepository.findLatestVersionByStatus(status);
    }

    public void saveByURL(MultipartFile file, String description, String status) {
        try {
            int newVersion = 0;
            String originalName = file.getOriginalFilename();
            String fileNameWithoutExtension = FilenameUtils.getBaseName(originalName);
            String extension = FilenameUtils.getExtension(originalName);

            String nameWithoutExtension = originalName.substring(0, originalName.lastIndexOf('.'));

            Optional<FileEntity> previousVersion = checkByUrlAndStatus(nameWithoutExtension, status);


            if (previousVersion.isPresent()) {
                newVersion = previousVersion.get().getVersion() + 1;
            }

            String newFileName = fileNameWithoutExtension + "_v" + newVersion + "." + extension;

            Path targetPath = this.root.resolve(newFileName);

            Files.copy(file.getInputStream(), targetPath);


            String url = MvcUriComponentsBuilder
                    .fromMethodName(FilesController.class, "getFile", newFileName.toString()).build().toString();

            // Сохраняем информацию о файле в базе данных
            FileEntity entity = new FileEntity();
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
}