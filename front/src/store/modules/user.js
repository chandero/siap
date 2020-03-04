import { loginByUsername, logout, getUserInfo } from '@/api/login'
import { getToken, setToken, removeToken, removeEmpresaToken } from '@/utils/auth'
import { getEmpresaInfo } from '@/api/empresa'

const user = {
  state: {
    user: '',
    status: '',
    code: '',
    token: getToken(),
    name: '',
    avatar: '',
    introduction: '',
    roles: [],
    setting: {
      articlePlatform: []
    },
    usuario: {
      usua_id: 0,
      usua_email: '',
      usua_nombre: '',
      usua_apellido: '',
      token: ''
    }
  },

  mutations: {
    SET_CODE: (state, code) => {
      state.code = code
    },
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_INTRODUCTION: (state, introduction) => {
      state.introduction = introduction
    },
    SET_SETTING: (state, setting) => {
      state.setting = setting
    },
    SET_STATUS: (state, status) => {
      state.status = status
    },
    SET_NAME: (state, userinfo) => {
      state.name = userinfo.usua_nombre + ' ' + userinfo.usua_apellido
      state.usuario.usua_id = userinfo.usua_id
      state.usuario.usua_email = userinfo.usua_email
      state.usuario.usua_nombre = userinfo.usua_nombre
      state.usuario.usua_apellido = userinfo.usua_apellido
      state.user = userinfo.usua_email
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_USUARIO: (state, usuario) => {
      state.usuario = usuario
      localStorage.setItem('siapusr', JSON.stringify(usuario))
    }
  },

  actions: {
    LoginByUsername({ commit }, userInfo) {
      const username = userInfo.username.trim()
      return new Promise((resolve, reject) => {
        loginByUsername(username, userInfo.password).then(response => {
          const data = response.data
          commit('SET_USUARIO', data)
          setToken(data.token)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    GetUserInfo({ commit, store, state }) {
      return new Promise((resolve, reject) => {
        getUserInfo().then(response => {
          console.log('Info Usuario: ' + JSON.stringify(response.data))
          if (!response.data) {
            reject('error')
          }
          const data = response.data
          commit('SET_ROLES', data.perfil)
          commit('SET_NAME', data)
          commit('SET_AVATAR', require('@/assets/default-user.png'))
          // commit('SET_INTRODUCTION', data.introduction)
          getEmpresaInfo().then(response => {
            if (!response.data) {
              reject('error')
            }
            this.dispatch('SetEmpresa', response.data)
            resolve(response)
          })
        }).catch(error => {
          reject(error)
        })
      })
    },

    LogOut({ commit, state }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          removeToken()
          removeEmpresaToken()
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    FedLogOut({ commit }) {
      return new Promise(resolve => {
        commit('SET_TOKEN', '')
        removeToken()
        removeEmpresaToken()
        resolve()
      })
    },

    ChangeRoles({ dispatch, commit }, role) {
      return new Promise(resolve => {
        // commit('SET_TOKEN', role)
        // setToken(role)
        getUserInfo().then(response => {
          const data = response.data
          commit('SET_ROLES', role)
          commit('SET_NAME', data)
          // commit('SET_AVATAR', data.avatar)
          // commit('SET_INTRODUCTION', data.introduction)
          // ('SetEmpresa', data.empr_id)
          resolve()
        })
      })
    }
  }
}

export default user
