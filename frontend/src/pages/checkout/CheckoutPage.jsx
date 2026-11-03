import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useCart } from "../../contexts/CartContext";
import { createOrder } from "../../services/orderService";
import useAuth from "../../hooks/useAuth";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";
import { formatPrice } from "../../utils/formatPrice";


const CheckoutPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const auth = useAuth();
    const { cartItems: contextCartItems, clearCart } = useCart();
    const [isProcessing, setIsProcessing] = useState(false);

    // Kiểm tra: Nếu navigate từ nút Mua Ngay (buyNowItem) thì chỉ lấy sản phẩm đó, nếu không thì dùng giỏ hàng chung
    const cartItems = location.state?.buyNowItem 
        ? [location.state.buyNowItem] 
        : contextCartItems;

    // Tính tổng số tiền cần thanh toán dựa trên số lượng và giá sản phẩm
    const totalAmount = cartItems.reduce((sum, item) => sum + (item.product?.price || 0) * (item.quantity || 1), 0);

    // Tính tổng giá trị gốc và tiền giảm giá
    const totalOriginalPrice = cartItems.reduce((sum, item) => {
        const oldPriceNumber = typeof item.product?.price === 'number' && item.product.price > 1000
            ? Math.round(item.product.price * 1.28)
            : (item.product?.price || 0);
        return sum + oldPriceNumber * (item.quantity || 1);
    }, 0);
    const totalDiscountAmount = totalOriginalPrice - totalAmount;

    const [errors, setErrors] = useState({});

    // Hàm validate kiểm tra tính hợp lệ của các trường thông tin giao hàng
    const validateForm = (name, phone, address) => {
        const newErrors = {};

        // Validate Full Name
        if (name === "") {
            newErrors.name = "Full name is required.";
        } else if (name.trim() === "") {
            newErrors.name = "Full name cannot be empty.";
        } else if (name.length < 2) {
            newErrors.name = "Full name must be at least 2 characters.";
        } else if (name.length > 100) {
            newErrors.name = "Full name must not exceed 100 characters.";
        } else if (!/^[\p{L}\s']+$/u.test(name)) {
            newErrors.name = "Full name can only contain letters.";
        }

        // Validate Phone Number
        if (phone === "") {
            newErrors.phone = "Phone number is required.";
        } else if (!/^\d+$/.test(phone)) {
            newErrors.phone = "Phone number must contain only digits.";
        } else if (!phone.startsWith("0")) {
            newErrors.phone = "Phone number must start with 0.";
        } else if (phone.length < 10 || phone.length > 11) {
            newErrors.phone = "Phone number must be between 10 and 11 digits.";
        }

        // Validate Shipping Address
        if (address === "") {
            newErrors.address = "Shipping address is required.";
        } else if (address.trim() === "") {
            newErrors.address = "Shipping address cannot be empty.";
        } else if (address.length < 10) {
            newErrors.address = "Shipping address must be at least 10 characters.";
        } else if (address.length > 255) {
            newErrors.address = "Shipping address must not exceed 255 characters.";
        }

        return newErrors;
    };

    // Hàm xử lý quá trình submit Đặt hàng
    const handleCheckout = async (e) => {
        e.preventDefault(); // Ngăn trình duyệt tải lại trang khi submit form

        const rawName = e.currentTarget.elements.namedItem("receiverName").value;
        const rawPhone = e.currentTarget.elements.namedItem("receiverPhone").value;
        const rawAddress = e.currentTarget.elements.namedItem("shippingAddress").value;

        const newErrors = validateForm(rawName, rawPhone, rawAddress);
        if (cartItems.length === 0) newErrors.general = "Your cart is empty.";
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }
        
        setErrors({});

        const name = rawName.trim();
        const phone = rawPhone.trim();
        const address = rawAddress.trim();

        setIsProcessing(true); // Bật trạng thái loading để vô hiệu hóa nút submit

        try {
            const order = await createOrder({
                receiverName: name,
                receiverPhone: phone,
                shippingAddress: address,
                items: cartItems.map(item => ({ productId: item.product.id, quantity: item.quantity || 1 })),
            });
            if (!location.state?.buyNowItem) clearCart();
            navigate("/payment-success", { state: { orderId: order.id }, replace: true });
        } catch (error) {
            setErrors({ general: error.message || "Order failed" });
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <div className="checkout-page-container container my-5">
            <h2 className="mb-4">Checkout</h2>

            {cartItems.length === 0 ? (
                <div className="text-center py-5 bg-light rounded-3">
                    <span className="fs-1 d-block mb-3" aria-hidden="true">🛒</span>
                    <h4>Your cart is empty</h4>
                    <p className="text-muted mb-4">You have no products to check out.</p>
                    <Link to="/products" className="btn btn-primary px-4 py-2">Continue Shopping</Link>
                </div>
            ) : (
                <form className="row g-4" onSubmit={handleCheckout} noValidate>
                    <div className="col-lg-7">
                        {errors.general && <div className="alert alert-danger" role="alert">{errors.general}</div>}

                        <section className="bg-white p-4 rounded-3 shadow-sm mb-4">
                            <h5 className="mb-4"><i className="bi bi-truck text-primary me-2" aria-hidden="true"></i>Shipping Information</h5>
                            <div className="row g-3">
                                <div className="col-md-6">
                                    <label htmlFor="receiverName" className="form-label fw-medium">Full Name</label>
                                    <input id="receiverName" type="text" name="receiverName" autoComplete="name" defaultValue={auth?.fullName || ''} placeholder="Enter your full name" className={`form-control form-control-lg ${errors.name ? 'is-invalid' : ''}`} onChange={() => setErrors(prev => ({...prev, name: ''}))} />
                                    {errors.name && <div className="invalid-feedback d-flex align-items-center gap-1"><i className="bi bi-exclamation-circle" aria-hidden="true"></i>{errors.name}</div>}
                                </div>
                                <div className="col-md-6">
                                    <label htmlFor="receiverPhone" className="form-label fw-medium">Phone Number</label>
                                    <input id="receiverPhone" type="tel" name="receiverPhone" inputMode="numeric" autoComplete="tel" placeholder="Enter phone number" className={`form-control form-control-lg ${errors.phone ? 'is-invalid' : ''}`} onChange={() => setErrors(prev => ({...prev, phone: ''}))} />
                                    {errors.phone && <div className="invalid-feedback d-flex align-items-center gap-1"><i className="bi bi-exclamation-circle" aria-hidden="true"></i>{errors.phone}</div>}
                                </div>
                                <div className="col-12">
                                    <label htmlFor="shippingAddress" className="form-label fw-medium">Shipping Address</label>
                                    <textarea id="shippingAddress" name="shippingAddress" rows={3} autoComplete="street-address" placeholder="Enter your full address" className={`form-control form-control-lg ${errors.address ? 'is-invalid' : ''}`} onChange={() => setErrors(prev => ({...prev, address: ''}))}></textarea>
                                    {errors.address && <div className="invalid-feedback d-flex align-items-center gap-1"><i className="bi bi-exclamation-circle" aria-hidden="true"></i>{errors.address}</div>}
                                </div>
                            </div>
                        </section>

                        <section className="bg-white p-4 rounded-3 shadow-sm">
                            <h5 className="mb-4"><i className="bi bi-cash-coin text-primary me-2" aria-hidden="true"></i>Payment Method</h5>
                            <div className="checkout-payment-option form-check">
                                <input id="paymentCod" className="form-check-input" type="radio" name="paymentMethod" defaultChecked disabled />
                                <label htmlFor="paymentCod" className="form-check-label fw-semibold">Cash on Delivery (COD)</label>
                                <p className="text-muted small mb-0">Pay when you receive the order</p>
                            </div>
                        </section>
                    </div>

                    <div className="col-lg-5">
                        <section className="checkout-summary-sticky bg-white p-4 rounded-3 shadow-sm">
                            <h5 className="mb-4"><i className="bi bi-bag-check text-primary me-2" aria-hidden="true"></i>Your Orders</h5>
                            <div className="checkout-summary-list">
                                {cartItems.map((item) => {
                                    const oldPriceNumber = typeof item.product?.price === 'number' && item.product.price > 1000
                                        ? Math.round(item.product.price * 1.28)
                                        : null;
                                    return (
                                        <div key={`${item.product.id}-${item.options?.color ?? ''}`} className="d-flex gap-3 py-3 border-bottom">
                                            <img className="checkout-order-image" src={getPrimaryImage(item.product)} alt={item.product?.name} loading="lazy" onError={handleImageError} />
                                            <div className="checkout-item-info flex-grow-1">
                                                <strong className="checkout-item-name">{item.product?.name}</strong>
                                                {item.options?.color && <small className="text-muted d-block">Color: {item.options.color}</small>}
                                                <small className="text-muted d-block">Qty: {item.quantity}</small>
                                                <div className="d-flex align-items-center flex-wrap gap-2 mt-1">
                                                    <strong className="text-primary">{formatPrice(item.product?.price)}</strong>
                                                    {oldPriceNumber && <><span className="text-muted text-decoration-line-through small">{formatPrice(oldPriceNumber)}</span><span className="badge bg-danger">-28%</span></>}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>

                            <div className="d-flex justify-content-between mt-3 mb-3 text-muted"><span>Original Value:</span><span>{formatPrice(totalOriginalPrice)}</span></div>
                            {totalDiscountAmount > 0 && <div className="d-flex justify-content-between mb-3 text-success"><span>Discount (Saved):</span><span>-{formatPrice(totalDiscountAmount)}</span></div>}
                            <div className="d-flex justify-content-between mb-3 text-muted"><span>Shipping:</span><span>Free</span></div>
                            <hr />
                            <div className="d-flex justify-content-between mb-4 fw-bold fs-5"><span>Total:</span><span className="text-primary">{formatPrice(totalAmount)}</span></div>
                            <button type="submit" className="btn btn-danger w-100 py-3 fw-bold rounded-3" disabled={isProcessing}>
                                {isProcessing ? <><span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Processing...</> : "PLACE ORDER"}
                            </button>
                            <div className="text-center mt-3"><Link to="/cart" className="text-decoration-none">← Back to Cart</Link></div>
                        </section>
                    </div>
                </form>
            )}
        </div>
    );
};

export default CheckoutPage;
