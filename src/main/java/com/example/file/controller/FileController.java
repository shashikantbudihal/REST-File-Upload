/**
 * 
 */
package com.example.file.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.file.payload.UploadFileResponse;
import com.example.file.service.FileStorageService;

/**
 * @author Shashi
 *
 */
@RestController
public class FileController {

	private static final Logger log = Logger.getLogger("FileController");
	
	@Autowired
	private FileStorageService  fileStorageService;

	/*
	 * upload single file
	 */
	@PostMapping(value = "/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile multipartFile) {
		
		String fileName = fileStorageService.storeFile(multipartFile);
		
		String filedownoadURI = ServletUriComponentsBuilder.
			fromCurrentContextPath().
				path("/downloadFile/").path(fileName).
					toUriString();
		
		return new UploadFileResponse(fileName,filedownoadURI, multipartFile.getContentType(), multipartFile.getSize());
		
	}
	
	 @PostMapping("/uploadMultipleFiles")
	public List<UploadFileResponse> uploadMultiFiles(@RequestParam("files") MultipartFile[] multipartFiles) {
		
		 return Arrays.asList(multipartFiles)
                .stream().map(file -> uploadFile((MultipartFile) file)).collect(Collectors.toList());
		 
	}

	 @GetMapping("/downloadFile/{fileName:.+}")
	 public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,HttpServletRequest req) {
		 
		 Resource resource = fileStorageService.loadFileAsResource(fileName);
		 String contentType = null;
		 try {
			  contentType = req.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 if(contentType == null) {
	            contentType = "application/octet-stream";
	        }
		 
		 
		 return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				 .body(resource);
	 }
	 
}
