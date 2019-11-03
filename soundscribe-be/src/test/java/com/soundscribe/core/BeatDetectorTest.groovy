package com.soundscribe.core

import spock.lang.Shared
import spock.lang.Specification

class BeatDetectorTest extends Specification {

    @Shared testPath

    void setup() {
        testPath = getClass().getClassLoader().getResource('.').path
    }

    void cleanup() {
    }

    def "Works as expected on TP0264A_01"() {
        given:
            String mp3Path = testPath + 'samples/TP0264A_01.mp3'
            BeatDetector beatDetector = new BeatDetector(mp3Path)

        when:
            Optional<Integer> bpm = beatDetector.analyzeTrack()

        then:
            bpm.isPresent()
            bpm.get() == 124
    }

    def "Works as expected on TP0264A_02"() {
        given:
        String mp3Path = testPath + 'samples/TP0264A_02.mp3'
        BeatDetector beatDetector = new BeatDetector(mp3Path)

        when:
        Optional<Integer> bpm = beatDetector.analyzeTrack()

        then:
        bpm.isPresent()
        bpm.get() == 143
    }

    def "Returns empty on non-existent file"() {
        given:
        String mp3Path = testPath + 'samples/doesNotExist.mp3'
        BeatDetector beatDetector = new BeatDetector(mp3Path)

        when:
        Optional<Integer> bpm = beatDetector.analyzeTrack()

        then:
        !bpm.isPresent()
    }
}
