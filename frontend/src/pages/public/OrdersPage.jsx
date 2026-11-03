import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { cancelOrder, getMyOrders } from "../../services/orderService";

export default function OrdersPage() {
    const [orders, setOrders] = useState([]);
    const [expanded, setExpanded] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [busyId, setBusyId] = useState(null);

    useEffect(() => {
        const controller = new AbortController();
        getMyOrders(controller.signal)
            .then(data => setOrders(data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))))
            .catch(err => { if (!controller.signal.aborted) setError(err.message); })
            .finally(() => { if (!controller.signal.aborted) setLoading(false); });
        return () => controller.abort();
    }, []);

    const handleCancel = async (id) => {
        if (!window.confirm("Cancel this order?")) return;
        try {
            setBusyId(id);
            setError("");
            const updated = await cancelOrder(id);
            setOrders(current => current.map(order => order.id === id ? updated : order));
        } catch (err) {
            setError(err.message);
        } finally {
            setBusyId(null);
        }
    };

    return <div className="container" style={{ padding: "40px 20px", minHeight: "70vh" }}>
        <h1>Order History</h1>
        {loading && <p>Loading orders...</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && orders.length === 0 && <p>You have no orders yet. <Link to="/products">Shop now</Link></p>}
        {orders.map(order => <article key={order.id} style={{ background: "white", border: "1px solid #e2e8f0", borderRadius: 8, padding: 20, marginBottom: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
                <strong>Order #{order.id}</strong><span>{order.date}</span><strong>{order.status}</strong>
            </div>
            <p>{order.customer.name} · {order.customer.phone}</p>
            <p>Total: {order.total.toLocaleString("vi-VN")}đ</p>
            <button type="button" className="order-action-button" onClick={() => setExpanded(expanded === order.id ? null : order.id)}>{expanded === order.id ? "Hide details" : "View details"}</button>
            {order.status === "PENDING" && <button type="button" className="order-action-button order-action-button--danger" disabled={busyId === order.id} onClick={() => handleCancel(order.id)}>Cancel order</button>}
            {expanded === order.id && <div>
                <p>Ship to: {order.customer.address}</p>
                <ul>{order.items.map(item => <li key={item.productId}>{item.productName} × {item.quantity} — {Number(item.lineTotal).toLocaleString("vi-VN")}đ</li>)}</ul>
            </div>}
        </article>)}
    </div>;
}
