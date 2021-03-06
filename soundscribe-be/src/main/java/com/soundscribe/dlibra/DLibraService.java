package com.soundscribe.dlibra;

import com.soundscribe.utilities.SoundscribeConfiguration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLibraService {

  private final SoundscribeConfiguration soundscribeConfiguration;

  /**
   * Uploads folder with all specified files to dLibra. Based on the main musicXml file creates
   * publication metadata.
   *
   * @param musicXml Main publication file
   * @param fileCollection Collection of files which will be uploaded.
   * @param id Publication ID. If no ID is specified, new publication will be added to collection.
   * @return Publication ID.
   * @throws IOException
   */
  public int uploadCollection(File musicXml, List<File> fileCollection, Integer id, String title)
      throws IOException {
    File mainDirectory = new File(soundscribeConfiguration.getUploadDirectory());
    File audioDirectory;

    if (mainDirectory.exists()) {
      deleteDir(mainDirectory);
    }

    if (mainDirectory.mkdir()) {
      audioDirectory = new File(mainDirectory.getAbsolutePath() + "/Audio");
      if (audioDirectory.mkdir()) {
        createPropertiesForPublication(musicXml, mainDirectory, id, title);

        copyFileToDir(musicXml, audioDirectory);
        if (fileCollection != null) {
          for (File file : fileCollection) {
            copyFileToDir(file, audioDirectory);
          }
        }
      } else {
        throw new IOException("Failed to create Audio directory.");
      }
    } else {
      throw new IOException("Failed to create upload directory.");
    }

    return uploadCreatedDictionary(mainDirectory);
  }

  /**
   * Downloads given collection from dLibra. Returns path to the directory with downloaded results.
   *
   * @param id Publication ID
   * @throws IOException if dLibra script failed
   */
  public File downloadCollection(Integer id) throws IOException {
    File mainDirectory = new File(soundscribeConfiguration.getDownloadDirectory());
    File subDirectory;

    if (!mainDirectory.exists()) {
      mainDirectory.mkdir();
    }

    subDirectory = new File(mainDirectory, String.valueOf(id));
    if (subDirectory.exists()) {
      deleteDir(subDirectory);
    }

    if (!subDirectory.mkdir()) {
      throw new IOException("Failed to create subdirectory.");
    }

    Process process =
        new ProcessBuilder(
                "sh",
                soundscribeConfiguration.getPathToDLibraScript() + "dlibra-cmdln.sh",
                "download-objects",
                String.valueOf(id),
                subDirectory.getAbsolutePath())
            .start();

    int status;
    try {
      status = process.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
      status = 1;
    }

    if (status != 0) {
      throw new IOException("DLibra script returned with non-zero code.");
    }
    return subDirectory;
  }

  /**
   * Uploads already prepared dictionary as publication to dLibra.
   *
   * @param mainDirectory Main folder to upload.
   * @return Publication ID.
   * @throws IOException
   */
  private int uploadCreatedDictionary(File mainDirectory) throws IOException {
    ProcessBuilder pb =
        new ProcessBuilder(
            "sh",
            soundscribeConfiguration.getPathToDLibraScript() + "dlibra-cmdln.sh",
            "upload-objects",
            mainDirectory.getAbsolutePath());
    Process p = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    String lineWithId = null;
    while ((line = reader.readLine()) != null) {
      if (line.contains("/publication/")) {
        lineWithId = line;
      }
    }

    if (mainDirectory.exists()) {
      deleteDir(mainDirectory);
    }

    if (lineWithId != null) {
      return getIdFromLine(lineWithId);
    }

    throw new IOException("Failed to upload data to dLibra.");
  }

  /**
   * Cuts publication ID from line generated by dlibra-cmdln script.
   *
   * @param line Line which contains publication ID.
   * @return publication ID.
   */
  private int getIdFromLine(String line) {
    String[] lineParts = line.split("/");
    return Integer.parseInt(lineParts[lineParts.length - 1]);
  }

  /**
   * Copies specified file to given dictionary.
   *
   * @param file File to copy
   * @param dir Dictionary where file will be placed.
   * @throws IOException
   */
  private void copyFileToDir(File file, File dir) throws IOException {
    Files.copy(file.toPath(), Paths.get(dir.getPath() + "/" + file.getName()));
  }

  /**
   * Deletes the directory with its contents.
   *
   * @param dir Directory to delete
   * @throws IOException
   */
  private void deleteDir(File dir) throws IOException {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      if (children != null) {
        for (String fileName : children) {
          deleteDir(new File(dir, fileName));
        }
      }
    }
    Files.delete(dir.toPath());
  }

  /**
   * Creates all necessary properties files to upload publication.
   *
   * @param musicxml Main publication file. Contains all necessary information about song.
   * @param mainDirectory Main folder with all properties and sound files.
   * @param id Publication ID. If no ID is specified, new publication will be added to collection.
   * @throws IOException
   */
  private void createPropertiesForPublication(
      File musicxml, File mainDirectory, Integer id, String title) throws IOException {

    String mainFileName = musicxml.getName();
    createPublicationProperties(mainDirectory.getAbsolutePath(), title, mainFileName, id);
    createMetaData(mainDirectory.getAbsolutePath(), title);
  }

  /**
   * Creates properties file for script which uploads data to dLibra. It contains name, id and other
   * important information about uploaded publication.
   *
   * @param rootPath Directory where file will be created
   * @param publicationName Publication name
   * @param mainFileName Main file of uploaded publication
   * @param id Publication id. If no id is specified, new publication will be added to collection.
   * @throws IOException
   */
  private void createPublicationProperties(
      String rootPath, String publicationName, String mainFileName, Integer id) throws IOException {
    File propertiesFile = new File(rootPath + "/publication.properties");

    try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(propertiesFile))) {
      fileWriter.write(String.format("publication.name=%s%n", publicationName));
      fileWriter.write("publication.mainFormat=Audio\n");
      fileWriter.write(String.format("publication.mainFile.Audio=Audio/%s%n", mainFileName));
      fileWriter.write("publication.actorsRights.Użytkownicy\\ publiczni=pv\n");
      fileWriter.write("publication.collections=172\n");
      fileWriter.write("publication.destination.directoryId=1109\n");
      fileWriter.write("publication.metadataFile=metadata.properties\n");
      fileWriter.write("publication.published=true\n");
      fileWriter.newLine();

      if (id != null) {
        fileWriter.write(String.format("edition.id=%d%n", id));
        fileWriter.write("update.mode=full\n");
      }
    }
  }

  /**
   * Creates properties file with song metadata.
   *
   * @param rootPath Directory where file will be created
   * @param title Song title
   * @throws IOException
   */
  private void createMetaData(String rootPath, String title) throws IOException {
    File metadataFile = new File(rootPath + "/metadata.properties");
    try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(metadataFile))) {
      // fileWriter.write("pl.Title.0=" + title + "\n");
      // fileWriter.write("pl.Description.0=0 min 20 sec\n");
    }
  }
}
