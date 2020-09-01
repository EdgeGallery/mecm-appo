package org.edgegallery.mecm.appo.utils;


import com.google.common.io.Files;
import java.io.File;
import java.text.Normalizer;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class FileChecker {

    private static final String REG
            = "[^\\s\\\\/:*?\"<>|](\\x20|[^\\s\\\\/:*?\"<>|])*[^\\s\\\\/:*?\"<>|.]$";

    private static final int MAX_LENGTH_FILE_NAME = 255;
    private static final long MAX_ZIP_FILE_SIZE = 50 * 1024 * 1024L;
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(REG);
    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s");

    private FileChecker() {
    }

    /**
     * Checks file if is invalid.
     *
     * @param file object.
     */
    public static void check(File file) {
        String fileName = file.getName();

        // file name should not contains blank.
        if (fileName != null && WHITE_SPACE_PATTERN.split(fileName).length > 1) {
            throw new IllegalArgumentException(fileName + " :fileName contain blank");
        }

        if (!isAllowedFileName(fileName)) {
            throw new IllegalArgumentException(fileName + " :fileName is Illegal");
        }

        if (file.length() > MAX_ZIP_FILE_SIZE) {
            throw new IllegalArgumentException(fileName + " :fileSize is too big");
        }
    }

    static boolean isAllowedFileName(String originalFilename) {
        return isValid(originalFilename)
                && "csar".equals(Files.getFileExtension(originalFilename.toLowerCase()));
    }

    static boolean isValid(String fileName) {
        if (StringUtils.isEmpty(fileName) || fileName.length() > MAX_LENGTH_FILE_NAME) {
            return false;
        }
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        return FILE_NAME_PATTERN.matcher(fileName).matches();
    }
}
