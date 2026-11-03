import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useCart } from "../../contexts/CartContext";
import useAuth from "../../hooks/useAuth";
import { logout } from "../../services/authService";

function Header() {
    const navigate = useNavigate();
    const location = useLocation();
    const [searchQuery, setSearchQuery] = useState("");
    const [isCategoryMenuOpen, setIsCategoryMenuOpen] = useState(false);
    const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
    
    const auth = useAuth();
    const userEmail = auth?.email;
    const displayName = auth?.fullName?.trim() || userEmail?.split('@')[0];

    const { cartCount, toggleCart } = useCart();

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/products?search=${encodeURIComponent(searchQuery)}`);
        } else {
            navigate(`/products`);
        }
    };

    const isActive = (path) => {
        if (path === "/" && (location.pathname === "/" || location.pathname === "/home")) return true;
        return location.pathname === path || location.search.includes(`category=${path.replace("/products?category=", "")}`);
    };

    const categoriesList = [
        { id: "phone", name: "Phones", icon: "📱", desc: "iPhone, Samsung, Xiaomi" },
        { id: "laptop", name: "Laptop & Computers", icon: "💻", desc: "MacBook, Asus, Dell" },
        { id: "watch", name: "Smartwatch", icon: "⌚", desc: "Apple Watch, Galaxy Watch" },
        { id: "headphone", name: "Audio & Headphones", icon: "🎧", desc: "AirPods, Sony, JBL" },
        { id: "accessories", name: "Premium Accessories", icon: "⚡", desc: "Sạc MagSafe, Cáp, Case" }
    ];

    return (
        <header className="site-header-wrapper sticky-top">
            {/* Top Bar */}
            <div className="header-top-bar">
                <div className="header-container container">
                    {/* Brand Logo */}
                    <Link to="/" className="header-brand">
                        <div className="brand-icon">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
                                <line x1="3" y1="6" x2="21" y2="6"/>
                                <path d="M16 10a4 4 0 0 1-8 0"/>
                            </svg>
                        </div>
                        <div className="brand-text">
                            <span className="brand-name">MyStore</span>
                            <span className="brand-tag">CellPhone Tech</span>
                        </div>
                    </Link>

                    {/* Clean CellphoneS Style Search Form */}
                    <form className="header-search-form" onSubmit={handleSearch}>
                        <div className="search-input-wrap">
                            <svg className="search-input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="11" cy="11" r="8"/>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                            </svg>
                            <input
                                type="text"
                                placeholder="What are you looking for? (e.g. iPhone 16, MacBook...)"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                            <button type="submit" className="search-btn">
                                <span>Search</span>
                            </button>
                        </div>
                    </form>

                    {/* Header Action Controls */}
                    <div className="header-actions">
                        <Link to={userEmail ? "/orders" : "/login"} className="header-action-item">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                <circle cx="12" cy="10" r="3"/>
                            </svg>
                            <div className="action-text">
                                <span className="action-label">Track</span>
                                <span className="action-val">Orders</span>
                            </div>
                        </Link>

                        <Link to="/cart" className="header-action-item cart-action">
                            <div className="cart-icon-wrap">
                                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <circle cx="9" cy="21" r="1"/>
                                    <circle cx="20" cy="21" r="1"/>
                                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                                </svg>
                                {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
                            </div>
                            <div className="action-text">
                                <span className="action-label">Cart</span>
                                <span className="action-val">{cartCount} items</span>
                            </div>
                        </Link>

                        <div className="header-user-menu">
                            {userEmail ? (
                                <div className="user-dropdown-container">
                                    <button type="button" className="user-menu-trigger" onClick={() => setIsUserMenuOpen(!isUserMenuOpen)} title={`${auth?.fullName?.trim() || displayName} · ${userEmail}`} aria-expanded={isUserMenuOpen} aria-label={`Account menu for ${displayName}`}>
                                        <span className="user-avatar">
                                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                                <circle cx="12" cy="7" r="4"/>
                                            </svg>
                                        </span>
                                        <span className="user-greeting">
                                            <span className="user-hello">Hello,</span>
                                            <span className="user-name">{displayName}</span>
                                        </span>
                                    </button>
                                    
                                    {isUserMenuOpen && (
                                        <div style={{
                                            position: 'absolute',
                                            top: '100%',
                                            right: '0',
                                            marginTop: '12px',
                                            background: '#fff',
                                            boxShadow: '0 1px 4px rgba(0,0,0,0.2)',
                                            borderRadius: '2px',
                                            zIndex: 9999,
                                            width: '180px',
                                            display: 'block'
                                        }}>
                                            {/* Triangle pointer */}
                                            <div style={{
                                                position: 'absolute',
                                                top: '-8px',
                                                right: '12px',
                                                width: '0',
                                                height: '0',
                                                borderLeft: '8px solid transparent',
                                                borderRight: '8px solid transparent',
                                                borderBottom: '8px solid #fff',
                                            }}></div>
                                            
                                            <div style={{ padding: '4px 0' }}>
                                                <div className="user-menu-identity">
                                                    <strong>{displayName}</strong>
                                                    <small>{userEmail}</small>
                                                </div>

                                                <Link to="/orders" style={{
                                                    display: 'block',
                                                    padding: '12px 16px',
                                                    color: '#333',
                                                    textDecoration: 'none',
                                                    fontSize: '14px',
                                                    transition: 'color 0.2s, background 0.2s',
                                                    backgroundColor: 'transparent'
                                                }}
                                                onMouseOver={(e) => { e.currentTarget.style.color = '#00bfa5'; e.currentTarget.style.background = '#fafafa'; }}
                                                onMouseOut={(e) => { e.currentTarget.style.color = '#333'; e.currentTarget.style.background = 'transparent'; }}
                                                >
                                                    My Orders
                                                </Link><button 
                                                    onClick={() => {
                                                        logout();
                                                        setIsUserMenuOpen(false);
                                                        navigate('/login');
                                                    }}
                                                    style={{
                                                        width: '100%',
                                                        padding: '12px 16px',
                                                        border: 'none',
                                                        background: 'transparent',
                                                        textAlign: 'left',
                                                        cursor: 'pointer',
                                                        color: '#333',
                                                        fontSize: '14px',
                                                        transition: 'color 0.2s, background 0.2s',
                                                    }}
                                                    onMouseOver={(e) => { e.currentTarget.style.color = '#00bfa5'; e.currentTarget.style.background = '#fafafa'; }}
                                                    onMouseOut={(e) => { e.currentTarget.style.color = '#333'; e.currentTarget.style.background = 'transparent'; }}
                                                >
                                                    Logout
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            ) : (
                                <Link to="/login" className="user-login-btn" style={{ padding: '8px 16px', borderRadius: '10px' }}>
                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                        <circle cx="12" cy="7" r="4"/>
                                    </svg>
                                    <span>Login</span>
                                </Link>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Bottom Navigation Menu with CellphoneS Category Bar */}
            <nav className="header-nav-bar">
                <div className="header-container container">
                    <div className="nav-left-wrapper">
                        {/* CellphoneS Style All-Categories Dropdown Trigger */}
                        <div className="category-dropdown-container">
                            <button 
                                type="button" 
                                className={`category-menu-btn ${isCategoryMenuOpen ? "active" : ""}`}
                                onClick={() => setIsCategoryMenuOpen(!isCategoryMenuOpen)}
                            >
                                <span className="menu-hamburger">☰</span>
                                <span>All categories</span>
                                <span className="chevron-down">▾</span>
                            </button>

                            {/* Dropdown Menu */}
                            {isCategoryMenuOpen && (
                                <div className="category-dropdown-menu" onMouseLeave={() => setIsCategoryMenuOpen(false)}>
                                    {categoriesList.map((cat) => (
                                        <Link 
                                            key={cat.id} 
                                            to={`/products?category=${cat.id}`}
                                            className="cat-dropdown-item"
                                            onClick={() => setIsCategoryMenuOpen(false)}
                                        >
                                            <span className="cat-drop-icon">{cat.icon}</span>
                                            <div className="cat-drop-text">
                                                <strong>{cat.name}</strong>
                                                <small>{cat.desc}</small>
                                            </div>
                                            <span className="cat-drop-arrow">›</span>
                                        </Link>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Navigation Links */}
                        <ul className="nav-menu">
                            <li className={location.pathname === "/products" && !location.search ? "active" : ""}>
                                <Link to="/products">
                                    <span className="nav-icon">🛍️</span>
                                    <span>All</span>
                                </Link>
                            </li>
                            <li className={location.search.includes("category=phone") ? "active" : ""}>
                                <Link to="/products?category=phone">
                                    <span className="nav-icon">📱</span>
                                    <span>Phone</span>
                                </Link>
                            </li>
                            <li className={location.search.includes("category=laptop") ? "active" : ""}>
                                <Link to="/products?category=laptop">
                                    <span className="nav-icon">💻</span>
                                    <span>Laptop</span>
                                </Link>
                            </li>
                            <li className={location.search.includes("category=watch") ? "active" : ""}>
                                <Link to="/products?category=watch">
                                    <span className="nav-icon">⌚</span>
                                    <span>Smartwatch</span>
                                </Link>
                            </li>
                            <li className={location.search.includes("category=headphone") ? "active" : ""}>
                                <Link to="/products?category=headphone">
                                    <span className="nav-icon">🎧</span>
                                    <span>Headphones</span>
                                </Link>
                            </li>
                            <li className={location.search.includes("category=accessories") ? "active" : ""}>
                                <Link to="/products?category=accessories">
                                    <span className="nav-icon">⚡</span>
                                    <span>Accessories</span>
                                </Link>
                            </li>
                            <li>
                                <Link to="/products?sort=discount">
                                    <span className="nav-icon" style={{color: '#ef4444'}}>🔥</span>
                                    <span style={{color: '#ef4444', fontWeight: 'bold'}}>Hot Deals</span>
                                </Link>
                            </li>
                            <li>
                                <Link to="/products?sort=new">
                                    <span className="nav-icon">✨</span>
                                    <span>New Products</span>
                                </Link>
                            </li>
                        </ul>
                    </div>

                </div>
            </nav>
        </header>
    );
}

export default Header;
