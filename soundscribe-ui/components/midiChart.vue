<script>
import { Scatter, mixins } from 'vue-chartjs'

export default {
  extends: Scatter,
  mixins: mixins.reactiveProp,
  props: {
    chartdata: {
      type: Object,
      default: null
    },
    options: {
      type: Object,
      default: null
    }
  },
  mounted () {
    this.renderChart(this.chartdata, this.options)
  },
  methods: {
  }
}
</script>
