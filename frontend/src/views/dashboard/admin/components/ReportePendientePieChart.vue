<template>
  <div :class="className" :style="{height:height,width:width}"></div>
</template>

<script>
import echarts from 'echarts' // echarts theme
import { debounce } from '@/utils'
import { siap_grafica_reporte_pendiente } from '@/api/grafica'
require('echarts/theme/macarons')

export default {
  name: 'sgrp-chart',
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
      default: '350px'
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
          seriesData.push({ value: data.pendiente, name: data.reti_descripcion })
        })
        this.chart = echarts.init(this.$el, 'macarons')

        this.chart.setOption({
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b} : {c} ({d}%)'
          },
          legend: {
            left: 'center',
            bottom: '5',
            data: legendData
          },
          calculable: true,
          series: [
            {
              name: 'REPORTES PENDIENTES',
              type: 'pie',
              roseType: 'radius',
              radius: ['40%', '70%'],
              center: ['50%', '45%'],
              data: seriesData,
              animationEasing: 'cubicInOut',
              animationDuration: 2600,
              itemStyle: {
                emphasis: {
                  shadowBlur: 10,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
              }
            }
          ]
        })
      })
    }
  }
}
</script>
