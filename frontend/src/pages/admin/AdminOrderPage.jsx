import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Pagination from "../../components/common/Pagination";
import ConfirmModal from "../../components/common/ConfirmModal";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";
import { getAdminOrders, getAdminOrder, updateOrderStatus as saveOrderStatus, ORDER_TRANSITIONS } from "../../services/orderService";

const ITEMS_PER_PAGE = 10;

const AdminOrderPage = () => {
    const [orders, setOrders] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [currentPage, setCurrentPage] = useState(1);
    const [statusFilter, setStatusFilter] = useState("all");
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [orderToCancel, setOrderToCancel] = useState(null);
    const [successToast, setSuccessToast] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            setLoading(true);
            setError("");
            const data = await getAdminOrders();
            setOrders(data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const updateOrderStatus = async (orderId, newStatus) => {
        try {
            const updated = await saveOrderStatus(orderId, newStatus);
            setOrders(current => current.map(order => order.id === orderId ? updated : order));
            setSelectedOrder(current => current?.id === orderId ? updated : current);
            setSuccessToast(`Order status updated to ${newStatus}`);
        } catch (err) {
            setError(err.message);
        }
    };

    const handleCancelOrder = async () => {
        if (orderToCancel) {
            await updateOrderStatus(orderToCancel, "CANCELLED");
            setOrderToCancel(null);
        }
    };

    // Filter and Pagination
    const filteredOrders = orders.filter((order) => {
        const matchesSearch = 
            String(order.id).includes(searchQuery.toLowerCase()) ||
            order.customer.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            order.customer.phone.includes(searchQuery);
        
        const matchesStatus = statusFilter === "all" || order.status === statusFilter;
        
        return matchesSearch && matchesStatus;
    });

    const paginatedOrders = filteredOrders.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    const getStatusBadge = (status) => {
        switch (status) {
            case 'SHIPPED': return <span className="order-status-badge order-status-badge--shipped">Shipped</span>;
            case 'COMPLETED': return <span className="order-status-badge order-status-badge--completed">Completed</span>;
            case 'CANCELLED': return <span className="order-status-badge order-status-badge--cancelled">Cancelled</span>;
            case 'CONFIRMED': return <span className="order-status-badge order-status-badge--confirmed">Confirmed</span>;
            case 'PENDING':
            default:
                return <span className="order-status-badge order-status-badge--pending">Pending</span>;
        }
    };

    return (
        <div className="admin-page-container">
            {loading && <p>Loading orders...</p>}
            {error && <p role="alert">{error}</p>}
            <div className="admin-header-actions" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1 className="admin-page-title">Orders Management</h1>
            </div>

            <div className="admin-filters" style={{ display: 'flex', gap: '15px', marginBottom: '20px' }}>
                <div className="search-bar" style={{ flex: 1 }}>
                    <input
                        type="text"
                        placeholder="Search by Order ID, Customer Name or Phone..."
                        value={searchQuery}
                        onChange={(e) => {
                            setSearchQuery(e.target.value);
                            setCurrentPage(1);
                        }}
                        style={{ width: '100%', padding: '10px 15px', borderRadius: '8px', border: '1px solid #cbd5e1' }}
                    />
                </div>
                <select 
                    value={statusFilter} 
                    onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}
                    style={{ padding: '10px 15px', borderRadius: '8px', border: '1px solid #cbd5e1', backgroundColor: 'white' }}
                >
                    <option value="all">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="CONFIRMED">Confirmed</option>
                    <option value="SHIPPED">Shipped</option>
                    <option value="COMPLETED">Completed</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>

            <div className="admin-table-container" style={{ background: 'white', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', overflow: 'hidden' }}>
                <table className="admin-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', textAlign: 'left' }}>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600' }}>ORDER ID</th>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600' }}>DATE</th>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600' }}>CUSTOMER</th>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600' }}>TOTAL</th>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600' }}>STATUS</th>
                            <th style={{ padding: '16px', color: '#64748b', fontWeight: '600', textAlign: 'right' }}>ACTIONS</th>
                        </tr>
                    </thead>
                    <tbody>
                        {paginatedOrders.length === 0 ? (
                            <tr>
                                <td colSpan="6" style={{ padding: '30px', textAlign: 'center', color: '#94a3b8' }}>No orders found</td>
                            </tr>
                        ) : (
                            paginatedOrders.map((order) => (
                                <tr key={order.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                                    <td style={{ padding: '16px', fontWeight: '600', color: '#334155' }}>#{order.id}</td>
                                    <td style={{ padding: '16px', color: '#64748b' }}>{order.date}</td>
                                    <td style={{ padding: '16px' }}>
                                        <div style={{ fontWeight: '500', color: '#334155' }}>{order.customer.name}</div>
                                        <div style={{ fontSize: '12px', color: '#94a3b8' }}>{order.customer.phone}</div>
                                    </td>
                                    <td style={{ padding: '16px', fontWeight: 'bold', color: '#2563eb' }}>{order.total.toLocaleString('en-US')} VND</td>
                                    <td style={{ padding: '16px' }}>{getStatusBadge(order.status)}</td>
                                    <td style={{ padding: '16px', textAlign: 'right' }}>
                                        <button 
                                            onClick={async () => { try { setSelectedOrder(await getAdminOrder(order.id)); } catch (err) { setError(err.message); } }}
                                            style={{ padding: '6px 12px', background: '#f1f5f9', color: '#334155', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', marginRight: '8px' }}
                                        >
                                            View
                                        </button>
                                        {ORDER_TRANSITIONS[order.status]?.includes("CANCELLED") && (
                                            <button 
                                                onClick={() => setOrderToCancel(order.id)}
                                                style={{ padding: '6px 12px', background: '#fee2e2', color: '#ef4444', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' }}
                                            >
                                                Cancel
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div style={{ marginTop: '20px' }}>
                <Pagination
                    currentPage={currentPage}
                    totalPages={Math.ceil(filteredOrders.length / ITEMS_PER_PAGE)}
                    onPageChange={setCurrentPage}
                />
            </div>

            {/* Order Details Modal */}
            {selectedOrder && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ background: 'white', borderRadius: '12px', width: '90%', maxWidth: '700px', maxHeight: '90vh', overflowY: 'auto', padding: '24px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e2e8f0', paddingBottom: '16px', marginBottom: '20px' }}>
                            <h2 style={{ margin: 0 }}>Order Details #{selectedOrder.id}</h2>
                            <button onClick={() => setSelectedOrder(null)} style={{ background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer', color: '#64748b' }}>&times;</button>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '24px' }}>
                            <div>
                                <h3 style={{ fontSize: '14px', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '8px' }}>Customer Information</h3>
                                <p style={{ margin: '4px 0' }}><strong>Name:</strong> {selectedOrder.customer.name}</p>
                                <p style={{ margin: '4px 0' }}><strong>Phone:</strong> {selectedOrder.customer.phone}</p>
                                <p style={{ margin: '4px 0' }}><strong>Address:</strong> {selectedOrder.customer.address}</p>
                            </div>
                            <div>
                                <h3 style={{ fontSize: '14px', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '8px' }}>Order Information</h3>
                                <p style={{ margin: '4px 0' }}><strong>Date:</strong> {selectedOrder.date}</p>
                                <p style={{ margin: '4px 0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <strong>Status:</strong> {getStatusBadge(selectedOrder.status)}
                                </p>
                            </div>
                        </div>

                        <h3 style={{ fontSize: '14px', color: '#94a3b8', textTransform: 'uppercase', marginBottom: '12px', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px' }}>Ordered Items</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px' }}>
                            {selectedOrder.items.map((item, idx) => (
                                <div key={idx} style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                                    <img className="admin-order-product-image" src={getPrimaryImage(item.product ?? { id: item.productId })} alt={item.product?.name ?? item.productName ?? "Product"} loading="lazy" onError={handleImageError} />
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: '600' }}>{item.product?.name}</div>
                                        <div style={{ fontSize: '13px', color: '#64748b' }}>{item.options?.color && `Color: ${item.options.color} | `}Qty: {item.quantity}</div>
                                    </div>
                                    <div style={{ fontWeight: 'bold' }}>
                                        {((item.product?.price || 0) * item.quantity).toLocaleString('en-US')} VND
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #e2e8f0', paddingTop: '20px' }}>
                            <div style={{ fontSize: '18px', fontWeight: 'bold' }}>Total Amount: <span style={{ color: '#2563eb' }}>{selectedOrder.total.toLocaleString('en-US')} VND</span></div>
                            
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <select 
                                    value={selectedOrder.status}
                                    onChange={(e) => updateOrderStatus(selectedOrder.id, e.target.value)}
                                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #cbd5e1' }}
                                >
                                    <option value={selectedOrder.status}>{selectedOrder.status}</option>
                                    {(ORDER_TRANSITIONS[selectedOrder.status] || []).map(status => <option key={status} value={status}>{status}</option>)}
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <ConfirmModal 
                isOpen={!!orderToCancel} 
                title="Cancel Order" 
                message="Are you sure you want to cancel this order? This action cannot be undone." 
                onConfirm={handleCancelOrder} 
                onCancel={() => setOrderToCancel(null)} 
            />

            {successToast && (
                <div style={{ position: 'fixed', top: '20px', right: '20px', background: '#22c55e', color: '#fff', padding: '16px 24px', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)', zIndex: 9999 }}>
                    {successToast}
                </div>
            )}
        </div>
    );
};

export default AdminOrderPage;
