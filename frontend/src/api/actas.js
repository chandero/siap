import request from '@/utils/request'


export function getActaActualizaciones (repo_id, repo_fecharecepcion, repo_fechasolucion) {
   return request({
       url: `/actas/${repo_id}/${repo_fecharecepcion}/${repo_fechasolucion}`,
       method: 'get'
   })
}
