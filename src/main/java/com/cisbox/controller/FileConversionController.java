package com.cisbox.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.*;

import org.springframework.http.*;
import org.springframework.http.codec.multipart.*;
import org.springframework.web.bind.annotation.*;
import com.cisbox.exception.FileValidationException;
import com.cisbox.service.FileConversionService;

import java.util.*;

/**
 * @author Tri Quang
 */
@RestController
@RequestMapping("/api/conversion")
@Tag(name = "File Conversion API", description = "API for converting uploaded files")
public class FileConversionController {

    private final FileConversionService fileConversionService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileConversionController(FileConversionService fileConversionService) {
        this.fileConversionService = fileConversionService;
    }

    @Operation(summary = "Convert a file", description = "Converts an uploaded file and returns the result")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File converted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file provided", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "413", description = "File size exceeds limit", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))) })
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, Object>>> convertFiles(@RequestPart("files") Flux<FilePart> files) {

        return files.switchIfEmpty(Mono.error(new FileValidationException("At least one file is required.")))
                .flatMap(file -> {
                    if (file.filename().isEmpty()) {
                        return Mono.error(new FileValidationException("One or more files are empty."));
                    }
                    return file.content().count()
                            .flatMap(size -> {
                                if (size > MAX_FILE_SIZE) {
                                    return Mono.error(new FileValidationException(
                                            "File " + file.filename() + " exceeds maximum allowed size of 10MB."));
                                }
                                return Mono.just(file);
                            });
                })
                .collectList()
                .flatMap(fileList -> fileConversionService.convertFiles(Flux.fromIterable(fileList)));
    }
}
