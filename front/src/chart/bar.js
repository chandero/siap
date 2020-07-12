import { Bar, mixins } from 'vue-chartjs'
const { reactiveProp } = mixins

export default {
  extends: Bar,
  mixins: [reactiveProp],
  props: ['options'],
  name: 'bar-chart',
  mounted() {
    this.renderChart(this.chartData, this.options)
  }
}
