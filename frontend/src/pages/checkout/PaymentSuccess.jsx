import { Link, Navigate, useLocation } from "react-router-dom";

const PaymentSuccess = () => {
    const location = useLocation();
    const orderId = location.state?.orderId;
    if (!orderId) return <Navigate to="/orders" replace />;

    return (
        <div className="container" style={{ textAlign: "center", padding: "80px 20px" }}>
            <div style={{ fontSize: "64px", color: "#10b981", margin: "0 auto 20px", width: "100px", height: "100px", background: "#ecfdf5", borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center" }}>
                ✓
            </div>
            <h1 style={{ fontSize: "32px", marginBottom: "16px", color: "#0f172a" }}>
                Order placed successfully!
            </h1>
            <p style={{ fontSize: "16px", color: "#64748b", marginBottom: "8px" }}>
                Thank you for shopping with us. We will contact you soon to confirm your order.
            </p>
            <div style={{ background: "#f8fafc", padding: "16px", borderRadius: "8px", display: "inline-block", margin: "20px 0 32px" }}>
                Your order ID: <strong style={{ color: "#2563eb", fontSize: "18px" }}>{orderId}</strong>
            </div>
            <div style={{ display: 'flex', gap: '16px', justifyContent: 'center' }}>
                <Link 
                    to="/products" 
                    className="btn btn-outline"
                    style={{ padding: "12px 24px", fontSize: "16px", color: "#475569", border: "1px solid #cbd5e1", borderRadius: "8px", textDecoration: "none" }}
                >
                    Continue Shopping
                </Link>
                <Link 
                    to="/orders" 
                    className="btn btn-primary"
                    style={{ padding: "12px 24px", fontSize: "16px", background: "#2563eb", color: "#fff", borderRadius: "8px", textDecoration: "none" }}
                >
                    View Orders
                </Link>
            </div>
        </div>
    );
};

export default PaymentSuccess;
