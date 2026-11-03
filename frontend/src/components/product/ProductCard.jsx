import { Link, useNavigate } from "react-router-dom";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";
import { useCart } from "../../contexts/CartContext";
import { readSession } from "../../services/authStorage";


const ProductCard = ({ product }) => {
    const primaryImage = getPrimaryImage(product);
    const { addToCart } = useCart();

    // Format giá tiền: nếu giá > 1000 thì hiển thị định dạng VNĐ (VD: 1.000.000đ), ngược lại hiển thị $
    const formattedPrice = typeof product.price === 'number' 
        ? product.price > 1000 
            ? `${product.price.toLocaleString('vi-VN')}đ` 
            : `$${product.price}`
        : product.price;

    const currentPrice = Number(product.price);
    const originalPrice = Number(product.originalPrice);
    const hasDiscount = product.originalPrice != null && Number.isFinite(currentPrice) && Number.isFinite(originalPrice) && originalPrice > currentPrice && currentPrice > 0;
    const formattedOriginalPrice = hasDiscount
        ? originalPrice > 1000 ? `${originalPrice.toLocaleString('vi-VN')}đ` : `$${originalPrice}`
        : null;
    const discountPercent = hasDiscount ? Math.round((1 - currentPrice / originalPrice) * 100) : null;

    const stockLeft = product.stockQuantity > 0 ? product.stockQuantity : 0;
    const navigate = useNavigate();

    // Hàm xử lý sự kiện khi người dùng click vào nút Thêm vào giỏ hàng
    const handleAddToCart = (e) => {
        e.preventDefault(); // Ngăn chặn sự kiện click lan truyền tới Link bọc ngoài card
        if (!readSession()) {
            // Yêu cầu chuyển hướng đến trang đăng nhập nếu chưa xác thực
            navigate("/login");
            return;
        }
        addToCart(product, 1); // Gọi context để thêm 1 sản phẩm vào giỏ hàng
    };

    // Hàm xử lý sự kiện khi người dùng click Mua ngay
    const handleBuyNow = (e) => {
        e.preventDefault();
        if (!readSession()) {
            navigate("/login");
            return;
        }
        // Chuẩn bị payload thông tin sản phẩm để chuyển thẳng qua trang Thanh toán (Checkout)
        const buyNowItem = { product, quantity: 1 };
        navigate("/checkout", { state: { buyNowItem } });
    };

    return (
        <article className="shopee-product-card">
            <Link to={`/products/${product.id}`} className="card-media-link" title={product.name}>
                <div className="card-media-wrapper">
                    <img
                        src={primaryImage}
                        alt={product.name}
                        loading="lazy"
                        onError={handleImageError}
                    />
                </div>
                
                <div className="card-content-body">
                    <h3 className="card-title">
                        {product.name}
                    </h3>

                    <div className="card-price-row">
                        <span className="current-price">{formattedPrice}</span>
                    </div>

                    {hasDiscount && (
                        <div className="card-old-price-row">
                            <span className="old-price">{formattedOriginalPrice}</span>
                            <span className="discount-badge">-{discountPercent}%</span>
                        </div>
                    )}

                    <div className="flash-sale-bar">
                        <span className="fire-icon" aria-hidden="true">🔥</span>
                        <span className="stock-text">
                            {stockLeft > 0 ? `${stockLeft} in stock` : "Sold out"}
                        </span>
                    </div>
                </div>
            </Link>
            
            <div className="card-footer-action">
                <button type="button" className="btn-add-to-cart" onClick={handleAddToCart}>
                    Add to Cart
                </button>
                <button type="button" className="buy-now-btn" onClick={handleBuyNow}>
                    Buy Now
                </button>
            </div>
        </article>
    );
};

export default ProductCard;
