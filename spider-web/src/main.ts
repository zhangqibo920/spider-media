import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { iconMap } from './utils/icon-map'
import './assets/styles/index.scss'

const app = createApp(App)

for (const [key, component] of Object.entries(iconMap)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus, { size: 'default' })

app.mount('#app')
