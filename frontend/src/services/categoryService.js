import apiClient from "../api/axiosInstance";
import { ENDPOINTS } from "../api/endpoints";

let categoriesPromise;

export const getCategories = () => {
    if (!categoriesPromise) {
        categoriesPromise = apiClient.get(ENDPOINTS.CATEGORIES)
            .then(response => response.data)
            .catch(error => {
                categoriesPromise = undefined;
                throw error;
            });
    }
    return categoriesPromise;
};
export const getCategoryById = async (id) => (await apiClient.get(`${ENDPOINTS.CATEGORIES}/${id}`)).data;
export const createCategory = async (payload) => {
    const response = await apiClient.post(ENDPOINTS.CATEGORIES, payload);
    categoriesPromise = undefined;
    return response.data;
};
export const updateCategory = async (id, payload) => {
    const response = await apiClient.put(`${ENDPOINTS.CATEGORIES}/${id}`, payload);
    categoriesPromise = undefined;
    return response.data;
};
export const deleteCategory = async (id) => {
    const response = await apiClient.delete(`${ENDPOINTS.CATEGORIES}/${id}`);
    categoriesPromise = undefined;
    return response;
};
