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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Slf4j
@Component
@RequiredArgsConstructor
public class DLibraService {

  private final SoundscribeConfiguration soundscribeConfiguration;

  public int uploadCollection(File musicXml, List<File> fileCollection, Integer id)
      throws IOException {
    File mainDirectory = new File(soundscribeConfiguration.getUploadDirectory());
    if (mainDirectory.exists()) {
      deleteDir(mainDirectory);
    }

    boolean mainDirectoryCreated = mainDirectory.mkdir();
    File audioDirectory;
    if (mainDirectoryCreated) {
      audioDirectory = new File(mainDirectory.getAbsolutePath() + "/Audio");
      boolean audioDirectoryCreated = audioDirectory.mkdir();
      if (audioDirectoryCreated) {
        createPropertiesForPublication(musicXml, mainDirectory, id);

        copyFileToDir(musicXml, audioDirectory);

        if (fileCollection != null) {
          for (File file : fileCollection) {
            copyFileToDir(file, audioDirectory);
          }
        }
      } else {
        throw new IOException();
      }
    } else {
      throw new IOException();
    }

    ProcessBuilder pb =
        new ProcessBuilder(
            "sh",
            soundscribeConfiguration.getPathToDLibraScript() + "/dlibra-cmdln.sh",
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
    throw new IOException();
  }

  private int getIdFromLine(String line) {
    String[] lineParts = line.split("/");
    return Integer.parseInt(lineParts[lineParts.length - 1]);
  }

  private void copyFileToDir(File file, File dir) throws IOException {
    Files.copy(file.toPath(), Paths.get(dir.getPath() + "/" + file.getName()));
  }

  /**
   * Deletes directory with its content.
   *
   * @param dir directory to delete
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

  private String getSongNameFromMusicxmlFile(File musicxml) throws IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    Document document;
    try {
      builder = factory.newDocumentBuilder();
      document = builder.parse(musicxml);
    } catch (Exception e) {
      throw new IOException("Failed to load song name from musicXml file.", e);
    }
    document.getDocumentElement().normalize();
    return document.getElementsByTagName("movement-title").item(0).getTextContent();
  }

  private void createPropertiesForPublication(File musicxml, File mainDirectory, Integer id)
      throws IOException {

    String mainFileName = musicxml.getName();
    String title = getSongNameFromMusicxmlFile(musicxml);

    createPublicationProperties(mainDirectory.getAbsolutePath(), title, mainFileName, id);
    createMetaData(mainDirectory.getAbsolutePath(), title);
  }

  private void createPublicationProperties(
      String rootPath, String publicationName, String mainFileName, Integer id) throws IOException {
    File propertiesFile = new File(rootPath + "/publication.properties");
    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(propertiesFile));
    fileWriter.write("publication.name=" + publicationName + "\n");
    fileWriter.write("publication.mainFormat=Audio\n");
    fileWriter.write("publication.mainFile.Audio=Audio/" + mainFileName + "\n");
    fileWriter.newLine();
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

  private void createMetaData(String rootPath, String title) throws IOException {
    File metadataFile = new File(rootPath + "/metadata.properties");
    BufferedWriter fileWriter = new BufferedWriter(new FileWriter(metadataFile));
    fileWriter.write("pl.Title.0=" + title + "\n");
    fileWriter.write("pl.Description.0=0 min 20 sec\n");
  }
}
