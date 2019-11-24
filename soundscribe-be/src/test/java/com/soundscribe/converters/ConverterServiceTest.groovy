package com.soundscribe.converters

import com.soundscribe.converters.musicxml.converter.MusicXmlConverter
import com.soundscribe.utilities.SoundscribeConfiguration
import spock.lang.Shared
import spock.lang.Specification

class ConverterServiceTest extends Specification {

    @Shared
            mxlFile = new File(this.getClass().getClassLoader().getResource("samples/Etude1.mxl").path)
    @Shared
            converterService

    void setup() {
        converterService = new ConverterService(Stub(MidiConverter),
                Stub(MusicXmlConverter),
                Stub(SoundscribeConfiguration))
    }

    def "ConvertMusicXmlToMei"() {
        when:
        Optional<File> mei = converterService.convert(mxlFile, new ConversionFormat("musicxml", "mei"))

        then:
        !mei.isEmpty()
        mei.get().getAbsolutePath().endsWith("mei")
        mei.get().exists()
        mei.get().text.length() == 21000
    }

    def "ConvertMusicXmlToAbc"() {
        when:
        Optional<File> abc = converterService.convert(mxlFile, new ConversionFormat("musicxml", "abc"))

        then:
        !abc.isEmpty()
        abc.get().getAbsolutePath().endsWith("abc")
        abc.get().exists()
        abc.get().text.length() == 574
    }

    def "ConvertUnsupportedFormat"() {
        when:
        Optional<File> abc = converterService.convert(mxlFile, new ConversionFormat("mei", "abc"))

        then:
        abc.isEmpty()
    }
}
