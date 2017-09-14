package org.sonar.plugins.text.checks.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileIOUtil {

  private FileIOUtil() {};

  private static final Logger LOG = LoggerFactory.getLogger(FileIOUtil.class);

  public static String readFileAsString(final Path path, final int failWhenCharacterCountExceeds) {
    CharBuffer fileContentBuffer = CharBuffer.allocate(failWhenCharacterCountExceeds);

    File inputFile = path.toFile();
    CharsetDecoder decoder = (StandardCharsets.UTF_8).newDecoder();
    decoder.onMalformedInput(CodingErrorAction.IGNORE);

    try (
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        Reader inputStreamReader = new InputStreamReader(fileInputStream, decoder);
        BufferedReader reader = new BufferedReader(inputStreamReader);
    ) {
      int result = reader.read(fileContentBuffer);
      if (result == failWhenCharacterCountExceeds) {
        LOG.warn("Text scanner (RequiredStringNotPresentRegexMatchCheck) maximum scan depth ( " + (failWhenCharacterCountExceeds-1) + " chars) encountered for file '" + path.toFile().getAbsolutePath() + "'. Did not check this file AT ALL.");
        throw new LargeFileEncounteredException();
      } else {
        fileContentBuffer.flip();
      }

    } catch (BufferOverflowException ex) {
      throw ex;
    } catch (IOException ex){
      throw new RuntimeException(ex);
    }

    return fileContentBuffer.toString();
  }
}
