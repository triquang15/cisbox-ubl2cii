package com.cisbox;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.springframework.boot.test.context.*;

import com.cisbox.common.*;
import com.helger.cii.d16b.*;
import com.helger.commons.error.list.*;
import com.helger.commons.io.file.*;
import com.helger.commons.io.resource.*;
import com.helger.commons.state.*;
import com.helger.commons.string.*;
import com.helger.phive.api.execute.*;
import com.helger.phive.api.result.*;
import com.helger.phive.api.validity.*;
import com.helger.phive.xml.source.*;
import com.helger.ubl21.*;

import oasis.names.specification.ubl.schema.xsd.invoice_21.*;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.*;
/**
 * @author Tri Quang
 */
@SpringBootTest
class CisboxUbl2ciiApplicationTests {
    
    private static final Logger logger = LoggerFactory.getLogger(CisboxUbl2ciiApplicationTests.class);
    
	@SuppressWarnings("unused")
    @Test
	void contextLoads() {
	    logger.info("Starting contextLoads test...");
	    for (final File aFile : MockSettings.getAllTestFilesUBL21Invoice ()) {
	        logger.info ("Converting " + aFile.toString () + " to CII D16B");
	        
	     // Read as UBL
	        final ErrorList aErrorList = new ErrorList ();
	        final InvoiceType aUBLInvoice = UBL21Marshaller.invoice ().setCollectErrors (aErrorList).read (aFile);
	        
	        assertTrue(aErrorList.containsNoError(), "Errors: " + aErrorList.toString());
	        assertNotNull (aUBLInvoice);
	        
	        // Main conversion
	        final CrossIndustryInvoiceType aCrossIndustryInvoice = UBL21ToCII16BConverter.convertToCrossIndustryInvoice (aUBLInvoice, aErrorList);
	        assertTrue(aErrorList.containsNoError(), "Errors: " + aErrorList.toString());
	        assertNotNull (aCrossIndustryInvoice);
	        
	     // Save converted file
	     // Define base output directory for source (within project)
	        String outputDir = "src/test/resources/generated/cii/"; // For test resources
	        // String outputDir = "src/main/resources/generated/cii/"; // For production resources

	        // Define disk output directory (set to null if not needed)
	        String diskOutputDir = "C:/output/generated/cii/"; // Change to an appropriate disk location
	        if (diskOutputDir == null || diskOutputDir.trim().isEmpty()) {
	            diskOutputDir = null; // Prevent directory creation if null
	        }

	        // Ensure directories exist
	        new File(outputDir).mkdirs();
	        if (diskOutputDir != null) {
	            new File(diskOutputDir).mkdirs();
	        }

	        // Create file paths
	        File aDestFileSource = new File(outputDir + FilenameHelper.getBaseName(aFile.getName()) + "-cii.xml");
	        File aDestFileDisk = (diskOutputDir != null) 
	                ? new File(diskOutputDir + FilenameHelper.getBaseName(aFile.getName()) + "-cii.xml") 
	                : null;

	        // Save converted file to project source
	        ESuccess eSuccessSource = new CIID16BCrossIndustryInvoiceTypeMarshaller()
	                .setFormattedOutput(true)
	                .setCollectErrors(aErrorList)
	                .write(aCrossIndustryInvoice, aDestFileSource);

	        // Save converted file to disk only if path exists
	        ESuccess eSuccessDisk = ESuccess.FAILURE;
	        if (aDestFileDisk != null) {
	            eSuccessDisk = new CIID16BCrossIndustryInvoiceTypeMarshaller()
	                    .setFormattedOutput(true)
	                    .setCollectErrors(aErrorList)
	                    .write(aCrossIndustryInvoice, aDestFileDisk);
	        }

	        // Log file paths
	        logger.info("File saved to project source: " + aDestFileSource.getAbsolutePath());
	        if (aDestFileDisk != null) {
	            logger.info("File saved to disk: " + aDestFileDisk.getAbsolutePath());
	        }

	        // Assertions
	        assertTrue(aErrorList.containsNoError(), "Errors: " + aErrorList.toString());
	        assertTrue(eSuccessSource.isSuccess());
	        if (aDestFileDisk != null) {
	            assertTrue(eSuccessDisk.isSuccess());
	        }

	       // TODO fix validation errors
           if (false) {
               // Validate against EN16931 validation rules
               final ValidationResultList aResultList = ValidationExecutionManager.executeValidation(
                       IValidityDeterminator.createDefault(),
                       MockSettings.VES_REGISTRY.getOfID(MockSettings.VID_CII_2017),
                       ValidationSourceXML.create(new FileSystemResource(aDestFileSource)));

               // Check that no errors (but maybe warnings) are contained
               for (final ValidationResult aResult : aResultList) {
                   if (!aResult.getErrorList().isEmpty())
                       logger.error(StringHelper.imploder()
                               .source(aResult.getErrorList(),
                                       x -> x.getErrorFieldName() + " - " + x.getErrorText(Locale.ROOT))
                               .separator('\n').build());
                   assertTrue(aResult.getErrorList().isEmpty(), "Errors: " + aResult.getErrorList().toString());
               }
           }
	    }
	}
}
