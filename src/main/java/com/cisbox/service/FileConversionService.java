package com.cisbox.service;

import java.io.*;
import java.util.*;

import org.springframework.core.io.buffer.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.http.codec.multipart.*;

import com.cisbox.common.*;
import com.cisbox.util.*;
import com.helger.commons.error.list.*;
import com.helger.ubl21.*;

import reactor.core.publisher.*;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.*;

/**
 * @author Tri Quang
 */
@Service
public class FileConversionService {

    private static final String DISK_OUTPUT_DIR = "C:/cisbox/output/ublcii/";

    public FileConversionService() {
        new File(DISK_OUTPUT_DIR).mkdirs();
    }

    public Mono<ResponseEntity<Map<String, Object>>> convertFiles(Flux<FilePart> fileParts) {
        return fileParts.flatMap(this::processFile) // Process each file reactively
                .collectList() // Collect results
                .map(results -> {
                    Map<String, Object> responseMap = new HashMap<>();
                    List<Map<String, String>> convertedFiles = new ArrayList<>();
                    List<String> errors = new ArrayList<>();

                    for (var result : results) {
                        if (result.containsKey("error")) {
                            errors.add(result.get("error"));
                        } else {
                            convertedFiles.add(result);
                        }
                    }

                    responseMap.put("convertedFiles", convertedFiles);
                    responseMap.put("errors", errors);
                    return ResponseEntity.ok(responseMap);
                });
    }

    private Mono<Map<String, String>> processFile(FilePart filePart) {
        if (!filePart.filename().endsWith(".xml")) {
            return Mono.just(Map.of("error", "File " + filePart.filename() + " is not a valid XML file."));
        }

        return DataBufferUtils.join(filePart.content()) // Join all DataBuffers into one
                .flatMap(dataBuffer -> {
                    try (InputStream inputStream = dataBuffer.asInputStream(true)) {
                        // Read UBL Invoice
                        ErrorList errorList = new ErrorList();
                        var ublInvoice = UBL21Marshaller.invoice()
                                .setCollectErrors(errorList)
                                .read(inputStream);

                        if (ublInvoice == null || !errorList.containsNoError()) {
                            return Mono.just(Map.of("error", "File " + filePart.filename() + " is invalid."));
                        }

                        // Convert to CII
                        CrossIndustryInvoiceType ciiInvoice = UBL21ToCII16BConverter.convertToCrossIndustryInvoice(ublInvoice, errorList);
                        if (ciiInvoice == null || !errorList.containsNoError()) {
                            return Mono.just(Map.of("error", "Conversion failed for file " + filePart.filename()));
                        }

                        // Generate file paths
                        String baseName = filePart.filename().replace(".xml", "");
                        File diskFile = new File(DISK_OUTPUT_DIR + baseName + "-cii.xml");

                        // Save files
                        boolean savedDisk = FileUtil.saveCIIFile(ciiInvoice, diskFile);

                        if (!savedDisk) {
                            return Mono.just(Map.of("error", "Failed to save converted file " + filePart.filename()));
                        }

                        // Store successful conversion info
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", filePart.filename());
                        fileInfo.put("diskFilePath", diskFile.getAbsolutePath());
                        return Mono.just(fileInfo);

                    } catch (Exception e) {
                        return Mono.just(Map.of("error", "Unexpected error processing file " + filePart.filename() + ": " + e.getMessage()));
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                });
    }
}
