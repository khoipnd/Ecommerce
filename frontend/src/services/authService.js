import apiClient from "../api/axiosInstance";
import { clearSession, readSession, saveSession } from "./authStorage";

export const login = async (identifier, password) => {
    const { data } = await apiClient.post("/api/v1/auth/login", { identifier, password });
    saveSession(data);
    return data;
};

export const register = async (email, password, fullName = "") => {
    const { data } = await apiClient.post("/api/v1/auth/register", { email, password, fullName });
    return data;
};

export const getCurrentAccount = async () => {
    const { data } = await apiClient.get("/api/v1/auth/me");
    return data;
};

export const loginAdmin = async (identifier, password) => {
    const data = await login(identifier, password);
    if (data.role !== "ADMIN") {
        clearSession();
        throw new Error("Tài khoản không có quyền quản trị.");
    }
    return data;
};

export const isAdminAuthenticated = () => readSession()?.role === "ADMIN";
export const logout = clearSession;
export const logoutAdmin = clearSession;
