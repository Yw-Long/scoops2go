import { createRouter, createWebHistory } from 'vue-router'
import AboutView from '../views/AboutView.vue'
import ScoopsBuilderView from '../views/ScoopsBuilderView.vue'
import CartView from '../views/CartView.vue'
import OrderTrackingView from '../views/OrderTrackingView.vue'
import NotFoundView from '../views/NotFoundView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: AboutView
  },
  {
    path: '/scoops-builder',
    name: 'scoops-builder',
    component: ScoopsBuilderView
  },
  {
    path: '/cart',
    name: 'cart',
    component: CartView,
    props: route => {
      let cart = JSON.parse(route.query.cart || '[]');
      let orderId = null;
      try { orderId = localStorage.getItem('s2g_orderId'); } catch(e) {}
      return { cart, orderId };
    }
  },
  {
  path: '/order-tracking',
  name: 'order-tracking',
  component: OrderTrackingView,
    props: route => ({ initialOrderNumber: route.query.orderNumber, initialEstimatedDeliveryTime: route.query.estimatedDeliveryTime })
  },
  {
    path: '/:catchAll(.*)',
    name: 'NotFound',
    component: NotFoundView
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
