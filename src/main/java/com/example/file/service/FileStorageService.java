/**
 * 
 */
package com.example.file.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.file.exception.FileStorageException;
import com.example.file.property.FileStorageProperties;

/**
 * @author Shashi
 *
 */
@Service
public class FileStorageService {
	
	private final Path fileStorageLocation;
	
	public FileStorageService(FileStorageProperties fileStorageProperties) throws FileStorageException {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
		
		try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
	}

	public String storeFile(MultipartFile multipartFile) {
		String filename = StringUtils.cleanPath(multipartFile.getOriginalFilename());
		
		if(filename.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + filename);
		}
		
		// Copy file to the target location (Replacing existing file with the same name)
        Path targetLocation = this.fileStorageLocation.resolve(filename);
        try {
			Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex 	) {
			// TODO Auto-generated catch block
			throw new FileStorageException("Could not store file " + filename + ". Please try again!", ex);
		}

        return filename;	
	}

	public Resource loadFileAsResource(String fileName) {
		try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
	}

