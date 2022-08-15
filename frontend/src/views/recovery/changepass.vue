<template>
  <div class="changepass-container">
    <el-form class="changepass-form" autoComplete="on" :model="changePassForm" ref="changePassForm" label-position="left">
      <div class="title-container">
        <h3 class="title">{{$t('changepass.title')}}</h3>
        <lang-select class="set-language"></lang-select>
      </div>
      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input name="password" :type="passwordType" v-model="changePassForm.password" :placeholder="$t('changepass.password')" />
        <span class="show-pwd" @click="showPwd">
          <svg-icon icon-class="eye" />
        </span>
      </el-form-item>

      <el-form-item prop="repassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input name="repassword" :change="validatePassword()" :type="repasswordType" v-model="changePassForm.repassword" :placeholder="$t('changepass.repassword')" />
        <span class="show-repwd" @click="showRePwd">
          <svg-icon icon-class="eye" />
        </span>
      </el-form-item>
      <p></p>
      <el-button :disabled="!send" type="primary" style="width:100%;margin-bottom:30px;" :loading="loading" @click.native.prevent="handleSend">{{$t('changepass.change')}}</el-button>
    </el-form>
  </div>
</template>

<script>
import LangSelect from '@/components/LangSelect'
import { linkValidator, changePassword } from '@/api/usuario'

export default {
  components: { LangSelect },
  data () {
    return {
      changePassForm: {
        password: '',
        repassword: ''
      },
      enla_uuid: '',
      send: false,
      passwordType: 'password',
      repasswordType: 'password',
      loading: false
    }
  },
  methods: {
    handleSend () {
      this.loading = true
      changePassword(this.enla_uuid, this.changePassForm.password).then(response => {
        this.loading = false
        if (response.status === 200) {
          this.$router.push({ path: '/login' })
        } else {
          this.$router.push({ path: '/401' })
        }
      }).catch(error => {
        this.loading = false
        console.log(error)
      })
    },
    showPwd () {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
    },
    showRePwd () {
      if (this.repasswordType === 'password') {
        this.repasswordType = ''
      } else {
        this.repasswordType = 'password'
      }
    },
    validatePassword () {
      if (this.changePassForm.password === this.changePassForm.repassword) {
        this.send = true
      } else {
        this.send = false
      }
    }
  },
  mounted () {
    this.enla_uuid = this.$route.params.e
    linkValidator(this.enla_uuid).then(response => {
      if (response.status === 404) {
        console.log('link e 404')
        this.$router.push({ path: '/404', replace: true, query: { noGoBack: true } })
      }
    }).catch(() => {
      console.log('link e 404')
      this.$router.push({ path: '/404', replace: true, query: { noGoBack: true } })
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
$bg:#2d3a4b;
$light_gray:#eee;

/* reset element-ui css */
.changepass-container {
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

.changepass-container {
  position: fixed;
  height: 100%;
  width: 100%;
  background-color: $bg;
  .changepass-form {
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

  .show-repwd {
    position: absolute;
    right: 10px;
    top: 7px;
    font-size: 16px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
  }

}
</style>
