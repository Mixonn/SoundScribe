<template>
  <div>
    <div v-if="scoreLoading" class="score-progress">
      <v-progress-circular
        :size="60"
        color="primary"
        indeterminate
      />
    </div>
    <div v-show="!scoreLoading" id="osmd-score" class="score" />
  </div>
</template>

<script>
import axios from 'axios'
import { OpenSheetMusicDisplay } from 'opensheetmusicdisplay'

export default {
  props: {
    'score': String
  },
  data () {
    return {
      osmd: null,
      scoreLoading: false
    }
  },
  watch: {
    score (val, oldVal) {
      if (!val || val === oldVal) { return }
      this.loadScore(val)
    }
  },
  mounted () {
    this.osmd = new OpenSheetMusicDisplay(
      document.getElementById('osmd-score')
    )
    this.$emit('osmdInit', this.osmd)
    if (this.score) { this.loadScore(this.score) }
  },
  methods: {
    async loadScore (scoreUrl) {
      this.scoreLoading = true
      const scoreXml = await axios.get(scoreUrl)
      await this.osmd.load(scoreXml.data)
      this.scoreLoading = false
      await this.$nextTick()
      await this.osmd.render()
      this.$emit('scoreLoaded')
    }
  }
}
</script>

<style scoped lang="scss">
.score {
  width: 100%;
  -webkit-box-shadow: 0px 4px 5px 0px rgba(0, 0, 0, 0.4);
  -moz-box-shadow: 0px 4px 5px 0px rgba(0, 0, 0, 0.4);
  box-shadow: 0px 4px 5px 0px rgba(0, 0, 0, 0.4);
}

.score-progress {
  text-align: center;
}
</style>
