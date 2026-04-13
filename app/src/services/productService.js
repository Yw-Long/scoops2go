import { BASE_URL } from '../api';

export async function fetchProducts() {
  try {
    const response = await fetch(`${BASE_URL}/product`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching products:', error);
    throw error;
  }
}

export async function fetchProductById(id) {
  try {
    const response = await fetch(`${BASE_URL}/product/${id}`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching product by id:', error);
    throw error;
  }
}
