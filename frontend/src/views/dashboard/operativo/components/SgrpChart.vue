<template>
  <div :class="className" :style="{height:height,width:width}"></div>
</template>

<script>
import echarts from 'echarts' // echarts theme
import { debounce } from '@/utils'
import { siap_grafica_reporte_pendiente } from '@/api/grafica'
require('echarts/theme/macarons')

const animationDuration = 6000

export default {
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '300px'
    }
  },
  data () {
    return {
      chart: null
    }
  },
  mounted () {
    this.initChart()
    this.__resizeHanlder = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHanlder)
  },
  beforeDestroy () {
    if (!this.chart) {
      return
    }
    window.removeEventListener('resize', this.__resizeHanlder)
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    initChart () {
      siap_grafica_reporte_pendiente().then(response => {
        var legendData = []
        var seriesData = []
        response.data.forEach(data => {
          legendData.push(data.reti_descripcion)
          seriesData.push({ name: data.reti_descripcion, type: 'bar', barWidth: '20%', data: new Array(data.pendiente), animationDuration })
        })
        this.chart = echarts.init(this.$el, 'macarons')

        this.chart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
            }
          },
          grid: {
            top: 10,
            left: '2%',
            right: '2%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: [{
            type: 'category',
            data: legendData,
            axisTick: {
              alignWithLabel: true
            }
          }],
          yAxis: [{
            type: 'value',
            axisTick: {
              show: true
            }
          }],
          series: seriesData
        })
      })
    }
  }
}
</script>
