import { lazy, Suspense } from "react";
import {
    BrowserRouter,
    Routes,
    Route,
} from "react-router-dom";

import Login from "../pages/login/Login";
import Register from "../pages/login/Register";



import PublicLayout from "../components/layout/PublicLayout";
import AdminLayout from "../components/layout/AdminLayout";
import ProtectedAdminRoute from "./ProtectedAdminRoute";
import ProtectedRoute from "./ProtectedRoute";
import ScrollToTop from "./ScrollToTop";

import HomePage from "../pages/public/HomePage";
import ProductListPage from "../pages/public/ProductListPage";
import ProductDetailPage from "../pages/public/ProductDetailPage";
import CartPage from "../pages/public/CartPage";

import CheckoutPage from "../pages/checkout/CheckoutPage";
import PaymentSuccess from "../pages/checkout/PaymentSuccess";

import OrdersPage from "../pages/public/OrdersPage";
import AdminOrderPage from "../pages/admin/AdminOrderPage";

import AdminLoginPage from "../pages/admin/AdminLoginPage";
import AdminDashboardPage from "../pages/admin/AdminDashboardPage";

import AdminProductPage from "../pages/admin/AdminProductPage";
import ProductFormPage from "../pages/admin/ProductFormPage";

import AdminCategoryPage from "../pages/admin/AdminCategoryPage";
import CategoryFormPage from "../pages/admin/CategoryFormPage";

import { CartProvider } from "../contexts/CartContext";

const StockImagesPage = import.meta.env.DEV
    ? lazy(() => import("../pages/dev/StockImagesPage"))
    : null;

const AppRoutes = () => {
   
    return (
        <CartProvider>
        <BrowserRouter>
            <ScrollToTop />
            <Routes>
                {StockImagesPage && (
                    <Route path="/dev/stock-images" element={<Suspense fallback={<p>Loading stock images…</p>}><StockImagesPage /></Suspense>} />
                )}
                {/* LOGIN */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />

                {/* PUBLIC */}
                <Route element={<PublicLayout />}>
                    <Route
                        path="/"
                        element={<HomePage />}
                    />

                    <Route
                        path="/products"
                        element={<ProductListPage />}
                    />

                    <Route
                        path="/products/:id"
                        element={<ProductDetailPage />}
                    />
                    
                    <Route
                        path="/cart"
                        element={<CartPage />}
                    />
                    
                    <Route
                        path="/checkout"
                        element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>}
                    />

                    <Route
                        path="/payment-success"
                        element={<ProtectedRoute><PaymentSuccess /></ProtectedRoute>}
                    />

                    <Route
                        path="/tracking"
                        element={<ProtectedRoute><OrdersPage /></ProtectedRoute>}
                    />
                    
                    <Route
                        path="/orders"
                        element={<ProtectedRoute><OrdersPage /></ProtectedRoute>}
                    />
                </Route>

                {/* ADMIN LOGIN */}
                <Route
                    path="/admin/login"
                    element={<AdminLoginPage />}
                />

                {/* PROTECTED ADMIN */}
                <Route element={<ProtectedAdminRoute />}>
                    <Route element={<AdminLayout />}>

                        {/* Dashboard */}
                        <Route
                            path="/admin"
                            element={<AdminDashboardPage />}
                        />

                        {/* Product */}
                        <Route
                            path="/admin/products"
                            element={<AdminProductPage />}
                        />

                        <Route
                            path="/admin/products/create"
                            element={<ProductFormPage />}
                        />

                        <Route
                            path="/admin/products/:id/edit"
                            element={<ProductFormPage />}
                        />

                        {/* Category */}
                        <Route
                            path="/admin/categories"
                            element={<AdminCategoryPage />}
                        />

                        <Route
                            path="/admin/categories/create"
                            element={<CategoryFormPage />}
                        />

                        <Route
                            path="/admin/categories/:id/edit"
                            element={<CategoryFormPage />}
                        />
                        <Route
                            path="/admin/orders"
                            element={<AdminOrderPage />}
                        />
                    </Route>
                </Route>
            </Routes>
        </BrowserRouter>
        </CartProvider>
    );
};


export default AppRoutes;
