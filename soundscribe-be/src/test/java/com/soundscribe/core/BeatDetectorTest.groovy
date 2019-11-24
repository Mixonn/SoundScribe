package com.soundscribe.core

import spock.lang.Shared
import spock.lang.Specification

class BeatDetectorTest extends Specification {

    @Shared
            testPath

    void setup() {
        testPath = getClass().getClassLoader().getResource('samples/').path
    }

    void cleanup() {
    }

    def "Works as expected on TP0264A_01"() {
        given:
        String mp3Path = testPath + 'TP0264A_01.mp3'
        BeatDetector beatDetector = new BeatDetector()

        when:
        Integer bpm = beatDetector.analyzeTrack(new File(mp3Path))

        then:
        bpm == 124
    }

    def "Works as expected on TP0264A_02"() {
        given:
        String mp3Path = testPath + 'TP0264A_02.mp3'
        BeatDetector beatDetector = new BeatDetector()

        when:
        Integer bpm = beatDetector.analyzeTrack(new File(mp3Path))

        then:
        bpm == 143
    }

    def "Returns empty on non-existent file"() {
        given:
        String mp3Path = testPath + 'doesNotExist.mp3'
        BeatDetector beatDetector = new BeatDetector()

        when:
        Integer bpm = beatDetector.analyzeTrack(new File(mp3Path))

        then:
        bpm == null
    }
}
