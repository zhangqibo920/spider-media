import { createI18n } from 'vue-i18n'
import zhCN from './lang/zh-CN'
import en from './lang/en'

const savedLang = localStorage.getItem('lang') || 'zh-CN'

const i18n = createI18n({
  legacy: false,
  locale: savedLang,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en': en,
  },
})

export function setLanguage(lang: string) {
  i18n.global.locale.value = lang as any
  localStorage.setItem('lang', lang)
}

export function getCurrentLanguage() {
  return i18n.global.locale.value
}

export default i18n
