import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import Footer from "../../components/layout/Footer";
import { register } from "../../services/authService";

function Register() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [errors, setErrors] = useState({ email: "", password: "", general: "" });
    const [successMessage, setSuccessMessage] = useState("");
    const navigate = useNavigate();

    const getPasswordStrength = (pass) => {
        if (!pass) return { text: "", color: "transparent", width: "0%" };
        if (pass.length < 6) return { text: "Weak", color: "#dc2626", width: "33%" };
        if (pass.length < 10) return { text: "Medium", color: "#f59e0b", width: "66%" };
        return { text: "Strong", color: "#16a34a", width: "100%" };
    };

    const strength = getPasswordStrength(password);

    const handleRegister = async (e) => {
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

        if (password.length < 6 || password.length > 72) {
            newErrors.password = "Password must be 6–72 characters";
            isValid = false;
        }

        setErrors(newErrors);

        if (!isValid) return;

        try {
            await register(trimmedEmail, password);
            setSuccessMessage("Registration successful!");
            navigate("/login", { replace: true });
        } catch (error) {
            setErrors({ ...newErrors, general: error.message || "Registration failed" });
        }
    };

    return (
        <div className="register-page login-page">
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
            {/* Header */}
            <header className="login-header">
                <div className="header-container">
                    {/* Logo */}
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
                        <Link to="/login" className="menu-item">Login</Link>
                        <Link to="/register" className="menu-item active">Register</Link>
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

            {/* Main */}
            <main className="register-main">
                <div className="register-container">
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

                    <div className="register-card">
                        {/* Left Side: Brand & Benefits */}
                        <div className="register-intro">
                            <div className="register-brand-icon">
                                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                    <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                    <line x1="3" y1="6" x2="21" y2="6"/>
                                    <path d="M16 10a4 4 0 0 1-8 0"/>
                                </svg>
                            </div>

                            <h1>Create an account</h1>
                            <p>
                                Join MyStore today to enjoy shopping for authentic tech products and exclusive member privileges.
                            </p>

                            <div className="register-benefits">
                                <div className="register-benefit">
                                    <div className="benefit-icon">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <rect x="1" y="3" width="15" height="13"/>
                                            <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/>
                                            <circle cx="5.5" cy="18.5" r="2.5"/>
                                            <circle cx="18.5" cy="18.5" r="2.5"/>
                                        </svg>
                                    </div>
                                    <div className="benefit-text">
                                        <strong>2H Fast Delivery</strong>
                                        <span>Track your order and receive it quickly</span>
                                    </div>
                                </div>

                                <div className="register-benefit">
                                    <div className="benefit-icon">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                                        </svg>
                                    </div>
                                    <div className="benefit-text">
                                        <strong>MyMember Privileges</strong>
                                        <span>Earn 5% and get exclusive member offers</span>
                                    </div>
                                </div>

                                <div className="register-benefit">
                                    <div className="benefit-icon">
                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                            <path d="m9 12 2 2 4-4"/>
                                        </svg>
                                    </div>
                                    <div className="benefit-text">
                                        <strong>100% Genuine Warranty</strong>
                                        <span>Instant E-Warranty activation nationwide</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Right Side: Form */}
                        <div className="register-form-section">
                            <div className="register-form-header">
                                
                                <h2>Register Account</h2>
                                <p>Enter email and password to register.</p>
                            </div>

                            <form onSubmit={handleRegister} noValidate>
                                {errors.general && (
                                    <div className="form-error">
                                        {errors.general}
                                    </div>
                                )}
                                <div className="form-group">
                                    <div className="label-row">
                                        <label htmlFor="email">Email Address</label>
                                    </div>

                                    <div className="phone-input">
                                        <span className="country-code">
                                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                                        </span>
                                        <input
                                            id="email"
                                            type="email"
                                            placeholder="Enter your email address"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                        />
                                    </div>
                                    {errors.email && <div style={{ color: '#dc2626', fontSize: '13px', marginTop: '4px' }}>{errors.email}</div>}
                                </div>

                                <div className="form-group" style={{ marginTop: '16px' }}>
                                    <div className="label-row">
                                        <label htmlFor="password">Password</label>
                                    </div>

                                    <div className="phone-input" style={{ paddingLeft: '0', position: 'relative' }}>
                                        <input
                                            id="password"
                                            type={showPassword ? "text" : "password"}
                                            placeholder="Enter password (min 6 chars)"
                                            value={password}
                                            onChange={(e) => setPassword(e.target.value)}
                                            style={{ borderLeft: 'none', borderRadius: '8px', paddingLeft: '16px', width: '100%', paddingRight: '40px' }}
                                        />
                                        <button 
                                            type="button" 
                                            onClick={() => setShowPassword(!showPassword)}
                                            style={{ position: 'absolute', right: '12px', background: 'none', border: 'none', cursor: 'pointer', color: '#64748b', display: 'flex', alignItems: 'center', height: '100%', padding: '0', top: 0 }}
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
                                    {password && (
                                        <div style={{ marginTop: '8px', fontSize: '12px' }}>
                                            <div style={{ height: '4px', background: '#e2e8f0', borderRadius: '2px', overflow: 'hidden' }}>
                                                <div style={{ height: '100%', width: strength.width, background: strength.color, transition: 'all 0.3s' }}></div>
                                            </div>
                                            <div style={{ color: strength.color, marginTop: '4px', fontWeight: '600' }}>{strength.text}</div>
                                        </div>
                                    )}
                                </div>

                                <button type="submit" className="register-button btn-primary" style={{ marginTop: '24px', width: '100%', padding: '12px', borderRadius: '8px', border: 'none', color: '#fff', fontWeight: 'bold' }}>
                                    REGISTER
                                </button>
                            </form>



                            <p className="terms-text">
                                By continuing, you agree to{" "}
                                <a href="#terms">Terms of Use</a> and{" "}
                                <a href="#privacy">Privacy Policy</a> of MyStore.
                            </p>
                        </div>
                    </div>
                </div>
            </main>

            {/* Footer */}
            <Footer />
        </div>
    );
}

export default Register;
