import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getProducts } from "../../services/productService";
import { getCategories } from "../../services/categoryService";
import { getAdminOrders } from "../../services/orderService";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";


const AdminDashboardPage = () => {
    const [productCount, setProductCount] = useState(0);
    const [categoryCount, setCategoryCount] = useState(0);
    const [orderCount, setOrderCount] = useState(0);
    const [cancelledCount, setCancelledCount] = useState(0);
    const [revenue, setRevenue] = useState(0);
    const [recentProducts, setRecentProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                setError(null);

                const [products, categories, orders] = await Promise.all([
                    getProducts(),
                    getCategories(),
                    getAdminOrders(),
                ]);

                setProductCount(products.length);
                setCategoryCount(categories.length);
                setRecentProducts(products.slice(0, 5));

                const validOrders = orders.filter(o => o.status !== 'CANCELLED');
                const cancelledOrders = orders.filter(o => o.status === 'CANCELLED');
                
                const totalRevenue = validOrders.reduce((sum, order) => sum + (order.total || 0), 0);

                setOrderCount(validOrders.length);
                setCancelledCount(cancelledOrders.length);
                setRevenue(totalRevenue);
            } catch (err) {
                setError(err);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) {
        return (
            <div className="admin-page">
                <Loading message="Loading Admin dashboard data..." />
            </div>
        );
    }

    if (error) {
        return (
            <div className="admin-page">
                <ErrorMessage message="Failed to load Admin dashboard data." />
            </div>
        );
    }

    return (
        <div className="admin-page">
            {/* Page Header */}
            <div className="admin-page-header">
                <div>
                    <p className="admin-page-label">SYSTEM OVERVIEW</p>
                    <h1>Admin Dashboard</h1>
                    <p>Welcome back! Here is the performance overview of MyStore.</p>
                </div>

                <div className="dashboard-header-actions">
                    <Link to="/" target="_blank" className="admin-secondary-button">
                        🌐 View Store
                    </Link>
                    <Link to="/admin/products/create" className="admin-primary-button">
                        + Add New Product
                    </Link>
                </div>
            </div>

            {/* Stat Cards Row */}
            <div className="dashboard-stats-grid">
                <div className="dashboard-stat-card stat-blue">
                    <div className="stat-card-top">
                        <div className="stat-icon-badge">📦</div>
                        <span className="stat-label">PRODUCTS</span>
                    </div>
                    <strong>{productCount}</strong>
                    <p>Total active products</p>
                </div>

                <div className="dashboard-stat-card stat-purple">
                    <div className="stat-card-top">
                        <div className="stat-icon-badge">📁</div>
                        <span className="stat-label">CATEGORIES</span>
                    </div>
                    <strong>{categoryCount}</strong>
                    <p>Classified product categories</p>
                </div>

                <div className="dashboard-stat-card stat-green">
                    <div className="stat-card-top">
                        <div className="stat-icon-badge">🛍️</div>
                        <span className="stat-label">ORDERS</span>
                    </div>
                    <strong>{orderCount}</strong>
                    <p>Active/Completed orders</p>
                </div>

                <div className="dashboard-stat-card stat-orange">
                    <div className="stat-card-top">
                        <div className="stat-icon-badge">❌</div>
                        <span className="stat-label">CANCELLED</span>
                    </div>
                    <strong>{cancelledCount}</strong>
                    <p>Cancelled orders</p>
                </div>

                <div className="dashboard-stat-card stat-purple" style={{ borderLeft: '4px solid #8b5cf6' }}>
                    <div className="stat-card-top">
                        <div className="stat-icon-badge">💰</div>
                        <span className="stat-label">REVENUE</span>
                    </div>
                    <strong>{revenue.toLocaleString('en-US')} VND</strong>
                    <p>Total estimated revenue</p>
                </div>
            </div>

            {/* Quick Actions & Recent Items */}
            <div className="dashboard-two-columns">
                {/* Recent Products Card */}
                <div className="dashboard-table-card">
                    <div className="card-header-row">
                        <h2>Recently Updated Products</h2>
                        <Link to="/admin/products" className="view-link">View all →</Link>
                    </div>

                    <div className="admin-table-wrapper">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Product</th>
                                    <th>Category</th>
                                    <th>Price</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentProducts.map((p) => (
                                    <tr key={p.id}>
                                        <td><span className="id-badge">#{p.id}</span></td>
                                        <td><strong>{p.name}</strong></td>
                                        <td><span className="category-tag">{p.category?.name || "Other"}</span></td>
                                        <td>
                                            <strong className="table-price">
                                                {typeof p.price === 'number' && p.price > 1000
                                                    ? `${p.price.toLocaleString('en-US')} VND`
                                                    : `$${p.price}`}
                                            </strong>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Management Shortcuts */}
                <div className="dashboard-shortcuts-column">
                    <div className="dashboard-shortcut-card">
                        <div className="shortcut-icon">🛠️</div>
                        <div>
                            <h3>Product Management</h3>
                            <p>Create, edit product information and adjust stock quantities.</p>
                        </div>
                        <Link to="/admin/products" className="admin-primary-button full-btn">
                            Access Product Management →
                        </Link>
                    </div>

                    <div className="dashboard-shortcut-card">
                        <div className="shortcut-icon">🏷️</div>
                        <div>
                            <h3>Category Management</h3>
                            <p>Create new categories, edit product grouping structure.</p>
                        </div>
                        <Link to="/admin/categories" className="admin-secondary-button full-btn">
                            Access Category Management →
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboardPage;
