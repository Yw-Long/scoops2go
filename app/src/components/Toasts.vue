<template>
  <div class="toasts" aria-live="polite" aria-atomic="true">
    <div v-for="toast in state.toasts" :key="toast.id" :class="['toast', toast.type]">
      <div class="toast-header">
        <strong class="toast-title">{{ toast.title }}</strong>
        <button class="toast-close" @click="remove(toast.id)" aria-label="Close">×</button>
      </div>
      <div class="toast-body">{{ toast.message }}</div>
    </div>
  </div>
</template>

<script>
import toastService from '../services/toastService';

export default {
  name: 'Toasts',
  setup() {
    return { state: toastService.state, remove: toastService.removeToast };
  }
}
</script>

<style scoped>
.toasts {
  position: fixed;
  right: 1rem;
  bottom: 1rem;
  z-index: 1100;
  display: flex;
  flex-direction: column-reverse;
  align-items: flex-end;
  gap: 0.5rem;
  max-width: 360px;
}
.toast {
  border-radius: 8px;
  padding: 0.6rem 0.9rem;
  box-shadow: 0 6px 18px rgba(0,0,0,0.08);
  background: #fff;
  color: #222;
  border-left: 6px solid transparent;
  width: 100%;
  box-sizing: border-box;
}
.toast.success {
  border-left-color: hsla(160, 100%, 37%, 1);
  background: linear-gradient(180deg, rgba(49,208,122,0.10), #f7fff9);
  color: #0b3b2a;
}
.toast.error { border-left-color: #dc3545; background: linear-gradient(180deg, rgba(220,53,69,0.06), #fff6f6); color: #641a14; }
.toast.info { border-left-color: #17a2b8; background: linear-gradient(180deg, rgba(23,162,184,0.06), #f6feff); color: #0b5058; }
.toast-header { display:flex; justify-content:space-between; align-items:center; }
.toast-title { font-weight:700; font-size:0.95rem; }
.toast-close { background:none; border:0; font-size:1.1rem; line-height:1; cursor:pointer; }
.toast-body { font-size:0.9rem; margin-top:0.35rem; }
</style>
