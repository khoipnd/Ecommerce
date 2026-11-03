import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

import {
    loginAdmin,
} from "../../services/authService";

const AdminLoginPage = () => {
    const navigate = useNavigate();

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [errors, setErrors] = useState({});

    const [loading, setLoading] =
        useState(false);

    // Hàm xử lý quá trình xác thực khi admin submit form đăng nhập
    const handleSubmit = async (event) => {
        event.preventDefault(); // Ngăn chặn trình duyệt reload lại trang mặc định

        setErrors({});
        const newErrors = {};

        // Biểu thức chính quy kiểm tra chuỗi có đúng định dạng email không
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        // Validate dữ liệu đầu vào cho trường Email
        if (!email.trim()) {
            newErrors.email = "Please enter Admin Email.";
        } else if (!emailRegex.test(email.trim())) {
            newErrors.email = "Invalid Email format.";
        }

        // Validate dữ liệu đầu vào cho trường Password
        if (!password) {
            newErrors.password = "Please enter password.";
        }

        // Nếu phát hiện có lỗi nhập liệu, cập nhật state lỗi và dừng việc gọi API
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        try {
            setLoading(true);

            // Gọi service API để xác thực thông tin đăng nhập của admin
            await loginAdmin(
                email.trim(),
                password
            );

            // Xác thực thành công -> Chuyển hướng người dùng vào trang dashboard của Admin
            // Thuộc tính replace: true nhằm xóa lịch sử trang hiện tại, ngăn việc ấn nút Back quay lại login
            navigate(
                "/admin",
                {
                    replace: true,
                }
            );
        } catch (err) {
            // Xử lý các lỗi trả về từ server (sai mật khẩu, tài khoản không tồn tại, ...)
            setErrors({ general: err.message || "Login failed." });
        } finally {
            setLoading(false); // Đặt lại cờ trạng thái xử lý sau khi hoàn thành request
        }
    };

    return (
        <div className="login-page">
            {/* Header */}
            <header className="login-header">
                <div className="header-container">
                    <Link to="/" className="header-logo">
                        <div className="logo-icon">
                            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                <line x1="3" y1="6" x2="21" y2="6"/>
                                <path d="M16 10a4 4 0 0 1-8 0"/>
                            </svg>
                        </div>
                        <div className="logo-text">
                            <span className="logo-name">MyStore</span>
                            <span className="logo-subtitle">Admin System</span>
                        </div>
                    </Link>
                </div>
            </header>

            {/* Main Area */}
            <main className="login-main">
                <div className="login-container">
                    {/* 2-column layout */}
                    <div className="login-content">
                        {/* LEFT COLUMN */}
                        <div className="login-left">
                            <div className="back-home-wrap">
                                <Link to="/" className="back-home-btn">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="back-icon">
                                        <line x1="19" y1="12" x2="5" y2="12"></line>
                                        <polyline points="12 19 5 12 12 5"></polyline>
                                    </svg>
                                    <span>Back to Home MyStore</span>
                                </Link>
                            </div>

                            <div className="main-image">
                                <img
                                    src="/images/login-banner.jpg"
                                    alt="iPhone, Laptop & Electronics - MyStore Admin"
                                />
                            </div>

                            <div className="benefits-grid">
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                            <path d="m9 12 2 2 4-4"/>
                                        </svg>
                                    </div>
                                    <h3>Security</h3>
                                    <p>Absolute safety</p>
                                </div>
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                                            <line x1="7" y1="7" x2="7.01" y2="7"/>
                                        </svg>
                                    </div>
                                    <h3>Management</h3>
                                    <p>Easy & Fast</p>
                                </div>
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                                        </svg>
                                    </div>
                                    <h3>Statistics</h3>
                                    <p>Visual & Accurate</p>
                                </div>
                            </div>
                        </div>

                        {/* RIGHT COLUMN - LOGIN CARD */}
                        <div className="login-box">
                            <div className="login-icon">
                                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"/>
                                </svg>
                            </div>

                            <h1>Admin Login</h1>
                            <p className="login-description">
                                Please log in with an admin account to access the MyStore Console.
                            </p>

                            <form onSubmit={handleSubmit}>
                                <div className="input-label-row">
                                    <label htmlFor="email">Admin Email</label>
                                </div>

                                <div className="input-group-custom">
                                    <span className="input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                                            <polyline points="22,6 12,13 2,6"/>
                                        </svg>
                                    </span>
                                    <input
                                        id="email"
                                        type="email"
                                        placeholder="Enter admin email"
                                        value={email}
                                        onChange={(e) => {
                                            setEmail(e.target.value);
                                            setErrors(prev => ({...prev, email: '', general: ''}));
                                        }}
                                        style={{ width: '100%', borderColor: errors.email ? '#ef4444' : '' }}
                                    />
                                </div>
                                {errors.email && (
                                    <div className="field-error" style={{ color: '#ef4444', fontSize: '13px', marginTop: '6px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                        <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" strokeWidth="2"/><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01"/></svg>
                                        {errors.email}
                                    </div>
                                )}

                                <div className="input-label-row" style={{ marginTop: '16px' }}>
                                    <label htmlFor="password">Password</label>
                                </div>

                                <div className="input-group-custom">
                                    <span className="input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                                        </svg>
                                    </span>
                                    <input
                                        id="password"
                                        type="password"
                                        placeholder="Enter password"
                                        value={password}
                                        onChange={(e) => {
                                            setPassword(e.target.value);
                                            setErrors(prev => ({...prev, password: '', general: ''}));
                                        }}
                                        style={{ width: '100%', borderColor: errors.password ? '#ef4444' : '' }}
                                    />
                                </div>
                                {errors.password && (
                                    <div className="field-error" style={{ color: '#ef4444', fontSize: '13px', marginTop: '6px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                        <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" strokeWidth="2"/><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4m0 4h.01"/></svg>
                                        {errors.password}
                                    </div>
                                )}

                                {errors.general && (
                                    <div className="login-error" style={{ marginTop: '16px', padding: '12px 16px', border: '1px solid #fecdd3', borderRadius: '8px', backgroundColor: '#fff1f2', color: '#e11d48', fontSize: '13px', fontWeight: '500' }}>
                                        {errors.general}
                                    </div>
                                )}

                                <button type="submit" className="continue-button" disabled={loading} style={{ marginTop: '24px' }}>
                                    {loading ? "LOGGING IN..." : "LOGIN →"}
                                </button>
                            </form>

                            <p className="note" style={{ marginTop: '24px', textAlign: 'center' }}>
                                © 2026 MyStore. All rights reserved.
                            </p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default AdminLoginPage;