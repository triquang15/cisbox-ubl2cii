package com.cisbox.util;

import java.io.*;

import com.helger.cii.d16b.*;
import com.helger.commons.state.*;

import un.unece.uncefact.data.standard.crossindustryinvoice._100.*;

/**
 * @author Tri Quang
 */
public class FileUtil {

    public static boolean saveCIIFile(CrossIndustryInvoiceType ciiInvoice, File file) {
        ESuccess success = new CIID16BCrossIndustryInvoiceTypeMarshaller()
                .setFormattedOutput(true)
                .write(ciiInvoice, file);
        return success.isSuccess();
    }
}

