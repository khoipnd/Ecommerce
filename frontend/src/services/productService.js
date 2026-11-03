import apiClient from "../api/axiosInstance";
import { ENDPOINTS } from "../api/endpoints";
import { getCategories } from "./categoryService";

const withCategory = (product, categories) => ({
    ...product,
    category: categories.find(category => String(category.id) === String(product.categoryId)) || null,
});
export const getProducts = async () => {
    const [products, categories] = await Promise.all([
        apiClient.get(ENDPOINTS.PRODUCTS).then(response => response.data),
        getCategories().catch(() => []),
    ]);
    return products.map(product => withCategory(product, categories));
};
export const getProductById = async (id) => {
    const [product, categories] = await Promise.all([
        apiClient.get(`${ENDPOINTS.PRODUCTS}/${id}`).then(response => response.data),
        getCategories().catch(() => []),
    ]);
    return withCategory(product, categories);
};
export const createProduct = async (payload) => (await apiClient.post(ENDPOINTS.PRODUCTS, payload)).data;
export const updateProduct = async (id, payload) => (await apiClient.put(`${ENDPOINTS.PRODUCTS}/${id}`, payload)).data;
export const deleteProduct = async (id) => apiClient.delete(`${ENDPOINTS.PRODUCTS}/${id}`);
