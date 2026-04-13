import { BASE_URL } from '../api';

export async function fetchToppings() {
  try {
    const response = await fetch(`${BASE_URL}/topping`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching toppings:', error);
    throw error;
  }
}