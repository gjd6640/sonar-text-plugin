package org.sonar.plugins.txt.checks.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.sonar.plugins.txt.checks.TextSourceFile;

public final class FileIOUtil {
  private static final Logger LOG = LoggerFactory.getLogger(FileIOUtil.class);

  private FileIOUtil() {};


  public static String readFileAsString(final Path path, final int failWhenCharacterCountExceeds) {
    try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
      return readInputStreamToString(fileInputStream, failWhenCharacterCountExceeds, path.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String readFileAsString(final TextSourceFile textSourceFile, final int failWhenCharacterCountExceeds) {
    try (InputStream fileInputStream = textSourceFile.getInputFile().inputStream()) {
      return readInputStreamToString(fileInputStream, failWhenCharacterCountExceeds, textSourceFile.getInputFile().uri().toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The caller MUST CLOSE the object that this method returns. Note that this adds no
   * buffering so it may also make sense for the caller to use: BufferedReader reader = new BufferedReader(inputStreamReader);
   */
  public static InputStreamReader createTolerantInputStreamReader(InputStream inStream) {
    CharsetDecoder decoder = (StandardCharsets.UTF_8).newDecoder().onMalformedInput(CodingErrorAction.IGNORE);
    return new InputStreamReader(inStream, decoder);
  }

  public static String readInputStreamToString(final InputStream fileContentsInputStream, final int failWhenCharacterCountExceeds, String fileLocationDescription) {
    CharBuffer fileContentBuffer = CharBuffer.allocate(failWhenCharacterCountExceeds);

    try (
        Reader inputStreamReader = FileIOUtil.createTolerantInputStreamReader(fileContentsInputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);
    ) {
      int result = reader.read(fileContentBuffer);
      if (result == failWhenCharacterCountExceeds) {
        LOG.warn("Text scanner (RequiredStringNotPresentRegexMatchCheck) maximum scan depth ( " + (failWhenCharacterCountExceeds-1) + " chars) encountered for file '" + fileLocationDescription + "'. Did not check this file AT ALL.");
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
