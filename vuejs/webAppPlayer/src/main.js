// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from '@/App'
import router from '@/router'
import store from '@/store'
import Vuelidate from 'vuelidate'
import interceptors from '@/services/interceptors'
import VuexFlash from 'vuex-flash'
import BootstrapVue from 'bootstrap-vue'
 
Vue.use(Vuelidate)
Vue.use(interceptors, { router })
Vue.use(VuexFlash, { mixin: true, duration: 2500 })
Vue.use(BootstrapVue)


Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
