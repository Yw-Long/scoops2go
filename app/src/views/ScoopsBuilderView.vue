<template>
  <div class="scoops-builder-view">
    <div class="tiles-container">
      <div class="tile gradient-blue">
        <div class="header">
          <h1>Build Your Treat!</h1>
          <button class="pink-button" @click="undoSelection">Undo</button>
        </div>
        <div class="selection-details">
          <div class="selection-tile">
            <p><strong>Cone:</strong> {{ selectedCone ? selectedCone.productName : 'None' }}</p>
            <p><strong>Flavours ({{ selectedFlavours.length }}):</strong>
              <span v-if="selectedFlavours.length">{{ selectedFlavours.map(f => f.productName).join(', ') }}</span>
              <span v-else>None</span>
            </p>
            <p><strong>Toppings ({{ selectedToppings.length }}):</strong>
              <span v-if="selectedToppings.length">{{ selectedToppings.map(t => t.productName).join(', ') }}</span>
              <span v-else>None</span>
            </p>
            <p v-if="selectedCone || selectedFlavours.length || selectedToppings.length">
              <strong>Total Price:</strong> £{{ treatTotalPrice.toFixed(2) }}
            </p>
          </div>
          <button
            class="green-button"
            :class="{ 'disabled-button': !treatIsValid }"
            :disabled="!treatIsValid"
            :title="!treatIsValid ? validationReason : 'Add selected items to basket'"
            :aria-disabled="!treatIsValid"
            @click="addToCart"
          >
            {{ treatIsValid ? 'Add to Basket' : 'Add to Basket (disabled)'}}
          </button>
          <div v-if="!treatIsValid" class="validation-box validation-box--warning" role="status" aria-live="polite">
            <strong class="validation-box__title">Action Needed:</strong>
            <p class="validation-box__message">
              <span v-if="!selectedCone">Select 1 cone.</span>
              <span v-else-if="selectedFlavours.length < 1">Select at least 1 flavour.</span>
              <!-- Upper limits removed: selection allowed beyond 3 flavours and 5 toppings -->
            </p>
          </div>
        </div>
      </div>
      <div class="tile gradient-pink selection-section">
        <h2>Select Flavours</h2>
        <div class="flavour-list">
          <div class="flavour-item" v-for="flavour in flavours" :key="flavour.productName">
            <div class="product-row">
              <input type="checkbox" :checked="selectedFlavours.some(f => f.productId === flavour.productId)" @change="selectFlavour(flavour)" />
              <h2 class="product-name">{{ flavour.productName }}</h2>
              <span class="product-price">£{{ flavour.price.toFixed(2) }}</span>
            </div>
            <p class="product-description">{{ flavour.description }}</p>
            <p class="product-ingredients" v-if="flavour.ingredients && flavour.ingredients.length">
              Ingredients:
              <span v-for="(obj, idx) in getIngredientObjects(flavour.ingredients)" :key="obj.text + idx">
                <strong v-if="obj.isAllergen">{{ obj.text }}</strong><span v-else>{{ obj.text }}</span><span v-if="idx !== flavour.ingredients.length - 1">, </span>
              </span>
            </p>
          </div>
        </div>
      </div>
      <div class="tile gradient-green selection-section">
        <h2>Select Toppings</h2>
        <div class="topping-list">
          <div class="topping-item" v-for="topping in toppings" :key="topping.productName">
            <div class="product-row">
              <input type="checkbox" :checked="selectedToppings.some(t => t.productId === topping.productId)" @change="selectTopping(topping)" />
              <span class="product-name"><strong>{{ topping.productName }}</strong></span>
              <span class="product-price">£{{ topping.price.toFixed(2) }}</span>
            </div>
            <p class="product-description">{{ topping.description }}</p>
            <p class="product-ingredients" v-if="topping.ingredients && topping.ingredients.length">
              Ingredients:
              <span v-for="(obj, idx) in getIngredientObjects(topping.ingredients)" :key="obj.text + idx">
                <strong v-if="obj.isAllergen">{{ obj.text }}</strong><span v-else>{{ obj.text }}</span><span v-if="idx !== topping.ingredients.length - 1">, </span>
              </span>
            </p>
          </div>
        </div>
      </div>
      <div class="tile gradient-purple selection-section">
        <h2>Select Cone (required)</h2>
        <div class="cone-list">
          <div class="cone-item" v-for="cone in cones" :key="cone.productName">
            <div class="product-row">
              <input type="radio" name="cone" :checked="selectedCone && selectedCone.productId === cone.productId" @change="selectCone(cone)" />
              <span class="product-name"><strong>{{ cone.productName }}</strong></span>
              <span class="product-price">${{ cone.price.toFixed(2) }}</span>
            </div>
            <p class="product-description">{{ cone.description }}</p>
            <p class="product-ingredients" v-if="cone.ingredients && cone.ingredients.length">
              Ingredients:
              <span v-for="(obj, idx) in getIngredientObjects(cone.ingredients)" :key="obj.text + idx">
                <strong v-if="obj.isAllergen">{{ obj.text }}</strong><span v-else>{{ obj.text }}</span><span v-if="idx !== cone.ingredients.length - 1">, </span>
              </span>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { fetchProducts } from '../services/productService';
