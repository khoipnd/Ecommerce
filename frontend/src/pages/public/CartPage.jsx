import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../../contexts/CartContext";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";
import { formatPrice } from "../../utils/formatPrice";


const CartPage = () => {
    const { cartItems, removeFromCart, updateQuantity, cartTotal } = useCart();
    const navigate = useNavigate();
    const [itemToRemove, setItemToRemove] = useState(null);

    const handleConfirmRemove = () => {
        if (itemToRemove) {
            removeFromCart(itemToRemove.product.id, itemToRemove.options?.color);
            setItemToRemove(null);
        }
    };

    const totalOriginalPrice = cartItems.reduce((total, item) => {
        const finalDiscountPercent = item.product.discount || (item.product.id % 3 !== 0 && item.product.hasDiscount !== false ? 15 : 0);
        const originalPrice = finalDiscountPercent > 0 && typeof item.product.price === 'number' && item.product.price > 1000
            ? Math.round(item.product.price * (1 + finalDiscountPercent / 100))
            : item.product.price;
        return total + (originalPrice * item.quantity);
    }, 0);

    const totalDiscountAmount = totalOriginalPrice - cartTotal;

    return (
        <div className="cart-page-container container my-5">
            <h2 className="mb-4">Your Cart</h2>
            
            {cartItems.length === 0 ? (
                <div className="empty-cart-message text-center py-5 bg-light rounded">
                    <span className="fs-1 d-block mb-3">🛒</span>
                    <h4>Your cart is empty</h4>
                    <p className="text-muted mb-4">You have no products in your shopping cart.</p>
                    <Link to="/products" className="btn btn-primary px-4 py-2">
                        Continue Shopping
                    </Link>
                </div>
            ) : (
                <div className="row">
                    <div className="col-lg-8">
                        <div className="cart-items-wrapper bg-white p-4 rounded shadow-sm">
                            {cartItems.map((item) => {
                                const finalDiscountPercent = item.product.discount || (item.product.id % 3 !== 0 && item.product.hasDiscount !== false ? 15 : 0);
                                const oldPriceNumber = finalDiscountPercent > 0 && typeof item.product.price === 'number' && item.product.price > 1000
                                    ? Math.round(item.product.price * (1 + finalDiscountPercent / 100))
                                    : null;

                                return (
                                <div key={`${item.product.id}-${item.options?.color ?? ''}`} className="cart-item-row d-flex align-items-center py-3 border-bottom">
                                    <div className="cart-page-item-img me-4">
                                        <img 
                                            src={getPrimaryImage(item.product)} 
                                            alt={item.product.name} 
                                            className="cart-page-item-photo"
                                            loading="lazy"
                                            onError={handleImageError}
                                        />
                                    </div>
                                    <div className="cart-item-details flex-grow-1">
                                        <h5 className="mb-1">
                                            <Link to={`/products/${item.product.id}`} className="text-dark text-decoration-none">
                                                {item.product.name}
                                            </Link>
                                        </h5>
                                        {item.options?.color && (
                                            <span className="text-muted small">Color: {item.options.color}</span>
                                        )}
                                        <div className="d-flex align-items-center mt-2">
                                            <div className="text-primary fw-bold fs-5 me-2">
                                                {formatPrice(item.product.price)}
                                            </div>
                                            {oldPriceNumber && (
                                                <div className="text-muted text-decoration-line-through small">
                                                    {formatPrice(oldPriceNumber)}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div className="cart-item-actions d-flex flex-column align-items-end ms-3">
                                        <button 
                                            className="btn btn-link text-danger p-0 mb-3 text-decoration-none" 
                                            onClick={() => setItemToRemove(item)}
                                        >
                                            <i className="bi bi-trash"></i> Remove
                                        </button>
                                        <div className="modern-quantity-control">
                                            <button 
                                                type="button"
                                                className="qty-btn" 
                                                aria-label="Decrease quantity"
                                                disabled={item.quantity <= 1}
                                                onClick={() => updateQuantity(item.product.id, item.options?.color, item.quantity - 1)}
                                            >
                                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                                            </button>
                                            <span className="qty-val">{item.quantity}</span>
                                            <button 
                                                type="button"
                                                className="qty-btn" 
                                                aria-label="Increase quantity"
                                                onClick={() => updateQuantity(item.product.id, item.options?.color, item.quantity + 1)}
                                            >
                                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                );
                            })}
                        </div>
                    </div>
                    
                    <div className="col-lg-4 mt-4 mt-lg-0">
                        <div className="cart-summary-box bg-white p-4 rounded shadow-sm">
                            <h5 className="mb-4">Order Summary</h5>
                            
                            <div className="d-flex justify-content-between mb-3 text-muted">
                                <span>Original Value:</span>
                                <span>{formatPrice(totalOriginalPrice)}</span>
                            </div>
                            {totalDiscountAmount > 0 && (
                                <div className="d-flex justify-content-between mb-3 text-success">
                                    <span>Discount (Saved):</span>
                                    <span>-{formatPrice(totalDiscountAmount)}</span>
                                </div>
                            )}
                            <div className="d-flex justify-content-between mb-3 text-muted">
                                <span>Subtotal:</span>
                                <span>{formatPrice(cartTotal)}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-3 text-muted">
                                <span>Shipping:</span>
                                <span>Free</span>
                            </div>
                            
                            <hr />
                            
                            <div className="d-flex justify-content-between mb-4">
                                <span className="fw-bold fs-5">Total:</span>
                                <span className="fw-bold fs-5 text-primary">{formatPrice(cartTotal)}</span>
                            </div>
                            
                            <button 
                                className="btn btn-danger w-100 py-3 fw-bold fs-6 rounded-3" 
                                onClick={() => navigate("/checkout")}
                            >
                                PROCEED TO CHECKOUT
                            </button>
                            
                            <div className="mt-3 text-center">
                                <Link to="/products" className="text-decoration-none text-primary">
                                    <i className="bi bi-arrow-left me-1"></i> Continue Shopping
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Custom Confirm Modal */}
            <div className={`modal fade ${itemToRemove ? 'show d-block' : ''}`} tabIndex="-1" style={{ zIndex: 1055, backgroundColor: itemToRemove ? 'rgba(0,0,0,0.6)' : 'transparent', pointerEvents: itemToRemove ? 'auto' : 'none' }}>
                <div className="modal-dialog modal-dialog-centered">
                    <div className="modal-content border-0 shadow-lg rounded-4">
                        <div className="modal-header border-0 pb-0 mt-2">
                            <h5 className="modal-title fw-bold">Remove Item</h5>
                            <button type="button" className="btn-close" onClick={() => setItemToRemove(null)}></button>
                        </div>
                        <div className="modal-body py-4 fs-6">
                            Are you sure you want to remove <strong>{itemToRemove?.product.name}</strong> from your cart?
                        </div>
                        <div className="modal-footer border-0 pt-0 mb-2">
                            <button type="button" className="btn btn-light rounded-3 px-4 fw-medium" onClick={() => setItemToRemove(null)}>Cancel</button>
                            <button type="button" className="btn btn-danger rounded-3 px-4 fw-bold" onClick={handleConfirmRemove}>Yes, Remove</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
