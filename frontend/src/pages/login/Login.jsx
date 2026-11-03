import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Footer from "../../components/layout/Footer";
import { login } from "../../services/authService";

const Login = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({ email: "", password: "", general: "" });
    const [successMessage, setSuccessMessage] = useState("");

    // Hàm xử lý quá trình đăng nhập của người dùng thường
    const handleContinue = async (e) => {
        e.preventDefault();
        
        let newErrors = { email: "", password: "", general: "" };
        let isValid = true;

        const rawEmail = email;
        const trimmedEmail = email.trim();
        
        if (!rawEmail) {
            newErrors.email = "Email is required";
            isValid = false;
        } else if (rawEmail !== trimmedEmail) {
            newErrors.email = "Invalid email format";
            isValid = false;
        } else if (trimmedEmail.length > 254) {
            newErrors.email = "Email must be less than 255 characters";
            isValid = false;
        } else if (trimmedEmail.includes("<script>") || trimmedEmail.includes("</")) {
            newErrors.email = "Invalid email format";
            isValid = false;
        } else {
            const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailRegex.test(trimmedEmail)) {
                newErrors.email = "Invalid email format";
                isValid = false;
            }
        }

        if (!password) {
            newErrors.password = "Password is required";
            isValid = false;
        }

        setErrors(newErrors);

        if (!isValid) return;

        try {
            await login(trimmedEmail, password);
            setSuccessMessage("Login successful!");
            navigate(location.state?.from || "/", { replace: true, state: location.state?.checkoutState });
        } catch (error) {
            setErrors({ ...newErrors, general: error.message || "Login failed" });
        }
    };

    return (
        <div className="login-page">
            {/* Hiển thị thông báo màu xanh lá cây khi đăng nhập thành công */}
            {successMessage && (
                <div style={{
                    position: 'fixed',
                    top: '20px',
                    right: '20px',
                    background: '#22c55e',
                    color: '#fff',
                    padding: '16px 24px',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    zIndex: 9999,
                    fontWeight: 'bold',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px'
                }}>
                    <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                    </svg>
                    {successMessage}
                </div>
            )}
            {/* Phần Header của trang Đăng nhập */}
            <header className="login-header">
                <div className="header-container">
                    {/* Logo dẫn về trang chủ */}
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
                            <span className="logo-subtitle">Authentic Technology</span>
                        </div>
                    </Link>

                    {/* Features Badges in Center */}
                    <div className="header-features">
                        <div className="feature-item">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                <path d="m9 12 2 2 4-4"/>
                            </svg>
                            <span>100% Genuine</span>
                        </div>
                        <div className="feature-item">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <rect x="1" y="3" width="15" height="13"/>
                                <polygon points="16 8 20 8 23 11 23 16 16 16 8"/>
                                <circle cx="5.5" cy="18.5" r="2.5"/>
                                <circle cx="18.5" cy="18.5" r="2.5"/>
                            </svg>
                            <span>2H Fast Delivery</span>
                        </div>
                        <div className="feature-item">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M3 18v-6a9 9 0 0 1 18 0v6"/>
                                <path d="M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z"/>
                            </svg>
                            <span>Hotline: 1800 6868</span>
                        </div>
                    </div>

                    {/* Header Menu on Right */}
                    <div className="header-menu">
                        <Link to="/products" className="menu-item">Store</Link>
                        <Link to="/login" className="menu-item active">Login</Link>
                        <Link to="/register" className="menu-item">Register</Link>
                        <Link to="/login" className="menu-item">Track Order</Link>
                        <div className="user-icon-btn">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                        </div>
                    </div>
                </div>
            </header>

            {/* Khu vực nội dung chính của trang Đăng nhập */}
            <main className="login-main">
                <div className="login-container">
                    {/* Bố cục chia 2 cột: Trái (Banner và lợi ích) - Phải (Form đăng nhập) */}
                    <div className="login-content">
                        {/* CỘT BÊN TRÁI: Hình ảnh quảng cáo và thông tin */}
                        <div className="login-left">
                            {/* Nút quay lại trang chủ */}
                            <div className="back-home-wrap">
                                <Link to="/" className="back-home-btn">
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="back-icon">
                                        <line x1="19" y1="12" x2="5" y2="12"></line>
                                        <polyline points="12 19 5 12 12 5"></polyline>
                                    </svg>
                                    <span>Back to Home MyStore</span>
                                </Link>
                            </div>

                            {/* 3D Electronics Banner */}
                            <div className="main-image">
                                <img
                                    src="https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?auto=format&fit=crop&w=800&q=80"
                                    alt="iPhone, Laptop & Electronics - MyStore"
                                />
                            </div>

                            {/* 3 Benefits Cards */}
                            <div className="benefits-grid">
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                            <path d="m9 12 2 2 4-4"/>
                                        </svg>
                                    </div>
                                    <h3>100%</h3>
                                    <p>Authentic</p>
                                </div>
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                                            <line x1="7" y1="7" x2="7.01" y2="7"/>
                                        </svg>
                                    </div>
                                    <h3>Earn 5%</h3>
                                    <p>MyMember</p>
                                </div>
                                <div className="benefit-card">
                                    <div className="benefit-icon">
                                        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
                                        </svg>
                                    </div>
                                    <h3>E-Warranty</h3>
                                    <p>Instant Activation</p>
                                </div>
                            </div>

                            {/* Trust customer statement */}
                            <div className="customer-trust">
                                Over <strong>1,200,000+</strong> tech enthusiasts trust MyStore every day.
                            </div>
                        </div>

                        {/* CỘT BÊN PHẢI: Form Đăng nhập */}
                        <div className="login-box">
                            <div className="login-icon">
                                <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                    <line x1="12" y1="18" x2="12.01" y2="18"/>
                                </svg>
                            </div>

                            <h1>Login</h1>
                            <p className="login-description">
                                Login with email and password
                            </p>

                            <form onSubmit={handleContinue} noValidate>
                                {errors.general && (
                                    <div className="form-error">
                                        {errors.general}
                                    </div>
                                )}
                                <div className="input-label-row">
                                    <label htmlFor="email">Email Address</label>
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
                                        placeholder="Enter email address"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        style={{ width: '100%' }}
                                    />
                                </div>
                                {errors.email && <div style={{ color: '#dc2626', fontSize: '13px', marginTop: '4px' }}>{errors.email}</div>}

                                <div className="input-label-row" style={{ marginTop: '16px' }}>
                                    <label htmlFor="password">Password</label>
                                </div>

                                <div className="input-group-custom" style={{ position: 'relative' }}>
                                    <span className="input-icon">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#64748b" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                                        </svg>
                                    </span>
                                    <input
                                        id="password"
                                        type={showPassword ? "text" : "password"}
                                        placeholder="Enter password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        style={{ width: '100%', paddingRight: '40px' }}
                                    />
                                    <button 
                                        type="button" 
                                        onClick={() => setShowPassword(!showPassword)}
                                        style={{ position: 'absolute', right: '12px', background: 'none', border: 'none', cursor: 'pointer', color: '#64748b', display: 'flex', alignItems: 'center', height: '100%', padding: '0' }}
                                        aria-label={showPassword ? "Hide password" : "Show password"}
                                    >
                                        {showPassword ? (
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                                        ) : (
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                                        )}
                                    </button>
                                </div>
                                {errors.password && <div style={{ color: '#dc2626', fontSize: '13px', marginTop: '4px' }}>{errors.password}</div>}


                                <button type="submit" className="continue-button btn-primary" style={{ marginTop: '24px', width: '100%', padding: '12px', borderRadius: '8px', border: 'none', color: '#fff', fontWeight: 'bold' }}>
                                    LOGIN
                                </button>
                            </form>



                            <div className="register-text">
                                New customer without an account?{" "}
                                <Link to="/register">Register now</Link>
                            </div>

                            <Link to="/admin/login" className="admin-login">
                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: '6px', verticalAlign: '-2px' }}>
                                    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                                    <line x1="8" y1="21" x2="16" y2="21"/>
                                    <line x1="12" y1="17" x2="12" y2="21"/>
                                </svg>
                                Administrator? Login MyStore Console
                            </Link>
                        </div>
                    </div>
                </div>
            </main>

            {/* Footer */}
            <Footer />
        </div>
    );
};

export default Login;
