import './assets/main.css'
import './assets/tiles.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import toastService from './services/toastService'

const app = createApp(App)

app.use(router)
app.config.globalProperties.$toast = toastService

app.mount('#app')
