package com.soundscribe.converters


import com.soundscribe.utilities.SoundscribeConfiguration
import spock.lang.Shared
import spock.lang.Specification

class ConverterServiceTest extends Specification {

    @Shared
            dirPath = this.getClass().getClassLoader().getResource("samples").path
    @Shared
            mxlFile = new File(dirPath + "/Etude1.mxl")
    @Shared
            converterService

    void setup() {
        converterService = new ConverterService(Stub(SoundscribeConfiguration), new MidiConverter(Stub(SoundscribeConfiguration),))
    }

    def "ConvertMusicXmlToMei"() {
        when:
        println mxlFile.path
        File mei = converterService.convert(mxlFile, new ConversionFormat("musicxml", "mei"))

        then:
        mei != null
        mei.getAbsolutePath().endsWith("mei")
        mei.exists()
        mei.text.length() == 21000
    }

    def "ConvertMusicXmlToAbc"() {
        when:
        File abc = converterService.convert(mxlFile, new ConversionFormat("musicxml", "abc"))

        then:
        abc != null
        abc.getAbsolutePath().endsWith("abc")
        abc.exists()
        abc.text.length() == 574
    }

    def "ConvertUnsupportedFormat"() {
        when:
        converterService.convert(mxlFile, new ConversionFormat("mei", "abc"))

        then:
        thrown Converter.ConversionNotSupported
    }
}
