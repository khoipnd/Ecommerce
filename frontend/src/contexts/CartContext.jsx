import { createContext, useContext, useState, useEffect } from "react";
import useAuth from "../hooks/useAuth";

const CartContext = createContext();

export const useCart = () => {
    return useContext(CartContext);
};

// Mỗi tài khoản có giỏ riêng; khách chưa đăng nhập dùng "cart_guest".
const GUEST_CART_KEY = "cart_guest";
const LEGACY_CART_KEY = "cart"; // key chung cũ, không gắn với tài khoản nào
const cartKeyFor = (userId) => (userId ? `cart_user_${userId}` : GUEST_CART_KEY);

const sameLine = (a, b) => a.product.id === b.product.id && a.options?.color === b.options?.color;

const readCart = (key) => {
    try {
        const parsed = JSON.parse(localStorage.getItem(key));
        return Array.isArray(parsed) ? parsed.filter(item => item && item.product && typeof item.product === 'object') : [];
    } catch (e) {
        return [];
    }
};

// Gộp giỏ khách vào giỏ tài khoản khi đăng nhập (cộng số lượng nếu trùng sản phẩm/màu)
const mergeCarts = (base, extra) => extra.reduce((items, line) => {
    const index = items.findIndex(item => sameLine(item, line));
    if (index === -1) return [...items, line];
    const merged = [...items];
    merged[index] = { ...merged[index], quantity: merged[index].quantity + line.quantity };
    return merged;
}, base);

const loadInitialCart = (key) => {
    const legacy = readCart(LEGACY_CART_KEY);
    localStorage.removeItem(LEGACY_CART_KEY);
    return mergeCarts(readCart(key), legacy);
};

export const CartProvider = ({ children }) => {
    const userId = useAuth()?.id;
    const ownerKey = cartKeyFor(userId);

    // Giữ key và items cùng một state để không ghi nhầm giỏ cũ sang key mới khi đổi tài khoản
    const [cart, setCart] = useState(() => ({ key: ownerKey, items: loadInitialCart(ownerKey) }));
    const cartItems = cart.items;
    const setCartItems = (update) => setCart(prev => ({
        ...prev,
        items: typeof update === "function" ? update(prev.items) : update,
    }));

    const [isCartOpen, setIsCartOpen] = useState(false);
    const [toastMessage, setToastMessage] = useState("");

    // Đăng nhập / đăng xuất: chuyển sang giỏ của người dùng hiện tại
    useEffect(() => {
        if (cart.key === ownerKey) return;
        let items = readCart(ownerKey);
        if (cart.key === GUEST_CART_KEY) {
            // Khách vừa đăng nhập: mang giỏ khách theo rồi xoá giỏ khách
            items = mergeCarts(items, cart.items);
            localStorage.removeItem(GUEST_CART_KEY);
        }
        setCart({ key: ownerKey, items });
        setIsCartOpen(false);
    }, [ownerKey, cart.key, cart.items]);

    useEffect(() => {
        localStorage.setItem(cart.key, JSON.stringify(cart.items));
    }, [cart]);

    const addToCart = (product, quantity = 1, options = {}) => {
        setCartItems((prevItems) => {
            const existingItemIndex = prevItems.findIndex(
                (item) => item.product.id === product.id && item.options?.color === options?.color
            );

            if (existingItemIndex > -1) {
                const newItems = [...prevItems];
                newItems[existingItemIndex] = {
                    ...newItems[existingItemIndex],
                    quantity: newItems[existingItemIndex].quantity + quantity
                };
                return newItems;
            } else {
                return [...prevItems, { product, quantity, options }];
            }
        });
        setToastMessage("Product added to cart!");
        setTimeout(() => setToastMessage(""), 3000);
    };

    const removeFromCart = (productId, colorOption) => {
        setCartItems((prevItems) => prevItems.filter((item) => !(item.product.id === productId && item.options?.color === colorOption)));
    };

    const updateQuantity = (productId, colorOption, quantity) => {
        if (quantity < 1) return;
        setCartItems((prevItems) => {
            return prevItems.map((item) => {
                if (item.product.id === productId && item.options?.color === colorOption) {
                    return { ...item, quantity };
                }
                return item;
            });
        });
    };

    const clearCart = () => {
        setCartItems([]);
    };
    
    const toggleCart = (state) => {
        if (state !== undefined) {
            setIsCartOpen(state);
        } else {
            setIsCartOpen((prev) => !prev);
        }
    };

    const cartTotal = cartItems.reduce((total, item) => total + ((item.product?.price || 0) * (item.quantity || 1)), 0);
    const cartCount = cartItems.reduce((count, item) => count + (item.quantity || 1), 0);

    return (
        <CartContext.Provider
            value={{
                cartItems,
                addToCart,
                removeFromCart,
                updateQuantity,
                clearCart,
                isCartOpen,
                toggleCart,
                cartTotal,
                cartCount
            }}
        >
            {toastMessage && (
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
                    gap: '12px',
                    animation: 'fadeIn 0.3s ease-out'
                }}>
                    <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                    </svg>
                    {toastMessage}
                </div>
            )}
            {children}
        </CartContext.Provider>
    );
};
