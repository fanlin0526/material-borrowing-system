/**
 * v-permission 按钮级权限指令
 * 用法: <el-button v-permission="['SUPER_ADMIN','TEACHER']">删除</el-button>
 * 学生看不到只允许教师/超管的按钮
 */

function getUserRole() {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.role || ''
  } catch {
    return ''
  }
}

export default {
  mounted(el, binding) {
    const required = binding.value // ['SUPER_ADMIN', 'TEACHER']
    if (!Array.isArray(required) || required.length === 0) return
    const role = getUserRole()
    if (!required.includes(role)) {
      el.style.display = 'none'
    }
  }
}
