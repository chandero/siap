<template>
  <div class="recovery-container">
    <el-form v-show="!comingsoon" class="recovery-form" autoComplete="on" :model="recoveryForm"  ref="recoveryForm" label-position="left">
      <div class="title-container">
        <h3 class="title">{{$t('recovery.title')}}</h3>
        <lang-select class="set-language"></lang-select>
      </div>
      <el-form-item prop="username">
        <span class="svg-container svg-container_login">
          <svg-icon icon-class="user" />
        </span>
        <el-input name="username" type="text" v-model="recoveryForm.username" autoComplete="on" :placeholder="$t('recovery.username')" />
      </el-form-item>
      <el-button type="primary" style="width:100%;margin-bottom:30px;" :loading="loading" @click.native.prevent="handleSend">{{$t('recovery.send')}}</el-button>
    </el-form>
    <el-dialog
      title="Información"
      :visible.sync="comingsoon"
      width="30%"
      center>
      <span>En Breve Recibirá un Correo Electrónico con la información de Recuperación...</span>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="cerrar()">Listo!</el-button>
      </span>
    </el-dialog>    
  </div>
</template>

<script>
import { isvalidUsername } from '@/utils/validate'
import { passwordRecovery } from '@/api/usuario'
import LangSelect from '@/components/LangSelect'

export default {
  components: { LangSelect },
  data() {
    return {
      recoveryForm: {
        username: ''
      },
      send: false,
      comingsoon: false
    }
  },
  methods: {
    handleSend() {
      if (!isvalidUsername(this.recoveryForm.username)) {
        this.$notify({
          title: 'Atención',
          message: 'Verifique la dirección de correo electrónico',
          type: 'warning'
        })
      } else {
        passwordRecovery(this.recoveryForm.username).then(response => {
          if (response.status === 200) {
            this.comingsoon = true
          }
        }).catch(error => {
          console.log(error)
        })
      }
    },
    cerrar() {
      this.comingsoon = false
      this.$router.push({ path: '/login' })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
$bg:#2d3a4b;
$light_gray:#eee;

/* reset element-ui css */
.recovery-container {
  .el-input {
    display: inline-block;
    height: 47px;
    width: 85%;
    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: $light_gray;
      height: 47px;
      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: #fff !important;
      }
    }
  }
  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
}
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray:#889aa4;
$light_gray:#eee;

.recovery-container {
  position: fixed;
  height: 100%;
  width: 100%;
  background-color: $bg;
  .recovery-form {
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
  .password-forgot {
    color: #fffff !important;
  }
}
</style>
