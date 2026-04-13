import { reactive } from 'vue';

const state = reactive({
  toasts: []
});

let nextId = 1;

function pushToast({ type = 'info', title = '', message = '', timeout = 4000 }) {
  const id = nextId++;
  state.toasts.push({ id, type, title, message });
  if (timeout > 0) {
    setTimeout(() => removeToast(id), timeout);
  }
  return id;
}

export function removeToast(id) {
  const idx = state.toasts.findIndex(t => t.id === id);
  if (idx !== -1) state.toasts.splice(idx, 1);
}

export function success(message, title = 'Success', timeout = 4000) {
  return pushToast({ type: 'success', title, message, timeout });
}

export function error(message, title = 'Error', timeout = 6000) {
  return pushToast({ type: 'error', title, message, timeout });
}

export function info(message, title = 'Info', timeout = 4000) {
  return pushToast({ type: 'info', title, message, timeout });
}

export default {
  state,
  success,
  error,
  info,
  removeToast
};
