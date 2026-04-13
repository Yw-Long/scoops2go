import { BASE_URL } from '../api';

export async function fetchCones() {
  try {
    const response = await fetch(`${BASE_URL}/cone`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching cones:', error);
    throw error;
  }
}