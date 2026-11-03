import React from "react";
import { Offcanvas, Button } from "react-bootstrap";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../../contexts/CartContext";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";
import { formatPrice } from "../../utils/formatPrice";


const CartOffcanvas = () => {
    const { isCartOpen, toggleCart, cartItems, removeFromCart, updateQuantity, cartTotal } = useCart();
    const navigate = useNavigate();

    const handleCheckout = () => {
        toggleCart(false);
        navigate("/checkout");
    };

    return (
        <Offcanvas show={isCartOpen} onHide={() => toggleCart(false)} placement="end" className="cart-offcanvas">
            <Offcanvas.Header closeButton>
                <Offcanvas.Title>Cart của bạn</Offcanvas.Title>
            </Offcanvas.Header>
            <Offcanvas.Body className="d-flex flex-column">
                {cartItems.length === 0 ? (
                    <div className="empty-cart flex-grow-1 d-flex flex-column align-items-center justify-content-center">
                        <span className="empty-icon fs-1 mb-3">🛒</span>
                        <p>Cart đang trống</p>
                        <Button variant="primary" onClick={() => toggleCart(false)}>
                            Continue mua sắm
                        </Button>
                    </div>
                ) : (
                    <>
                        <div className="cart-items-list flex-grow-1 overflow-auto">
                            {cartItems.map((item) => (
                                <div key={`${item.product.id}-${item.options?.color ?? ''}`} className="cart-item d-flex mb-3 pb-3 border-bottom">
                                    <div className="cart-item-img me-3">
                                        <img 
                                            src={getPrimaryImage(item.product)} 
                                            alt={item.product.name} 
                                            className="cart-item-photo"
                                            loading="lazy"
                                            onError={handleImageError}
                                        />
                                    </div>
                                    <div className="cart-item-info flex-grow-1">
                                        <div className="d-flex justify-content-between align-items-start">
                                            <h6 className="mb-1 text-truncate cart-item-name">{item.product.name}</h6>
                                            <button 
                                                className="btn-close btn-sm" 
                                                onClick={() => removeFromCart(item.product.id, item.options?.color)}
                                                aria-label={`Remove ${item.product.name}`}
                                            ></button>
                                        </div>
                                        {item.options?.color && (
                                            <small className="text-muted d-block mb-1">Màu: {item.options.color}</small>
                                        )}
                                        <div className="d-flex justify-content-between align-items-center mt-2">
                                            <span className="fw-bold text-danger">{formatPrice(item.product.price)}</span>
                                            <div className="modern-quantity-control">
                                                <button 
                                                    type="button"
                                                    className="qty-btn"
                                                    aria-label="Decrease quantity"
                                                    disabled={item.quantity <= 1}
                                                    onClick={() => updateQuantity(item.product.id, item.options?.color, item.quantity - 1)}
                                                >-</button>
                                                <span className="qty-val">{item.quantity}</span>
                                                <button 
                                                    type="button"
                                                    className="qty-btn"
                                                    aria-label="Increase quantity"
                                                    onClick={() => updateQuantity(item.product.id, item.options?.color, item.quantity + 1)}
                                                >+</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <div className="cart-footer mt-3 pt-3 border-top">
                            <div className="d-flex justify-content-between mb-3">
                                <strong>Total:</strong>
                                <strong className="text-danger fs-5">{formatPrice(cartTotal)}</strong>
                            </div>
                            <Button variant="danger" className="w-100 py-2 fw-bold" onClick={handleCheckout}>
                                THANH TOÁN
                            </Button>
                        </div>
                    </>
                )}
            </Offcanvas.Body>
        </Offcanvas>
    );
};

export default CartOffcanvas;
