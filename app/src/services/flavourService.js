import { BASE_URL } from '../api';

export async function fetchFlavours() {
  try {
    const response = await fetch(`${BASE_URL}/flavor`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching flavours:', error);
    throw error;
  }
}