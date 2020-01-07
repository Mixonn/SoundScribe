## Setup

### Vamp
Vamp is an audio processing system for plugins that extract descriptive information from audio data.
- Build and install Vamp SDK from [here](https://vamp-plugins.org/develop.html) (choose the main SDK).
- Build and install jVamp (a JNI interface) from [here](https://code.soundsoftware.ac.uk/projects/jvamp/files).
- Install required Vamp plugins
  - pYIN (plugin for pitch and note tracking) from [here](https://code.soundsoftware.ac.uk/projects/pyin/files)
  
      Choose `Linux 64-bit binary` and put the content of the archive in `/usr/local/lib/vamp`
  
  - Vamp example plugins (plugin from BPM estimation) from [here](https://www.vamp-plugins.org/download.html)
  
      This one should be installed, see if `vamp-example-plugins.*` files are present in `/usr/local/lib/vamp`)

### Converters
We use external software for conversions between some supported music extensions.

- [Verovio](https://github.com/rism-ch/verovio/wiki/Building-instructions#command-line-tool)

  ~~~
  git clone https://github.com/rism-ch/verovio.git
  cd tools
  cmake .
  make
  sudo make install
  ~~~
 
 
- [abc2xml](https://wim.vree.org/svgParse/abc2xml.html)
  Move `soundscribe-be/libs/python/abc2xml.py` to /usr/lib and name it as `abc2xml`, make it executable
  
- [xml2abc](https://wim.vree.org/svgParse/xml2abc.html)
  Move `soundscribe-be/libs/python/xml2abc.py` to /usr/lib and name it as `xml2abc`, make it executable

To verify the installation check if `verovio`, `abc2xml` and `xml2abc` commands are successfully executed from a console.

### Additional required software
- perl
