import apiClient from "../api/axiosInstance";

export const ORDER_STATUSES = ["PENDING", "CONFIRMED", "SHIPPED", "COMPLETED", "CANCELLED"];
export const ORDER_TRANSITIONS = {
    PENDING: ["CONFIRMED", "CANCELLED"], CONFIRMED: ["SHIPPED", "CANCELLED"],
    SHIPPED: ["COMPLETED"], COMPLETED: [], CANCELLED: [],
};
// Chuyển DTO REST thành cấu trúc giao diện cũ, giữ nguyên enum trạng thái backend.
export const toOrderView = (order) => ({
    ...order,
    date: order.createdAt ? new Date(order.createdAt).toLocaleString("vi-VN") : "—",
    total: Number(order.totalAmount ?? 0),
    customer: { name: order.receiverName, phone: order.receiverPhone, address: order.shippingAddress },
    items: (order.items ?? []).map(item => ({ ...item, product: { id: item.productId, name: item.productName, price: Number(item.unitPrice) } })),
});
export const createOrder = async (payload) => {
    const { data } = await apiClient.post("/api/v1/orders", payload); // /checkout: backend tự tính tiền và trừ kho trong giao dịch.
    return toOrderView(data);
};
export const getMyOrders = async (signal) => {
    const { data } = await apiClient.get("/api/v1/orders/my", { signal }); // /orders và /tracking chỉ đọc đơn của chủ JWT.
    return data.map(toOrderView);
};
export const getOrder = async (id) => {
    const { data } = await apiClient.get(`/api/v1/orders/${id}`); // Chi tiết đơn và trang xác nhận đặt hàng.
    return toOrderView(data);
};
export const cancelOrder = async (id) => {
    const { data } = await apiClient.patch(`/api/v1/orders/${id}/cancel`); // Khách hủy đơn PENDING; backend tự hoàn kho.
    return toOrderView(data);
};
export const getAdminOrders = async (status, signal) => {
    const { data } = await apiClient.get("/api/v1/admin/orders", { params: status && status !== "all" ? { status } : {}, signal }); // /admin/orders và dashboard: lọc trạng thái trên backend.
    return data.map(toOrderView);
};
export const getAdminOrder = async (id) => {
    const { data } = await apiClient.get(`/api/v1/admin/orders/${id}`); // Nút View lấy chi tiết đơn mới nhất.
    return toOrderView(data);
};
export const updateOrderStatus = async (id, status) => {
    const { data } = await apiClient.patch(`/api/v1/admin/orders/${id}/status`, { status }); // Admin gửi enum viết hoa, đúng bước backend cho phép.
    return toOrderView(data);
};
