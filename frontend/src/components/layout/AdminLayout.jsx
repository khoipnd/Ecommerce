import {
    Link,
    NavLink,
    Outlet,
    useNavigate,
} from "react-router-dom";

import {
    logoutAdmin,
} from "../../services/authService";

const AdminLayout = () => {
    const navigate = useNavigate();

    // Hàm xử lý đăng xuất dành cho Admin
    const handleLogout = () => {
        // Xóa thông tin phiên làm việc, token của admin trong storage/cookie
        logoutAdmin();

        // Chuyển hướng người dùng về trang đăng nhập
        // Tùy chọn replace: true giúp xóa lịch sử trang trước đó, không cho ấn Back quay lại trang dashboard
        navigate(
            "/admin/login",
            {
                replace: true,
            }
        );
    };

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <div className="admin-brand">
                    <Link to="/admin" className="header-brand" style={{ textDecoration: 'none' }}>
                        <div className="brand-icon" style={{ backgroundColor: '#222', color: '#fff', borderRadius: '8px', padding: '6px', display: 'flex' }}>
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                <line x1="3" y1="6" x2="21" y2="6"/>
                                <path d="M16 10a4 4 0 0 1-8 0"/>
                            </svg>
                        </div>
                        <div className="brand-text" style={{ marginLeft: '10px' }}>
                            <span className="brand-name" style={{ display: 'block', fontSize: '18px', fontWeight: '700', color: '#333' }}>MyStore</span>
                            <span className="brand-tag" style={{ display: 'block', fontSize: '12px', color: '#888', marginTop: '2px' }}>Admin Panel</span>
                        </div>
                    </Link>
                </div>

                <nav className="admin-navigation">
                    <p className="admin-nav-title">
                        MANAGEMENT
                    </p>

                    <NavLink
                        to="/admin"
                        end
                        className={({ isActive }) =>
                            `admin-nav-link ${
                                isActive
                                    ? "active"
                                    : ""
                            }`
                        }
                    >
                        <span className="nav-icon">
                            ▦
                        </span>

                        Dashboard
                    </NavLink>

                    <NavLink
                        to="/admin/products"
                        className={({ isActive }) =>
                            `admin-nav-link ${
                                isActive
                                    ? "active"
                                    : ""
                            }`
                        }
                    >
                        <span className="nav-icon">
                            □
                        </span>

                        Products
                    </NavLink>

                    <NavLink
                        to="/admin/categories"
                        className={({ isActive }) =>
                            `admin-nav-link ${
                                isActive
                                    ? "active"
                                    : ""
                            }`
                        }
                    >
                        <span className="nav-icon">
                            ◇
                        </span>

                        Categories
                    </NavLink>

                    <NavLink
                        to="/admin/orders"
                        className={({ isActive }) =>
                            `admin-nav-link ${
                                isActive
                                    ? "active"
                                    : ""
                            }`
                        }
                    >
                        <span className="nav-icon">
                            ≡
                        </span>

                        Orders
                    </NavLink>
                </nav>

                <div className="admin-sidebar-bottom">
                    <button
                        type="button"
                        className="admin-logout-button"
                        onClick={handleLogout}
                    >
                        <span className="nav-icon">
                            ←
                        </span>

                        Logout
                    </button>
                </div>
            </aside>

            <div className="admin-main">
                <header className="admin-topbar">
                    <div>
                        <p className="admin-topbar-label">
                            ADMINISTRATION
                        </p>

                        <h2>
                            Store Management
                        </h2>
                    </div>

                    <div className="admin-user">
                        <div className="admin-user-avatar">
                            A
                        </div>

                        <div>
                            <strong>
                                Administrator
                            </strong>

                            <span>
                                Admin
                            </span>
                        </div>
                    </div>
                </header>

                <main className="admin-content">
                    {/* Component Outlet sẽ render các component con dựa trên route tương ứng (vd: Dashboard, quản lý Product, Order,...) */}
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;