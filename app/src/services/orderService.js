import { BASE_URL } from '../api';

export async function createOrder(orderPayload) {
  console.log('Creating order with payload:', orderPayload);
  const res = await fetch(`${BASE_URL}/order`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(orderPayload)
  });
  if (!res.ok) {
    let errText = '';
    try { errText = await res.text(); errText = errText || res.statusText; } catch(e) { errText = res.statusText; }
    throw new Error(`Failed to create order: ${errText}`);
  }
  return res.json();
}

export async function updateOrder(orderPayload) {
  const res = await fetch(`${BASE_URL}/order`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(orderPayload)
  });
  if (!res.ok) {
    let errText = '';
    try { errText = await res.text(); errText = errText || res.statusText; } catch(e) { errText = res.statusText; }
    throw new Error(`Failed to update order: ${errText}`);
  }
  return res.json();
}

export async function getOrder(orderId) {
  const res = await fetch(`${BASE_URL}/order/${orderId}`);
  if (!res.ok) {
    let errText = '';
    try { errText = await res.text(); errText = errText || res.statusText; } catch(e) { errText = res.statusText; }
    throw new Error(`Failed to fetch order: ${errText}`);
  }
  return res.json();
}

export async function checkoutOrder(orderId, payload) {
  const res = await fetch(`${BASE_URL}/order/${orderId}/checkout`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: payload ? JSON.stringify(payload) : null
  });
  if (!res.ok) {
    let errText = '';
    try { errText = await res.text(); errText = errText || res.statusText; } catch(e) { errText = res.statusText; }
    throw new Error(`Checkout failed: ${errText}`);
  }
  return res.json();
}

export async function clearOrder(orderId) {
  const resp = await fetch(`${BASE_URL}/order/${orderId}`, {
    method: 'DELETE'
  });
  if (!resp.ok) throw new Error(`Failed to clear order: ${resp.status}`);

  // Handle 204 or empty body safely
  if (resp.status === 204) return { orderId };
  const text = await resp.text();
  if (!text) return { orderId };
  return JSON.parse(text);
}

export async function searchOrder(orderNumber) {
  // Expect a numeric id (string or number) and fetch by id
  if (!orderNumber) throw new Error('Order id required');
  const id = Number(orderNumber);
  if (!Number.isInteger(id)) throw new Error('Order id must be a numeric id');
  const res = await fetch(`${BASE_URL}/order/${id}`);
  if (!res.ok) {
    let errText = '';
    try { errText = await res.text(); errText = errText || res.statusText; } catch(e) { errText = res.statusText; }
    throw new Error(`Failed to find order: ${errText}`);
  }
  return res.json();
}