import { createOrder, updateOrder, getOrder } from '../services/orderService';
import toastService from '../services/toastService';
import { RouterLink } from 'vue-router';

export default {
  name: 'ScoopsBuilderView',
  components: {
    RouterLink
  },
  data() {
    return {
      products: [],
      flavours: [],
      toppings: [],
      cones: [],
  selectedFlavours: [],
  selectedToppings: [],
  selectedCone: null,
      cart: [],
      ukAllergens: [
        'celery', 'cereals containing gluten', 'crustaceans', "eggs", 'fish',
        'lupin', 'milk', 'molluscs', 'mustard', 'nuts',
        'peanuts', 'sesame seeds', 'soya', 'sulphur dioxide'
      ]
    }
  },
  async created() {
    await this.loadProducts();
  },
  methods: {
    async loadProducts() {
      try {
        this.products = await fetchProducts();
        this.flavours = this.products.filter(p => p.type === 'FLAVOR');
        this.toppings = this.products.filter(p => p.type === 'TOPPING');
        this.cones = this.products.filter(p => p.type === 'CONE');
      } catch (error) {
        console.error('Error loading products:', error);
      }
    },
    getIngredientObjects(ingredients) {
      // Returns [{ text, isAllergen }] for each ingredient
      return ingredients.map(ingredient => {
        const ingredientNorm = ingredient.trim().toLowerCase();
        const isAllergen = this.ukAllergens.some(allergen => {
          const allergenNorm = allergen.trim().toLowerCase();
          return ingredientNorm === allergenNorm;
        });
        return { text: ingredient, isAllergen };
      });
    },
    selectFlavour(flavour) {
      const idx = this.selectedFlavours.findIndex(f => f.productId === flavour.productId);
      if (idx === -1) {
        this.selectedFlavours.push(flavour);
      } else {
        this.selectedFlavours.splice(idx, 1);
      }
    },
    selectTopping(topping) {
      const idx = this.selectedToppings.findIndex(t => t.productId === topping.productId);
      if (idx === -1) {
        this.selectedToppings.push(topping);
      } else {
        this.selectedToppings.splice(idx, 1);
      }
    },
    selectCone(cone) {
      this.selectedCone = cone;
    },
    addToCart() {
      if (!this.treatIsValid) return;
      // Build a Treat payload: name, quantity, products (by id)
      const products = [];
      // cone + flavours + toppings
      if (this.selectedCone) products.push(this.selectedCone);
      this.selectedFlavours.forEach(f => products.push(f));
      this.selectedToppings.forEach(t => products.push(t));

      // Build order payload
      const orderId = localStorage.getItem('s2g_orderId');
      const orderPayload = {
        promotion: null,
        basketItems: [{products: products}]
      };

      // If there's an existing order, we'll PUT with updated basket (append treat)
      const doRequest = async () => {
        try {
          if (!orderId) {
            //orderPayload.basketItems = products; //[treatPayload];
            console.log("basketItems:" + JSON.stringify(orderPayload.basketItems));
            const created = await createOrder(orderPayload);
            localStorage.setItem('s2g_orderId', created.orderId);
            this.$emit('orderUpdated', created);
            // toast success for creation
            toastService.success(`Order ${created.orderId} created and item added to basket`, 'Order Created');
          } else {
            // Fetch current order, append the new treat, then PUT the full order
            try {
              const existing = await getOrder(parseInt(orderId, 10));
              if (!existing || !existing.basketItems) existing.basketItems = [];
              existing.basketItems.push({products: products});
              const updated = await updateOrder(existing);
              this.$emit('orderUpdated', updated);
              // toast success for update
              toastService.success(`Order ${updated.orderId} updated — item added to basket`, 'Order Updated');
            } catch (fetchErr) {
              // If fetching fails (order missing), fallback to creating a new order
              console.warn('Fetching existing order failed, creating new order', fetchErr);
              //orderPayload.basketItems = products;
              const created = await createOrder(orderPayload);
              localStorage.setItem('s2g_orderId', created.orderId);
              this.$emit('orderUpdated', created);
              toastService.success(`Order ${created.orderId} created and item added to basket`, 'Order Created');
            }
          }

          // clear selection
          this.selectedFlavours = [];
          this.selectedToppings = [];
          this.selectedCone = null;
        } catch (err) {
          console.error('Order request failed', err);
          const msg = err && err.message ? err.message : 'Unknown error';
          toastService.error(`Order request failed: ${msg}`, 'Order Error');
        }
      };
      doRequest();
    },
    undoSelection() {
      if (this.selectedCone) {
        this.selectedCone = null;
      } else if (this.selectedToppings.length > 0) {
        this.selectedToppings.pop();
      } else if (this.selectedFlavours.length > 0) {
        this.selectedFlavours.pop();
      }
    }
  }
  ,
  computed: {
    treatIsValid() {
      return (
        this.selectedCone &&
        this.selectedFlavours.length >= 1 &&
        // upper limits removed: allow more than 3 flavours and more than 5 toppings
        true
      );
    },
    treatTotalPrice() {
      let total = 0;
      if (this.selectedCone) total += this.selectedCone.price;
      total += this.selectedFlavours.reduce((s, f) => s + (f.price || 0), 0);
      total += this.selectedToppings.reduce((s, t) => s + (t.price || 0), 0);
      return total;
    }
    ,
    validationReason() {
      if (!this.selectedCone) return 'Please select a cone.';
      if (this.selectedFlavours.length < 1) return 'Select at least 1 flavour.';
      // upper limits removed
      return '';
    }
  }
}
</script>

