package org.sonar.plugins.text.checks.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIOUtil {

  
  public static String readFileAsString(Path path, int failWhenCharacterCountExceeds) {
    CharBuffer fileContentBuffer = CharBuffer.allocate(failWhenCharacterCountExceeds);

    try ( BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8) ) {
      int result = reader.read(fileContentBuffer);
      if (result == failWhenCharacterCountExceeds) {
        System.out.println("Text scanner (RequiredStringNotPresentRegexMatchCheck) maximum scan depth ( " + (failWhenCharacterCountExceeds-1) + " chars) encountered for file '" + path.toFile().getAbsolutePath() + "'. Did not check this file AT ALL.");
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
