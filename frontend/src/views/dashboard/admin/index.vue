<template>
  <div class="dashboard-admin-container">
    <div class=" clearfix">
      <pan-thumb style="float: left" :image="avatar"> Su perfil:
        <span class="pan-info-roles" :key='item' v-for="item in roles">{{item}}</span>
      </pan-thumb>
      <div class="info-container">
        <span class="display_name">{{name}}</span>
        <span style="font-size:20px;padding-top:20px;display:inline-block;">Administrador</span>
      </div>
    </div>
    <div>
      <img class="emptyGif" :src="emptyGif">
    </div>
<!--     <el-container>
      <el-main>
        <el-row :gutter="32">
          <el-col :xs="24" :sm="24" :lg="12">
            <div class="chart-wrapper">
              <span>REPORTES PENDIENTES</span>
              <p></p>
              <sgrp-chart style="height: 400px;"></sgrp-chart>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :lg="12">
            <div class="chart-wrapper">
              <span>REPORTES FUERA DE FECHA</span>
              <p></p>
              <sgrv-chart style="height: 400px;"></sgrv-chart>
            </div>
          </el-col>
        </el-row>
      </el-main>
    </el-container> -->
    <travel-map></travel-map>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import PanThumb from '@/components/PanThumb'
// import SgrpChart from './components/ReportePendientePieChart'
// import SgrvChart from './components/ReporteVencidoPieChart'
import TravelMap from './components/TravelMap.vue'

export default {
  name: 'dashboard-admin',
  components: {
    PanThumb,
    // SgrpChart,
    // SgrvChart,
    TravelMap
  },
  computed: {
    ...mapGetters([
      'name',
      'avatar',
      'roles'
    ])
  },
  data () {
    return {
      emptyGif: ''
    }
  },
  methods: {
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.dashboard-admin-container {
  padding: 8px;
  background-color: rgb(240, 242, 245);
  .chart-wrapper {
    background: #fff;
    padding: 8px 8px 0;
    margin-bottom: 8px;
  }
}
	.emptyGif {
		display: block;
		width: 45%;
		margin: 0 auto;
	}

	.dashboard-admin-container {
		background-color: #e3e3e3;
		min-height: 100vh;
		margin-top: -50px;
		padding: 100px 60px 0px;
		.pan-info-roles {
			font-size: 12px;
			font-weight: 700;
			color: #333;
			display: block;
		}
		.info-container {
			position: relative;
			margin-left: 190px;
			height: 150px;
			line-height: 200px;
			.display_name {
				font-size: 48px;
				line-height: 48px;
				color: #212121;
				position: absolute;
				top: 25px;
			}
		}
	}

</style>