<style scoped>
.scoops-builder-view {
  padding: 2rem;
  text-align: center;
}

.tiles-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selection-details {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  position: relative;
}

.selection-section {
  margin-bottom: 2rem;
}

.flavour-list, .topping-list, .cone-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.flavour-item, .topping-item, .cone-item {
  padding: 1rem;
  width: 100%;
  max-width: 600px;
  text-align: left;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.2rem;
}

.product-row {
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 0.2rem;
}
.product-price {
  margin-left: 1rem;
}

.flavour-item h2, .topping-item p, .cone-item p {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: #333;
}

.flavour-item .product-description,
.topping-item .product-description,
.cone-item .product-description {
  font-size: 0.85rem !important;
  color: #333 !important;
  margin-bottom: 0.2rem;
}

.flavour-item .product-ingredients,
.topping-item .product-ingredients,
.cone-item .product-ingredients {
  font-size: 0.75rem !important;
  color: #888 !important;
  margin-bottom: 0.2rem;
}
.product-ingredients strong {
  font-weight: bold;
}

.disabled-button {
  opacity: 0.45;
  cursor: not-allowed;
  box-shadow: none !important;
  filter: grayscale(30%);
  border: 1px solid rgba(0,0,0,0.08);
  pointer-events: none;
}

.validation-box {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  border: 1px solid rgba(0,0,0,0.06);
  max-width: 580px;
  margin-top: 0.5rem;
}
.validation-box__title {
  font-weight: 800;
  color: #8a4b00;
}
.validation-box__message {
  margin: 0;
  color: #4b2b00;
}
.validation-box--warning {
  background: linear-gradient(180deg, #fff4e5, #fff6e8);
  border-color: rgba(238, 170, 60, 0.25);
}

/* Custom checkbox/radio styles to match app theme */
input[type="checkbox"], input[type="radio"] {
  appearance: none;
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 6px;
  border: 2px solid #cfeee1;
  background: #fff;
  display: inline-block;
  vertical-align: text-top;
  margin-right: 0.6rem;
  position: relative;
  top: 2px;
}

input[type="radio"] {
  border-radius: 50%;
}

input[type="checkbox"]:checked, input[type="radio"]:checked {
  background: linear-gradient(180deg,#31d07a,#0fb06a);
  border-color: #0fa96a;
}
                                                                                             
input[type="checkbox"]:checked, input[type="radio"]:checked {
  background: linear-gradient(180deg,#31d07a,#0fb06a);
  border-color: #0fa96a;
}

input[type="checkbox"]:checked::after,
input[type="radio"]:checked::after {
  content: none;
}

input[type="checkbox"]:focus-visible, input[type="radio"]:focus-visible {
  outline: 3px solid rgba(49,208,122,0.25);
  outline-offset: 2px;
}

input[type="checkbox"]:disabled, input[type="radio"]:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.checkout-button {
  margin-top: 1rem;
  display: inline-block;
  text-align: center;
}
</style>