import axios from "axios";
import { clearSession, readSession } from "../services/authStorage";

const apiClient = axios.create({ baseURL: import.meta.env.VITE_API_URL });

apiClient.interceptors.request.use((config) => {
    const token = readSession()?.token;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && readSession()) clearSession();
        const body = error.response?.data;
        const message = body?.message || body?.error || (body && typeof body === "object" ? Object.values(body)[0] : null);
        if (typeof message === "string") error.message = message;
        return Promise.reject(error);
    }
);

export default apiClient;
