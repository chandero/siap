import Cookies from 'js-cookie'

const TokenKey = 'User-Token'
const EmpresaKey = 'Company-Token'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  localStorage.token = token
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  delete localStorage.token
  return Cookies.remove(TokenKey)
}

export function getEmpresaToken() {
  return Cookies.get(EmpresaKey)
}

export function setEmpresaToken(token) {
  return Cookies.set(EmpresaKey, token)
}

export function removeEmpresaToken() {
  return Cookies.remove(EmpresaKey)
}
