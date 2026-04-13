<template>
  <div class="cart-view gradient-blue">
    <h1>Your Basket</h1>
    <div v-if="order && order.basketItems && order.basketItems.length">
      <ul>
        <li v-for="(treat, index) in order.basketItems" :key="index" class="cart-item tile">
          <div class="item-details">
            <p><strong>Items:</strong> {{ treat.products.map(p => p.productName).join(', ') }}</p>
          </div>
          <div class="item-price">
            <p>£{{ (treat.products.reduce((s,p) => s + p.price, 0)).toFixed(2) }}</p>
          </div>
        </li>
      </ul>
      <div class="order-summary tile">
        <p><strong>Subtotal:</strong> £{{ order.orderTotal ? (order.orderTotal - order.deliveryCost).toFixed(2) : '0.00' }}</p>
        <p><strong>Delivery:</strong> £{{ order.deliveryCost ? order.deliveryCost.toFixed(2) : '0.00' }}</p>
        <p><strong>Total:</strong> £{{ order.orderTotal ? order.orderTotal.toFixed(2) : '0.00' }}</p>
        <p><strong>Est Delivery:</strong> {{ order.estDeliveryMinutes }} minutes</p>

        <!-- Promotion entry -->
        <div class="promotion-section">
          <label for="promo-input"><strong>Promotion code</strong></label>
          <div class="promo-row">
            <input
              id="promo-input"
              type="text"
              v-model.trim="promoCode"
              placeholder="Enter code"
              @keyup.enter="applyPromotion"
              :disabled="applyingPromotion || !orderId"
            />
            <button
              class="green-button"
              @click="applyPromotion"
              :disabled="!promoCode || applyingPromotion || !orderId"
            >
              {{ applyingPromotion ? 'Applying…' : 'Apply' }}
            </button>
          </div>
          <p
            v-if="promoMessage"
            :class="{'promo-error': promoError, 'promo-success': !promoError}"
            class="promo-feedback"
            role="status"
          >
            {{ promoMessage }}
          </p>
        </div>

        <div style="display:flex;gap:8px;justify-content:center;">
          <button class="green-button" @click="checkout">Checkout</button>
          <button class="red-button" @click="clearBasket">Clear Basket</button>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Your cart is empty.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CartView',
  props: {
    cart: {
      type: Array,
      required: false,
      default: () => []
    },
    orderId: {
      type: [String, Number],
      required: false
    }
  },
  data() {
    return {
      order: null,
      promoCode: '',
      applyingPromotion: false,
      promoMessage: '',
      promoError: false
    };
  },
  async created() {
    if (this.orderId) {
      await this.loadOrder();
    }
  },
  watch: {
    orderId: async function (newId) {
      if (newId) await this.loadOrder();
    }
  },
  methods: {
    async loadOrder() {
      try {
        const { getOrder } = await import('../services/orderService');
        this.order = await getOrder(this.orderId);
      } catch (err) {
        console.error('Failed to load order', err);
      }
    },
    async applyPromotion() {
      if (!this.orderId || !this.promoCode) return;
      this.applyingPromotion = true;
      this.promoMessage = '';
      this.promoError = false;

      try {
        const { updateOrder } = await import('../services/orderService');
        const toastService = (await import('../services/toastService')).default;

        // Adjust payload key to match your backend contract:
        // e.g. { promotionCode }, { promoCode }, { couponCode }, or { promotions: [code] }
        //const payload = { promotion: this.promoCode };
        this.order.promotion = this.promoCode

        const result = await updateOrder(this.order);
        const updated = result && (result.order || result);
        if (updated && updated.promotion) {
          this.order = updated;
          this.promoMessage = 'Promotion applied!';
          this.promoError = false;
          toastService.success('Promotion applied to your order!', 'Promotion');
          // Let parent know order changed (if they track orderId/order state)
          this.$emit('orderUpdated', this.orderId);
        } else {
          const msg = (result && result.message) ? result.message : 'Failed to apply promotion';
          this.promoMessage = msg;
          this.promoError = true;
          toastService.error(msg, 'Promotion');
        }
      } catch (err) {
        console.error('Apply promotion failed', err);
        const toastService = (await import('../services/toastService')).default;
        const msg = err && err.message ? err.message : 'Failed to apply promotion';
        this.promoMessage = msg;
        this.promoError = true;
        toastService.error(msg, 'Promotion Error');
      } finally {
        this.applyingPromotion = false;
      }
    },
    async checkout() {
      try {
        const { checkoutOrder } = await import('../services/orderService');
        const toastService = (await import('../services/toastService')).default;
        const result = await checkoutOrder(this.orderId);
        // API returns { paid: true, transactionId, order }
        if (result && result.paid) {
          const tx = result.transactionId;
          const order = result.order || result;
          // persist current order id briefly for navigation/state
          try { localStorage.setItem('s2g_orderId', order.id); } catch(e) {}
          toastService.success(`Payment accepted — TX ${tx}`, 'Checkout Complete');
          // clear the client-side basket (remove stored order id and in-memory order)
          try { localStorage.removeItem('s2g_orderId'); } catch(e) {}
          this.order = null;
          this.$emit('orderUpdated', null);
          // navigate to order tracking and show recently checked-out order (include estimatedDeliveryTime if provided)
          const est = result.estimatedDeliveryTime || null;
          this.$router.push({ name: 'order-tracking', query: { orderNumber: order.orderId, estimatedDeliveryTime: est } });
        } else {
          const msg = result && result.message ? result.message : 'Payment failed';
          toastService.error(msg, 'Checkout Failed');
        }
      } catch (err) {
        console.error('Checkout failed', err);
        const toastService = (await import('../services/toastService')).default;
        const msg = err && err.message ? err.message : 'Checkout failed';
        toastService.error(msg, 'Checkout Error');
      }
    }
    ,
    async clearBasket() {
      if (!this.orderId) return;
      if (!confirm('Are you sure you want to clear the basket? This will delete the current order.')) return;
      try {
        const { clearOrder } = await import('../services/orderService');
        const toastService = (await import('../services/toastService')).default;
        const res = await clearOrder(this.orderId);
        // remove stored order id and update parent/nav
        try { localStorage.removeItem('s2g_orderId'); } catch (e) {}
        this.order = null;
        this.$emit('orderUpdated', null);
        // Prefer the known orderId; only use res.orderId if present
        const clearedId = (res && (res.orderId || res.id)) || this.orderId;
        toastService.success(`Basket cleared for order ${clearedId}`, 'Basket Cleared');
      } catch (err) {
        console.error('Failed to clear basket', err);
        const toastService = (await import('../services/toastService')).default;
        const msg = err && err.message ? err.message : 'Failed to clear basket';
        toastService.error(msg, 'Clear Basket Error');
      }
    }
  }
}
</script>

<style scoped>
.cart-view {
  padding: 2rem;
  text-align: center;
}

.cart-view h1 {
  margin-bottom: 1rem;
}

.cart-view ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  margin-bottom: 1rem;
}

.item-details p {
  margin: 0.25rem 0;
  font-size: 0.9rem;
  color: #333;
}

.item-price p {
  margin: 0;
  font-size: 1rem;
  color: #333;
  font-weight: bold;
}

.green-button {
  background: linear-gradient(180deg, #3ad29f, #2bb381);
  border: none;
  color: white;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  cursor: pointer;
}

.red-button {
  background: linear-gradient(180deg, #ff7b7b, #ff5a5a);
  border: none;
  color: white;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  cursor: pointer;
}

.promotion-section {
  border-top: 1px solid rgba(0,0,0,0.06);
  margin-top: 0.75rem;
  padding-top: 0.75rem;
}

.promo-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 0.25rem;
}

.promotion-section input {
  flex: 1;
  padding: 0.5rem 0.6rem;
  border: 1px solid #cfd8dc;
  border-radius: 6px;
}

.promo-feedback {
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
}

.promo-success {
  color: #1b5e20;
}

.promo-error {
  color: #b71c1c;
}

</style>