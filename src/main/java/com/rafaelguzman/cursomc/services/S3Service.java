package com.rafaelguzman.cursomc.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

@Service
public class S3Service {

	private Logger LOG = LoggerFactory.getLogger(S3Service.class);

	@Autowired
	private AmazonS3 s3Client;

	@Value("${s3.bucket}")
	private String bucketName;

	public URI uploadFile(MultipartFile multiPartFile) {

		try {
			String filename = multiPartFile.getOriginalFilename();
			InputStream is = multiPartFile.getInputStream();
			String contentType = multiPartFile.getContentType();
			return uploadFile(is, filename, contentType);
		} catch (IOException e) {
			throw new RuntimeException("Erro de IO: " + e.getMessage());
		}
	}

	public URI uploadFile(InputStream is, String filename, String contentType) {

		try {
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(contentType);

			LOG.info("Iniciando o Upload...");
			s3Client.putObject(bucketName, filename, is, objectMetadata);
			LOG.info("Upload finalizado!");
//		} catch (AmazonServiceException e) {
//			LOG.info("AmazonServiceException: " + e.getErrorMessage());
//			LOG.info("Status code: " + e.getErrorCode());
//		} catch (AmazonClientException e) {
//			LOG.info("AmazonClientOException: " + e.getMessage());
//		}
			return s3Client.getUrl(bucketName, filename).toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Erro ao converter de URL para URI");
		}
	}
}
