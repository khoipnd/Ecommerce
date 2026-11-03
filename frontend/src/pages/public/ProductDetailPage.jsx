import { useState, useEffect } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import useProductDetail from "../../hooks/useProductDetail";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import { getProductGallery, getProductSpecs, getCategoryKey, handleImageError } from "../../utils/productImages";

import { useCart } from "../../contexts/CartContext";
import { readSession } from "../../services/authStorage";

const ProductDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { product, loading, error } = useProductDetail(id);
    const { addToCart } = useCart();
    
    const [selectedImage, setSelectedImage] = useState("");
    const [selectedColor, setSelectedColor] = useState(0);
    const [quantity, setQuantity] = useState(1);
    const [addedToCart, setAddedToCart] = useState(false);
    const [activeTab, setActiveTab] = useState("specs");

    const galleryImages = product ? getProductGallery(product) : [];
    const specsList = product ? getProductSpecs(product) : [];
    const catKey = product ? getCategoryKey(product) : 'phone';

    useEffect(() => {
        if (galleryImages.length > 0) {
            setSelectedImage(galleryImages[0]);
        }
    }, [product]);

    if (loading) {
        return (
            <div className="page-container">
                <Loading message="Loading thông tin chi tiết products..." />
            </div>
        );
    }

    if (error || !product) {
        return (
            <div className="page-container">
                <ErrorMessage message={error || "Products bạn tìm kiếm không tồn tại."} />
                <div style={{ marginTop: 20 }}>
                    <Link to="/products" className="btn-detail">
                        ← Quay lại danh sách products
                    </Link>
                </div>
            </div>
        );
    }

    const formattedPrice = typeof product.price === 'number'
        ? product.price > 1000
            ? `${product.price.toLocaleString('vi-VN')}đ`
            : `$${product.price}`
        : product.price;

    const oldPrice = typeof product.price === 'number' && product.price > 1000
        ? `${Math.round(product.price * 1.15).toLocaleString('vi-VN')}đ`
        : null;

    const isOutOfStock = (product.stockQuantity ?? 0) === 0;

    const colorVariants = catKey === 'phone' || catKey === 'laptop'
        ? [
            { name: "Space Black", color: "#1d1d1f" },
            { name: "Natural Titanium", color: "#8a8682" },
            { name: "Silver", color: "#e3e4e5" },
            { name: "Deep Blue", color: "#223547" }
          ]
        : catKey === 'watch'
        ? [
            { name: "Titanium Tự Nhiên", color: "#c5c5c7" },
            { name: "Đen Nhám", color: "#2d2d2e" },
            { name: "Vàng Gold", color: "#e0d0b0" }
          ]
        : [
            { name: "Đen Midnight", color: "#181a1b" },
            { name: "Trắng Starlight", color: "#f0ece1" },
            { name: "Xanh Sky", color: "#8ab4f8" }
          ];

    const handleAddToCart = () => {
        if (!readSession()) {
            navigate("/login");
            return;
        }
        const colorOption = colorVariants[selectedColor]?.name;
        addToCart(product, quantity, { color: colorOption });
        setAddedToCart(true);
        setTimeout(() => setAddedToCart(false), 2500);
    };

    const handleBuyNow = () => {
        if (!readSession()) {
            navigate("/login");
            return;
        }
        const colorOption = colorVariants[selectedColor]?.name;
        const buyNowItem = { product, quantity, options: { color: colorOption } };
        navigate("/checkout", { state: { buyNowItem } });
    };

    return (
        <main className="product-detail-page-wrapper">
            <div className="container">
                {/* Breadcrumbs */}
                <div className="detail-breadcrumbs">
                    <Link to="/">Trang chủ</Link>
                    <span>/</span>
                    <Link to="/products">Products</Link>
                    <span>/</span>
                    <span className="current">{product.name}</span>
                </div>

                <div className="product-detail-main-card">
                    {/* Gallery Section */}
                    <div className="product-detail-gallery">
                        <span className="detail-badge">{product.badge || "Chính hãng 100% Apple"}</span>
                        
                        <div className="main-image-wrap">
                            <img
                                src={selectedImage || galleryImages[0]}
                                alt={product.name}
                                loading="lazy"
                                onError={handleImageError}
                            />
                        </div>

                        {/* Gallery Thumbnails */}
                        {galleryImages.length > 1 && (
                            <div className="gallery-thumbnails">
                                {galleryImages.map((img, idx) => (
                                    <button
                                        key={idx}
                                        type="button"
                                        className={`thumb-btn ${selectedImage === img ? "active" : ""}`}
                                        onClick={() => setSelectedImage(img)}
                                    >
                                        <img src={img} alt={`${product.name} view ${idx + 1}`} loading="lazy" onError={handleImageError} />
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Information Section */}
                    <div className="product-detail-info-wrap">
                        <div className="category-meta-tag">
                            {product.category?.name || "Công nghệ cao cấp"}
                        </div>

                        <h1 className="detail-product-title">{product.name}</h1>

                        <div className="rating-row">
                            <span className="stars">⭐⭐⭐⭐⭐</span>
                            <span className="rating-score">{product.rating || "4.9"}/5</span>
                            <span className="divider">•</span>
                            <span className="reviews-count">{product.reviewCount || 128} đánh giá đã mua</span>
                            <span className="divider">•</span>
                            <span className="verified-purchase">✓ Đã xác minh</span>
                        </div>

                        {/* Price Box */}
                        <div className="detail-price-box">
                            <div className="price-title-row">
                                <span className="price-label">Giá ưu đãi niêm yết</span>
                                <span className="vat-tag">Đã bao gồm VAT</span>
                            </div>
                            <div className="price-values">
                                <span className="main-price">{formattedPrice}</span>
                                {oldPrice && <span className="old-price">{oldPrice}</span>}
                                {oldPrice && <span className="discount-badge">-15%</span>}
                            </div>
                        </div>

                        {/* Color Selector */}
                        <div className="color-selector-wrap">
                            <label className="selector-label">
                                Màu sắc: <strong>{colorVariants[selectedColor]?.name}</strong>
                            </label>
                            <div className="color-options">
                                {colorVariants.map((item, idx) => (
                                    <button
                                        key={idx}
                                        type="button"
                                        className={`color-btn ${selectedColor === idx ? "selected" : ""}`}
                                        onClick={() => setSelectedColor(idx)}
                                        title={item.name}
                                    >
                                        <span className="color-swatch" style={{ backgroundColor: item.color }}></span>
                                        <span className="color-name">{item.name}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Perks Grid */}
                        <div className="detail-perks-grid">
                            <div className="perk-item">
                                <span className="perk-icon">🚚</span>
                                <div>
                                    <strong>Giao siêu tốc 2H</strong>
                                    <span>Miễn phí giao hàng toàn quốc</span>
                                </div>
                            </div>
                            <div className="perk-item">
                                <span className="perk-icon">🛡</span>
                                <div>
                                    <strong>Bảo hành 12 tháng</strong>
                                    <span>1 đổi 1 trong 30 ngày nếu lỗi</span>
                                </div>
                            </div>
                            <div className="perk-item">
                                <span className="perk-icon">💎</span>
                                <div>
                                    <strong>Cam kết 100% chính hãng</strong>
                                    <span>Bồi thường 200% nếu phát hiện giả</span>
                                </div>
                            </div>
                        </div>

                        {/* Short Description */}
                        <p className="detail-short-desc">
                            {product.description || "Products công nghệ thế hệ mới chính hãng Apple / Samsung. Thiết kế sang trọng, vi xử lý mạnh mẽ, trải nghiệm vượt trội."}
                        </p>

                        {/* Quantity & Actions */}
                        {!isOutOfStock ? (
                            <div className="purchase-controls">
                                <div className="quantity-wrap">
                                    <span className="qty-label">Quantity:</span>
                                    <div className="quantity-selector">
                                        <button 
                                            type="button" 
                                            onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                                        >
                                            -
                                        </button>
                                        <span>{quantity}</span>
                                        <button 
                                            type="button" 
                                            onClick={() => setQuantity((q) => Math.min(product.stockQuantity || 10, q + 1))}
                                        >
                                            +
                                        </button>
                                    </div>
                                </div>

                                <div className="action-buttons-group">
                                    <button 
                                        type="button" 
                                        className={`btn-add-cart ${addedToCart ? "added" : ""}`}
                                        onClick={handleAddToCart}
                                    >
                                        {addedToCart ? "✓ Đã thêm ando giỏ hàng!" : "🛒 Add to Cart"}
                                    </button>

                                    <button 
                                        type="button"
                                        className="btn-buy-now border-0"
                                        onClick={handleBuyNow}
                                    >
                                        Buy Now <span>→</span>
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="out-of-stock-banner">
                                ❌ Products hiện đang tạm hết hàng. Vui lòng quay lại sau hoặc chọn products khác!
                            </div>
                        )}
                    </div>
                </div>

                {/* Technical Specifications & Details Section */}
                <div className="product-specifications-card">
                    <div className="spec-tab-header">
                        <button 
                            className={`tab-btn ${activeTab === 'specs' ? 'active' : ''}`}
                            onClick={() => setActiveTab('specs')}
                        >
                            ⚙️ Thông Số Kỹ Thuật
                        </button>
                        <button 
                            className={`tab-btn ${activeTab === 'desc' ? 'active' : ''}`}
                            onClick={() => setActiveTab('desc')}
                        >
                            📝 Mô Tả Chi Tiết
                        </button>
                        <button 
                            className={`tab-btn ${activeTab === 'policy' ? 'active' : ''}`}
                            onClick={() => setActiveTab('policy')}
                        >
                            🔰 Chính Sách Bảo Hành
                        </button>
                    </div>

                    {activeTab === 'specs' && (
                        <div className="specs-content-grid">
                            {specsList.map((spec, index) => (
                                <div key={index} className="spec-row-item">
                                    <span className="spec-label">{spec.label}</span>
                                    <span className="spec-val">{spec.val}</span>
                                </div>
                            ))}
                            <div className="spec-row-item">
                                <span className="spec-label">Tình trạng kho hàng</span>
                                <span className="spec-val highlight-stock">Sẵn hàng ({product.stockQuantity ?? 0} products)</span>
                            </div>
                        </div>
                    )}

                    {activeTab === 'desc' && (
                        <div className="tab-desc-content">
                            <h3>Giới thiệu về {product.name}</h3>
                            <p>{product.description}</p>
                            <p style={{ marginTop: 12 }}>
                                Thiết bị được sản xuất theo dây chuyền công nghệ cao cấp nhất năm 2026, tích hợp vi xử lý tối ưu điện năng, hệ điều hành mượt mà and màn hình hiển thị đỉnh cao chuẩn màu sắc.
                            </p>
                        </div>
                    )}

                    {activeTab === 'policy' && (
                        <div className="tab-policy-content">
                            <ul>
                                <li><strong>Bảo hành chính hãng:</strong> 12 tháng tại tất cả trung tâm bảo hành ủy quyền toàn quốc.</li>
                                <li><strong>Đổi mới 30 ngày:</strong> Áp dụng 1 đổi 1 ngay lập tức nếu có lỗi từ nhà sản xuất.</li>
                                <li><strong>Giao hàng & Kiểm tra:</strong> Cho phép mở hộp kiểm tra ngoại hình products trước khi thanh toán.</li>
                            </ul>
                        </div>
                    )}
                </div>
            </div>
        </main>
    );
};

export default ProductDetailPage;
