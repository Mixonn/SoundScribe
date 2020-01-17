package com.soundscribe.converters


import com.soundscribe.utilities.SoundscribeConfiguration
import org.mockito.Mock
import spock.lang.Shared
import spock.lang.Specification

class ConverterServiceTest extends Specification {

    @Shared
            dirPath = this.getClass().getClassLoader().getResource("samples").path
    @Shared
            musicXmlFile = new File(dirPath + "/Etude1.musicxml")
    @Shared
            abcFile = new File(dirPath + "/Etude2.abc")
    @Mock
            converterService

    void setup() {
        SoundscribeConfiguration soundscribeConfiguration = Mock(SoundscribeConfiguration)
        soundscribeConfiguration.getMaxNotesInLineAbc() >> 10
        converterService = new ConverterService(soundscribeConfiguration, new MidiConverter(soundscribeConfiguration,))
    }

    def "ConvertMusicXmlToMei"() {
        when:
        File mei = converterService.convert(musicXmlFile, new ConversionFormat("musicxml", "mei"))

        then:
        mei != null
        mei.getAbsolutePath().endsWith("mei")
        mei.exists()
        mei.text.length() == 21000
    }

    def "ConvertMusicXmlToAbc"() {
        when:
        File out = converterService.convert(musicXmlFile, new ConversionFormat("musicxml", "abc"))

        then:
        out != null
        out.getAbsolutePath().endsWith("abc")
        out.exists()
        out.text.length() == 575
    }

    def "ConvertAbcToMusicXml"() {
        when:
        File out = converterService.convert(abcFile, new ConversionFormat("abc", "musicxml"))

        then:
        out != null
        out.getAbsolutePath().endsWith("musicxml")
        out.exists()
        out.text.length() == 20857
    }

    def "ConvertUnsupportedFormat"() {
        when:
        converterService.convert(musicXmlFile, new ConversionFormat("mei", "abc"))

        then:
        thrown Converter.ConversionNotSupported
    }
}
