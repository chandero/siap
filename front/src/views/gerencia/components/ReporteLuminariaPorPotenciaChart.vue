<template>
  <div :class="className" :style="{height:height,width:width}"></div>
</template>

<script>
import { Bar } from 'vue-chartjs'
import { siap_grafica_reporte_potencia } from '@/api/grafica'
import { getColor } from '@/utils/color'

export default {
  name: 'sgrpo-chart',
  extends: Bar,
  props: ['chartdata', 'options'],
  data() {
    return {
      brColor: [],
      bgColor: [],
      seriesData: [],
      labels: [],
      values: [],
      data: {
        labels: [],
        datasets: []
      },
      options: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        }
      }
    }
  },
  methods: {
  },
  mounted() {
    siap_grafica_reporte_potencia().then(response => {
      this.seriesData = response.data
      this.seriesData.forEach(item => {
        this.labels.push(item.label)
        this.values.push(item.value)
        const color = getColor()
        this.bgColor.push(color)
        this.brColor.push(color)
      })
      this.data.datasets.push({
        label: 'Potencia',
        data: this.values,
        backgroundColor: this.bgColor,
        borderColor: this.brColor,
        borderWidth: 1
      })
      this.renderChart(this.data, this.options)
    }).catch(error => {
      console.log('Error obteniendo datos grafica potencia => ' + error)
    })
  }
}
</script>
