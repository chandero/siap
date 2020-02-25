import { getEmpresas, setEmpresa } from '@/api/empresa'
import { setToken, setEmpresaToken } from '@/utils/auth'

const empresa = {
  state: {
    role: '',
    empresa: {
      empr_id: 0,
      empr_descripcion: '',
      empr_sigla: '',
      muni_descripcion: '',
      depa_descripcion: ''
    },
    empresas: {}
  },

  mutations: {
    SET_EMPRESAS: (state, data) => {
      state.empresas = data
    },
    SET_EMPRESA: (state, empresa) => {
      state.empresa.empr_id = empresa.empr_id
      state.empresa.empr_descripcion = empresa.empr_descripcion
      state.empresa.empr_sigla = empresa.empr_sigla
      state.role = empresa.perfil
      state.empresa.muni_descripcion = empresa.muni_descripcion
      state.empresa.depa_descripcion = empresa.depa_descripcion
      localStorage.setItem('siapempr', empresa.empr_id)
    }
  },

  actions: {
    GetEmpresas({ commit, state }) {
      return new Promise((resolve, reject) => {
        getEmpresas().then(response => {
          const data = response.data
          commit('SET_EMPRESAS', data)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    SetEmpresa({ dispatch, commit }, empresa) {
      return new Promise((resolve, reject) => {
        setEmpresa(empresa.empr_id).then(response => {
          const data = response.data
          setToken(data.token)
          setEmpresaToken(data.token)
          commit('SET_EMPRESA', data)
          dispatch('ChangeRoles', data.perfil)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default empresa
