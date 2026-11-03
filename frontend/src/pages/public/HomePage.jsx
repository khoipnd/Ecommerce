import { useState } from "react";
import { Link } from "react-router-dom";
import useProducts from "../../hooks/useProducts";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import ProductCard from "../../components/product/ProductCard";
import { getCategoryKey, getPrimaryImage, handleImageError } from "../../utils/productImages";

const HomePage = () => {
    const { products, loading, error } = useProducts();
    const [heroSlide, setHeroSlide] = useState(0);
    const [phoneFilter, setPhoneFilter] = useState("all");
    const [laptopFilter, setLaptopFilter] = useState("all");
    const [audioFilter, setAudioFilter] = useState("all");

    const slides = products.length ? products.slice(0, 3).map(product => ({
        tag: "FEATURED PRODUCT",
        title: product.name,
        desc: product.description || "Explore this product in our catalog.",
        price: `${Number(product.price).toLocaleString("vi-VN")}đ`,
        oldPrice: "",
        discount: "",
        img: getPrimaryImage(product),
        btnText: "View product",
        link: `/products/${product.id}`,
    })) : [{
        tag: "MYSTORE",
        title: "Explore our products",
        desc: "Browse the catalog for current products and prices.",
        price: "", oldPrice: "", discount: "",
        img: "/assets/devices_banner.png",
        btnText: "Browse products", link: "/products",
    }];
    const activeSlide = slides[heroSlide % slides.length];

    // Product sections use the current API catalog.
    const discountedProducts = products;

    // Dynamic filtering for sections
    const allPhones = discountedProducts.filter(p => getCategoryKey(p) === "phone");
    const phoneProducts = allPhones.filter(p => {
        if (phoneFilter === "apple") return p.name.toLowerCase().includes("iphone");
        if (phoneFilter === "samsung") return p.name.toLowerCase().includes("samsung") || p.name.toLowerCase().includes("galaxy");
        return true;
    }).slice(0, 4);

    const allLaptops = discountedProducts.filter(p => getCategoryKey(p) === "laptop");
    const laptopProducts = allLaptops.filter(p => {
        if (laptopFilter === "macbook") return p.name.toLowerCase().includes("macbook");
        if (laptopFilter === "gaming") return p.name.toLowerCase().includes("asus") || p.name.toLowerCase().includes("rog") || p.name.toLowerCase().includes("predator");
        return true;
    }).slice(0, 4);

    const allAudio = discountedProducts.filter(p => getCategoryKey(p) === "audio");
    const audioProducts = allAudio.filter(p => {
        if (audioFilter === "airpods") return p.name.toLowerCase().includes("airpods");
        if (audioFilter === "headphone") return p.name.toLowerCase().includes("sony") || p.name.toLowerCase().includes("bose") || p.name.toLowerCase().includes("marshall");
        return true;
    }).slice(0, 4);

    const featuredProducts = discountedProducts.slice(0, 4);

    return (
        <div className="home-page">
            {error && <div className="container"><ErrorMessage message="Could not load the current product catalog." /></div>}
            {/* CELLPHONES / APPLE HERO BANNER SECTION */}
            <section className="cellphones-hero-section container">
                <div className="hero-main-banner">
                    <div className="slide-content-left">
                        <span className="hero-slide-tag">{activeSlide.tag}</span>
                        <h2>{activeSlide.title}</h2>
                        <p className="hero-slide-desc">{activeSlide.desc}</p>

                        <div className="hero-slide-price-row">
                            <span className="hero-current-price">{activeSlide.price}</span>
                            <span className="hero-old-price">{activeSlide.oldPrice}</span>
                            {activeSlide.discount && (
                                <span className="discount-badge">{activeSlide.discount}</span>
                            )}
                        </div>

                        <div className="hero-slide-actions">
                            <Link to={activeSlide.link} className="btn-hero-primary">
                                {activeSlide.btnText} <span>→</span>
                            </Link>
                            <Link to="/products" className="btn-hero-secondary">
                                View all {products.length} products
                            </Link>
                        </div>
                    </div>

                    <div className="slide-content-right">
                        <div className="slide-img-container">
                            <img src={activeSlide.img} alt={activeSlide.title} loading={heroSlide === 0 ? "eager" : "lazy"} onError={handleImageError} />
                        </div>
                    </div>

                    {/* Carousel Dots */}
                    <div className="carousel-dots-nav">
                        {slides.map((_, idx) => (
                            <button
                                key={idx}
                                className={`dot-btn ${heroSlide === idx ? "active" : ""}`}
                                onClick={() => setHeroSlide(idx)}
                            ></button>
                        ))}
                    </div>
                </div>

                {/* Right Side Stacked Banners */}
                <div className="hero-side-banners">
                    <div className="side-banner-card banner-1">
                        <span className="side-badge">DISCOVER</span>
                        <h3>Explore our catalog</h3>
                        <p>See current products and prices.</p>
                        <Link to="/products" className="side-link">Browse products <span>→</span></Link>
                    </div>

                    <div className="side-banner-card banner-2">
                        <span className="side-badge highlight">MYSTORE</span>
                        <h3>Find your next device</h3>
                        <p>Browse all available products.</p>
                        <Link to="/products" className="side-link">View products <span>→</span></Link>
                    </div>
                </div>
            </section>

            {/* MEMBER PROMO BANNER */}
            <section className="promo-banner-section container">
                <div className="promo-banner-card">
                    <div className="promo-text">
                        <span className="promo-badge">MYSTORE ACCOUNT</span>
                        <h2>Create an account to manage your orders</h2>
                        <p>Track your orders and checkout with your account.</p>
                        <Link to="/register" className="promo-button">
                            Register an account now <span>→</span>
                        </Link>
                    </div>
                    <div className="promo-visual">
                        <div className="promo-badge-circle">
                            <span>YOUR</span>
                            <strong>ACCOUNT</strong>
                        </div>
                    </div>
                </div>
            </section>

            {/* FLASH SALE / DEALS HOT SECTION */}
            <section className="flash-sale-section container">
                <div className="flash-sale-header">
                    <div className="flash-title-wrap">
                        <span className="flash-icon">⚡</span>
                        <h2>FEATURED PRODUCTS</h2>
                    </div>
                    <Link to="/products" className="flash-view-all">
                        View all products <span>→</span>
                    </Link>
                </div>

                <div className="flash-sale-grid">
                    {featuredProducts.map((product) => (
                        <div key={product.id} className="flash-product-wrapper">
                            <ProductCard product={product} />
                        </div>
                    ))}
                </div>
            </section>

            {/* CATEGORY SHOWCASE 1: SMARTPHONES */}
            <section className="category-showcase-section container">
                <div className="showcase-header">
                    <div className="showcase-title-group">
                        <span className="section-icon">📱</span>
                        <h2>FEATURED PHONES ({allPhones.length} products)</h2>
                    </div>

                    <div className="showcase-tabs">
                        <button 
                            className={`tab-chip ${phoneFilter === 'all' ? 'active' : ''}`}
                            onClick={() => setPhoneFilter('all')}
                        >
                            All
                        </button>
                        <button 
                            className={`tab-chip ${phoneFilter === 'apple' ? 'active' : ''}`}
                            onClick={() => setPhoneFilter('apple')}
                        >
                            iPhone
                        </button>
                        <button 
                            className={`tab-chip ${phoneFilter === 'samsung' ? 'active' : ''}`}
                            onClick={() => setPhoneFilter('samsung')}
                        >
                            Samsung Galaxy
                        </button>
                        <Link to="/products?category=phone" className="tab-chip-more">
                            View all ({allPhones.length}) →
                        </Link>
                    </div>
                </div>

                {loading ? <Loading message="Loading phone list..." /> : (
                    <div className="showcase-products-grid">
                        {phoneProducts.map(product => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                )}
            </section>

            {/* CATEGORY SHOWCASE 2: LAPTOP & MACBOOK */}
            <section className="category-showcase-section container">
                <div className="showcase-header">
                    <div className="showcase-title-group">
                        <span className="section-icon">💻</span>
                        <h2>BEST SELLING LAPTOPS & MACBOOKS ({allLaptops.length} products)</h2>
                    </div>

                    <div className="showcase-tabs">
                        <button 
                            className={`tab-chip ${laptopFilter === 'all' ? 'active' : ''}`}
                            onClick={() => setLaptopFilter('all')}
                        >
                            All
                        </button>
                        <button 
                            className={`tab-chip ${laptopFilter === 'macbook' ? 'active' : ''}`}
                            onClick={() => setLaptopFilter('macbook')}
                        >
                            MacBook
                        </button>
                        <button 
                            className={`tab-chip ${laptopFilter === 'gaming' ? 'active' : ''}`}
                            onClick={() => setLaptopFilter('gaming')}
                        >
                            Laptop Gaming
                        </button>
                        <Link to="/products?category=laptop" className="tab-chip-more">
                            View all ({allLaptops.length}) →
                        </Link>
                    </div>
                </div>

                {loading ? <Loading message="Loading laptop list..." /> : (
                    <div className="showcase-products-grid">
                        {laptopProducts.map(product => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                )}
            </section>

            {/* CATEGORY SHOWCASE 3: TAI NGHE & ÂM THANH */}
            <section className="category-showcase-section container">
                <div className="showcase-header">
                    <div className="showcase-title-group">
                        <span className="section-icon">🎧</span>
                        <h2>PREMIUM HEADPHONES & AUDIO ({allAudio.length} products)</h2>
                    </div>

                    <div className="showcase-tabs">
                        <button 
                            className={`tab-chip ${audioFilter === 'all' ? 'active' : ''}`}
                            onClick={() => setAudioFilter('all')}
                        >
                            All
                        </button>
                        <button 
                            className={`tab-chip ${audioFilter === 'airpods' ? 'active' : ''}`}
                            onClick={() => setAudioFilter('airpods')}
                        >
                            AirPods
                        </button>
                        <button 
                            className={`tab-chip ${audioFilter === 'headphone' ? 'active' : ''}`}
                            onClick={() => setAudioFilter('headphone')}
                        >
                            Sony / Marshall / Bose
                        </button>
                        <Link to="/products?category=audio" className="tab-chip-more">
                            View all ({allAudio.length}) →
                        </Link>
                    </div>
                </div>

                {loading ? <Loading message="Loading audio list..." /> : (
                    <div className="showcase-products-grid">
                        {audioProducts.map(product => (
                            <ProductCard key={product.id} product={product} />
                        ))}
                    </div>
                )}
            </section>

            {/* TRUST & SERVICE COMMITMENTS BANNER */}
            <section className="trust-commitments-section container">
                <div className="trust-grid">
                    <div className="trust-card">
                        <div className="trust-icon-box">✓</div>
                        <div className="trust-text">
                            <strong>100% Authentic</strong>
                            <span>Authorized Apple, Samsung, Sony brand</span>
                        </div>
                    </div>

                    <div className="trust-card">
                        <div className="trust-icon-box">🛡️</div>
                        <div className="trust-text">
                            <strong>12-Month Warranty</strong>
                            <span>1-to-1 exchange in 30 days if defective</span>
                        </div>
                    </div>

                    <div className="trust-card">
                        <div className="trust-icon-box">🚚</div>
                        <div className="trust-text">
                            <strong>2H Fast Delivery</strong>
                            <span>Free shipping for orders from 500,000VND</span>
                        </div>
                    </div>

                    <div className="trust-card">
                        <div className="trust-icon-box">💳</div>
                        <div className="trust-text">
                            <strong>0% Interest Installment</strong>
                            <span>Simple procedures, fast 5-minute approval</span>
                        </div>
                    </div>
                </div>
            </section>


        </div>
    );
};

export default HomePage;
