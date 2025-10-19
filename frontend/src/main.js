import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/global.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')

// 添加页面加载动画
const style = document.createElement('style')
style.textContent = `
  /* 页面加载动画 */
  @keyframes pageLoader {
    0% { transform: scale(0.8); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  }
  
  #app {
    animation: pageLoader 0.5s ease-out;
  }
`
document.head.appendChild(style)