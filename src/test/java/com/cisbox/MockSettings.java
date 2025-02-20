package com.cisbox;

import java.io.*;

import com.helger.commons.annotation.*;
import com.helger.commons.collection.impl.*;
import com.helger.commons.io.file.*;
import com.helger.diver.api.coord.*;
import com.helger.phive.api.executorset.*;
import com.helger.phive.en16931.*;
import com.helger.phive.xml.source.*;

import jakarta.annotation.*;
import lombok.extern.slf4j.*;

@Slf4j
public class MockSettings {
    static final DVRCoordinate VID_CII_2017 = EN16931Validation.VID_CII_1313.getWithVersionLatestRelease();

    static final ValidationExecutorSetRegistry<IValidationSourceXML> VES_REGISTRY = new ValidationExecutorSetRegistry<>();
    static {
        EN16931Validation.initEN16931(VES_REGISTRY);
    }

    @Nonnull
    @Nonempty
    @ReturnsMutableCopy
    public static ICommonsList<File> getAllTestFilesUBL21Invoice() {
        final File ublDir = new File("src/main/resources/static/ubl21");

        // Ensure the directory exists before searching for files
        if (!ublDir.exists()) {
            boolean created = ublDir.mkdirs();
            if (created) {
                log.info("Created missing directory: {}", ublDir.getAbsolutePath());
            } else {
                log.error("Failed to create directory: {}", ublDir.getAbsolutePath());
            }
        }

        final ICommonsList<File> ret = new CommonsArrayList<>();
        for (final File f : new FileSystemRecursiveIterator(ublDir)) {
            if (f.isFile() && f.getName().endsWith(".xml")) {
                ret.add(f);
                log.info("Found UBL 2.1 invoice file: {}", f.getAbsolutePath());
            }
        }

        log.info("Total UBL 2.1 invoice files found: {}", ret.size());
        return ret;
    }
}

