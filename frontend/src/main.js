import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import vPermission from './directives/permission'
import { authStore } from './stores/auth'

async function bootstrap() {
  // 初始化认证状态（从服务端获取真实角色）
  await authStore.init()

  const app = createApp(App)
  app.use(ElementPlus)
  app.use(router)
  app.directive('permission', vPermission)
  app.mount('#app')
}

bootstrap()
