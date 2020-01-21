package com.soundscribe.converters;

import com.google.common.io.Files;
import com.soundscribe.utilities.CommonUtil;
import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CrossPlatformConverter {

  private final File input;
  private final SoundscribeConfiguration soundscribeConfiguration;
  private String directory;

  CrossPlatformConverter(File input, SoundscribeConfiguration soundscribeConfiguration) {
    this.soundscribeConfiguration = soundscribeConfiguration;
    this.input = input;
    try {
      this.directory = input.getParentFile().getAbsolutePath();
    } catch (NullPointerException e) {
      try {
        this.directory = new File(".").getCanonicalPath();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  File convertMusicXmlToMei() {
    boolean isSuccess =
        executeCommand("verovio", input.getName(), "-f", "musicxml", "-t", "mei", "-a");
    if (isSuccess) {
      String meiFilename =
          directory + "/" + Files.getNameWithoutExtension(input.getName()) + ".mei";
      return new File(meiFilename);
    } else {
      return null;
    }
  }

  File convertMeiToMusicXml() {
    String mxlFilename =
        directory + "/" + Files.getNameWithoutExtension(input.getName()) + ".musicxml";
    boolean isSuccess = executeCommand("meitomusicxml", input.getName(), mxlFilename);
    if (isSuccess) {
      return new File(mxlFilename);
    } else {
      return null;
    }
  }

  File convertMusicXmlToAbc() {
    String abcFilename = directory + "/" + Files.getNameWithoutExtension(input.getName()) + ".abc";
    boolean isSuccess = executeCommand("xml2abc", input.getName(), "-o", directory);
    isSuccess &=
        executeCommand(
            "perl", "-i", "-pe", "s/(\\S)(#)/^$1/g", abcFilename); // Fix # occurrences in abc
    if (isSuccess) {
      File abcFile = new File(abcFilename);
      File tempFile = new File(abcFile.getAbsolutePath() + ".tmp");

      try (BufferedReader reader = new BufferedReader(new FileReader(abcFile));
          BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
        List<String> notes = new ArrayList<>();

        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
          if (currentLine.contains(":")) {
            writer.write(currentLine + System.lineSeparator());
          } else {
            String trimmedLine = currentLine.trim();
            notes.addAll(Arrays.asList(trimmedLine.split(" ")));
          }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
          stringBuilder.append(notes.get(i));
          stringBuilder.append(" ");
          if (i % soundscribeConfiguration.getMaxNotesInLineAbc() == 0 && i != 0) {
            stringBuilder.append(System.lineSeparator());
            writer.write(stringBuilder.toString());
            stringBuilder = new StringBuilder();
          } else {
            if (i == notes.size() - 1) {
              stringBuilder.append(System.lineSeparator());
              writer.write(stringBuilder.toString());
            }
          }
        }
        boolean successful = tempFile.renameTo(abcFile);
        if (!successful) {
          throw new IOException("Error while creating converting to abc");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return abcFile;
    } else {
      return null;
    }
  }

  File convertAbcToMusicXml() {
    Path tmpPath = Paths.get(directory + "/tmp");
    String baseFileName = CommonUtil.getFileNameWithoutExtension(input);
    File mxlFile = new File(tmpPath.toString(), baseFileName + ".mxl");
    File musicXmlFile = new File(directory + "/" + baseFileName + ".musicxml");
    boolean isSuccess =
        executeCommand("abc2xml", input.getAbsolutePath(), "-o", tmpPath.toString());
    isSuccess &= executeCommand("mv", mxlFile.getAbsolutePath(), musicXmlFile.getAbsolutePath());
    try {
      java.nio.file.Files.deleteIfExists(tmpPath);
    } catch (IOException ignored) {
    }
    if (isSuccess) {
      return musicXmlFile;
    } else {
      throw new RuntimeException("Abc to MusicXML conversion unsuccessful");
    }
  }

  private boolean executeCommand(String... commands) {
    try {
      Process process =
          new ProcessBuilder().command(commands).directory(new File(directory)).start();

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        log.debug("System command exited with non-zero exit code");
        log.debug("Command that failed: " + Arrays.toString(commands));
        return false;
      }
    } catch (IOException | InterruptedException e) {
      log.debug(e.getMessage(), e);
      return false;
    }

    return true;
  }
}
