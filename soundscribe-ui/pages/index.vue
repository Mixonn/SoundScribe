<template>
  <section class="section">
    <Score :score="selectedScore" @osmdInit="osmdInit" @scoreLoaded="scoreLoaded" />
    <PlaybackControls
      :playback-engine="pbEngine"
      :score-title="scoreTitle"
    />
  </section>
</template>

<script>
import PlaybackEngine from '../osmd/PlaybackEngine'
import PlaybackControls from '../components/PlaybackControls.vue'
import scores from '../scores'
import Score from '~/components/Score'

export default {
  name: 'HomePage',

  components: {
    Score,
    PlaybackControls
  },
  data () {
    return {
      pbEngine: new PlaybackEngine(),
      scores,
      selectedScore: null,
      osmd: null,
      scoreTitle: '',
      drawer: true
    }
  },
  computed: {},
  methods: {
    osmdInit (osmd) {
      console.log('OSMD init')
      this.osmd = osmd
      this.selectedScore =
                'https://opensheetmusicdisplay.github.io/demo/sheets/MuzioClementi_SonatinaOpus36No3_Part1.xml'
    },
    scoreLoaded () {
      console.log('Score loaded')
      if (this.osmd.sheet.title) { this.scoreTitle = this.osmd.sheet.title.text }
      this.pbEngine.loadScore(this.osmd)
    },
    scoreChanged (scoreUrl) {
      if (this.pbEngine.state === 'PLAYING') { this.pbEngine.stop() }
      this.selectedScore = scoreUrl
    }
  }
}
</script>
