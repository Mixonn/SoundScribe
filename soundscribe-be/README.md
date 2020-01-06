## Setup

### Vamp
Vamp is an audio processing system for plugins that extract descriptive information from audio data.
- Install Vamp SDK from [here](https://vamp-plugins.org/develop.html).
- Install jVamp (a JNI interface) from [here](https://code.soundsoftware.ac.uk/projects/jvamp/files).
- Install required Vamp plugins
  - pYIN (plugin for pitch and note tracking) from [here](https://code.soundsoftware.ac.uk/projects/pyin/files)
  - Vamp example plugins (this one should be installed)

### Converters
We use external software to convert between most popular music extensions.
- [Verovio](https://github.com/rism-ch/verovio/wiki/Building-instructions#command-line-tool)
- [abc2xml](https://wim.vree.org/svgParse/abc2xml.html)

  Move libs/python/abc2xml.py to /usr/lib/abc2xml and make it executable
  
- [xml2abc](https://wim.vree.org/svgParse/xml2abc.html)

  Move libs/python/xml2abc.py to /usr/lib/xml2abc and make it executable
- ~~mei2musicxml (https://github.com/gburlet/musicxml-mei-conversion/)~~

  ~~You have to install https://github.com/DDMAL/libmei (including Python bindings) to make it work~~

To verify successful installation check if `verovio`, `abc2xml` and `xml2abc` commands are correctly executed from command-line.

### Additional required software
- perl