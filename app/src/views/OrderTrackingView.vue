<template>
  <div class="order-tracking-view">
    <div class="tiles-container">
      <div class="tile gradient-blue">
        <div class="header">
          <h1>Order Tracking</h1>
        </div>
        <div class="selection-details">
          <div class="selection-tile">
            <div class="lookup-form product-row">
              <input class="lookup-input" v-model="orderNumber" placeholder="Enter order number" />
              <button class="green-button" @click="search">Find Order</button>
            </div>
            <div v-if="loading" class="lookup-status">Searching...</div>
            <div v-if="error" class="validation-box validation-box--warning">{{ error }}</div>
          </div>
        </div>
      </div>

      <div v-if="order" class="tile gradient-pink order-summary">
        <h2>Order #{{ order.orderId }}</h2>
  <p><strong>Placed:</strong> {{ formatDateTime(order.orderTime) }}</p>
  <p><strong>Estimated Delivery:</strong> {{ formatDateTime(estimatedDeliveryRaw) }}</p>

        <div class="basket-items">
          <div v-for="(treat, idx) in order.basketItems" :key="treat.id || idx" class="cart-item">
            <div class="item-details">
              <p><strong>Items:</strong> {{ treat.products ? treat.products.map(p => p.productName).join(', ') : '' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderTrackingView',
  props: {
    initialOrderNumber: { type: String, default: null },
    initialEstimatedDeliveryTime: { type: String, default: null }
  },
  data() {
    return {
      orderNumber: this.initialOrderNumber || '',
      order: null,
      loading: false,
      error: null,
      // store raw ISO timestamp for estimated delivery and format on render
      estimatedDeliveryRaw: this.initialEstimatedDeliveryTime || null,
      formatter: null
    }
  },
  async created() {
    // create a consistent, human-friendly formatter (weekday, date, time, timezone)
    this.formatter = new Intl.DateTimeFormat(undefined, {
      weekday: 'short', year: 'numeric', month: 'short', day: 'numeric',
      hour: 'numeric', minute: '2-digit', hour12: true, timeZoneName: 'short'
    });

    if (this.orderNumber) await this.search();
  },
  methods: {
    async search() {
      this.loading = true; this.error = null; this.order = null;
      // require numeric id only
      if (!this.orderNumber || isNaN(Number(this.orderNumber))) {
        this.loading = false;
        this.error = 'Please enter a numeric order id.';
        return;
      }
      try {
        const { searchOrder } = await import('../services/orderService');
        const res = await searchOrder(this.orderNumber);
        this.order = res;
        // compute a raw estimated delivery timestamp if not provided
        if (!this.estimatedDeliveryRaw) {
          if (res.estimatedDeliveryTime) {
            this.estimatedDeliveryRaw = res.estimatedDeliveryTime;
          } else if (res.orderTime && res.estDeliveryMinutes) {
            const t = new Date(res.orderTime).getTime() + res.estDeliveryMinutes * 60000;
            this.estimatedDeliveryRaw = new Date(t).toISOString();
          } else {
            this.estimatedDeliveryRaw = null;
          }
        }
      } catch (err) {
        this.error = err.message || 'Failed to find order';
      } finally {
        this.loading = false;
      }
    },
    formatDateTime(value) {
      if (!value) return '—';
      try {
        const d = (typeof value === 'number') ? new Date(value) : new Date(value);
        if (isNaN(d.getTime())) return 'Invalid date';
        return this.formatter ? this.formatter.format(d) : d.toLocaleString();
      } catch (e) {
        return 'Invalid date';
      }
    }
  }
}
</script>

<style scoped>
.order-tracking-view { padding: 2rem; }
.tiles-container { display:flex; flex-direction:column; gap:2rem }

.lookup-input { flex:1; padding:0.75rem; border:1px solid #ddd; border-radius:6px }
.lookup-form { display:flex; gap:8px; width:100%; align-items:center }
.lookup-status { margin-top:0.5rem; color:#666 }
.order-summary p { margin:0.25rem 0 }
.basket-items { margin-top:1rem }
.cart-item { padding:0.75rem; border:1px solid #eee; border-radius:6px; margin-bottom:0.5rem; background:#fff }
.item-details p { margin:0 }
</style>
