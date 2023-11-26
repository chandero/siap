<template>
  <div class="empresa-container">
<!--       <el-form class="empresa-form" autoComplete="on" :rules="empresaRules" ref="empresaForm" label-position="left">
        <div class="title-container">
            <h3  class="title">{{$t('empresa.title')}}</h3>
        </div>
        <el-form-item prop="empresa">
            <span class="svg-container svg-container_empresa">
                <svg-icon icon-class="industry" />
            </span>
            <el-select class="select_empresa" v-model="empresa.empr_id" name="empresa" :placeholder="$t('empresa.select')">
                <el-option v-for="empresa in empresas" :key="empresa.empr_id" :label="empresa.empr_sigla" :value="empresa.empr_id" >
                </el-option>
            </el-select>
        </el-form-item>
        <el-button :disabled="selected" type="primary" style="width:100%;margin-bottom:30px;" :loading="loading" @click.native.prevent="handleSelect">{{$t('empresa.submit')}}</el-button>
      </el-form> -->
  </div>
</template>
<script>
import { getEmpresas } from '@/api/empresa'
import settings from '@/settings'

export default {
  name: 'empresa',
  data () {
    return {
      empresa: {
        empr_id: 1,
        empr_descripcion: '',
        empr_sigla: ''
      },
      empresaRules: {
        empresa: [{ required: true, trigger: 'blur' }]
      },
      empresas: [],
      loading: false,
      selected: false
    }
  },

  created () {
    getEmpresas().then(response => {
      this.empresas = response.data
      this.empresa.empr_id = this.empresas[0].empr_id
      this.handleSelect()
    }).catch(() => {
    })
  },
  methods: {
    handleSelect () {
      this.loading = true
      this.$store.dispatch('SetEmpresa', this.empresa).then(response => {
        this.loading = false
        this.empresas.filter(empresa => {
          if (empresa.empr_id === this.empresa.empr_id) {
            document.title = settings.title + ' - ' + empresa.empr_sigla
          }
        })
        this.$router.push({ path: '/dashboard' })
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray:#889aa4;
$light_gray:#eee;

.empresa-container {
  position: fixed;
  height: 100%;
  width: 100%;
  background-size: cover;
  background-image: url('~@/assets/img/background_login.jpg');
  .empresa-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 520px;
    padding: 35px 35px 15px 35px;
    margin: 120px auto;
  }
  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;
    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }
  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
    &_login {
      font-size: 20px;
    }
  }
  .select_empresa {
    width: 100%;
  }
  .title-container {
    position: relative;
    .title {
      font-size: 26px;
      font-weight: 400;
      color: $light_gray;
      margin: 0px auto 40px auto;
      text-align: center;
      font-weight: bold;
    }
    .set-language {
      color: #fff;
      position: absolute;
      top: 5px;
      right: 0px;
    }
  }
  .show-pwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }
  .thirdparty-button {
    position: absolute;
    right: 35px;
    bottom: 28px;
  }
}
</style>
