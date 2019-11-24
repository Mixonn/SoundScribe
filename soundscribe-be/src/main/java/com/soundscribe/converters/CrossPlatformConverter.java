package com.soundscribe.converters;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
class CrossPlatformConverter {

    private String filename;
    private String directory;

    CrossPlatformConverter(String filename) {
        this.filename = filename;
        try {
            this.directory = new File(filename).getParentFile().getAbsolutePath();
        } catch (NullPointerException e) {
            try {
                this.directory = new File(".").getCanonicalPath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    File convertMusicXmlToMei() {
        boolean isSuccess = convert("verovio", filename, "-f", "musicxml", "-t", "mei", "-a");
        if (isSuccess) {
            String meiFilename = directory + "/" + Files.getNameWithoutExtension(filename) + ".mei";
            return new File(meiFilename);
        } else {
            return null;
        }
    }

    File convertMeiToMusicXml() {
        String mxlFilename = directory + "/" + Files.getNameWithoutExtension(filename) + ".mxl";
        System.out.println(filename + " " + mxlFilename);
        boolean isSuccess = convert("meitomusicxml", filename, mxlFilename);
        if (isSuccess) {
            return new File(mxlFilename);
        } else {
            return null;
        }
    }

    File convertMusicXmlToAbc() {
        String abcFilename = directory + "/" + Files.getNameWithoutExtension(filename) + ".abc";
        boolean isSuccess = convert("xml2abc", filename, "-o", directory);
        if (isSuccess) {
            return new File(abcFilename);
        } else {
            return null;
        }
    }

    File convertAbcToMusicXml() {
        String abcFilename = directory + "/" + Files.getNameWithoutExtension(filename) + ".abc";
        boolean isSuccess = convert("abc2xml", filename, "-o", directory);
        if (isSuccess) {
            return new File(abcFilename);
        } else {
            return null;
        }
    }

    private boolean convert(String... commands) {
        try {
            Process process = executeCommand(commands);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.debug("System command exited with non-zero exit code");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            log.debug(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private Process executeCommand(String... commands) throws IOException {
        return new ProcessBuilder().command(commands).directory(new File(directory)).start();
    }
}
