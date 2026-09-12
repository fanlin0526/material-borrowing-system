/**
 * v-permission 按钮级权限指令
 * 用法: <el-button v-permission="['SUPER_ADMIN','TEACHER']">删除</el-button>
 * 角色以服务端验证结果为准（通过 authStore）
 */
import { authStore } from '../stores/auth'

export default {
  mounted(el, binding) {
    const required = binding.value // ['SUPER_ADMIN', 'TEACHER']
    if (!Array.isArray(required) || required.length === 0) return
    if (!required.includes(authStore.role)) {
      el.style.display = 'none'
    }
  }
}
