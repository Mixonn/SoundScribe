package com.soundscribe.converters

import com.soundscribe.converters.musicxml.converter.MusicXmlConverter
import com.soundscribe.utilities.SoundscribeConfiguration
import spock.lang.Shared
import spock.lang.Specification

class ConverterServiceTest extends Specification {

    @Shared
            mxlPath = this.getClass().getClassLoader().getResource("samples/Etude1.mxl").path

    @Shared
            converterService

    void setup() {
        converterService = new ConverterService(Stub(MidiConverter),
                Stub(MusicXmlConverter),
                Stub(SoundscribeConfiguration))
    }

    def "ConvertMusicXmlToMei"() {
        when:
        File mei = converterService.convertMusicXmlToMei(mxlPath)
        println mei.getAbsolutePath()

        then:
        mei.getAbsolutePath().endsWith("mei")
        mei.exists()
        mei.text.length() == 21000
    }

    def "ConvertMusicXmlToAbc"() {
        when:
        File abc = converterService.convertMusicXmlToAbc(mxlPath)

        then:
        abc.getAbsolutePath().endsWith("abc")
        println abc.getAbsolutePath()
        abc.exists()
        abc.text.length() == 574
    }
}
