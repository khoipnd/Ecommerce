import { useEffect, useState, useMemo } from "react";
import { Link } from "react-router-dom";

import {
    getProducts,
    deleteProduct,
} from "../../services/productService";

import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import Pagination from "../../components/common/Pagination";
import ConfirmModal from "../../components/common/ConfirmModal";
import { getPrimaryImage, handleImageError } from "../../utils/productImages";

const ITEMS_PER_PAGE = 5;

const AdminProductPage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [searchQuery, setSearchQuery] = useState("");
    const [currentPage, setCurrentPage] = useState(1);
    const [successToast, setSuccessToast] = useState("");
    const [productToDelete, setProductToDelete] = useState(null);

    const fetchProducts = async () => {
        try {
            setLoading(true);
            setError("");
            const data = await getProducts();
            setProducts(data);
        } catch (err) {
            setError(err.message || "Failed to load products.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts();
    }, []);

    const filteredProducts = useMemo(() => {
        if (!searchQuery.trim()) return products;
        const q = searchQuery.toLowerCase();
        return products.filter(
            (p) =>
                (p?.name?.toLowerCase().includes(q)) ||
                (p?.category?.name && p.category.name.toLowerCase().includes(q))
        );
    }, [products, searchQuery]);

    const totalPages = Math.ceil(filteredProducts.length / ITEMS_PER_PAGE);

    const paginatedProducts = useMemo(() => {
        const start = (currentPage - 1) * ITEMS_PER_PAGE;
        return filteredProducts.slice(start, start + ITEMS_PER_PAGE);
    }, [filteredProducts, currentPage]);

    const handleDelete = (id) => {
        setProductToDelete(id);
    };

    const confirmDelete = async () => {
        if (!productToDelete) return;
        try {
            setError("");
            await deleteProduct(productToDelete);
            setProducts((previous) => previous.filter((product) => product.id !== productToDelete));
            setSuccessToast("Product deleted successfully!");
            setTimeout(() => setSuccessToast(""), 3000);
        } catch (err) {
            setError(err.message || "Failed to delete product.");
        } finally {
            setProductToDelete(null);
        }
    };

    if (loading) {
        return (
            <div className="admin-page">
                <Loading message="Loading products data..." />
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-page-header">
                <div>
                    <p className="admin-page-label">SYSTEM MANAGEMENT / PRODUCTS</p>
                    <h1>Product Management</h1>
                    <p>Add new, update price, stock and categories for products.</p>
                </div>

                <Link to="/admin/products/create" className="admin-primary-button">
                    + Add New Product
                </Link>
            </div>

            <ConfirmModal 
                isOpen={!!productToDelete} 
                title="Delete Product" 
                message="Are you sure you want to delete this product? This action cannot be undone." 
                onConfirm={confirmDelete} 
                onCancel={() => setProductToDelete(null)} 
            />

            {successToast && (
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
                    {successToast}
                </div>
            )}

            {error && <ErrorMessage message={error} />}

            <div className="admin-table-card">
                {/* Search Bar & Filter Controls */}
                <div className="admin-table-controls">
                    <div className="admin-search-wrap">
                        <input
                            type="text"
                            placeholder="Search products by name or category..."
                            value={searchQuery}
                            onChange={(e) => {
                                setSearchQuery(e.target.value);
                                setCurrentPage(1);
                            }}
                        />
                    </div>
                    <div className="admin-total-badge">
                        Total: <strong>{filteredProducts.length}</strong> products
                    </div>
                </div>

                {filteredProducts.length === 0 ? (
                    <div className="admin-empty-state">
                        <h2>No products found</h2>
                        <p>Try searching with a different keyword or add a new product.</p>
                        <Link to="/admin/products/create" className="admin-primary-button">
                            + Add New Product
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="admin-table-wrapper">
                            <table className="admin-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Product</th>
                                        <th>Category</th>
                                        <th>Price</th>
                                        <th>Stock</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {paginatedProducts.map((product) => {
                                        const formattedPrice = typeof product.price === 'number'
                                            ? product.price > 1000
                                                ? `${product.price.toLocaleString('en-US')} VND`
                                                : `$${product.price}`
                                            : product.price;

                                        const isLowStock = (product.stockQuantity ?? 0) <= 5;

                                        return (
                                            <tr key={product.id}>
                                                <td>
                                                    <span className="id-badge">#{product.id}</span>
                                                </td>

                                                <td>
                                                    <div className="product-table-cell">
                                                        <div className="table-img-wrap">
                                                            <img
                                                                src={getPrimaryImage(product)}
                                                                alt={product.name}
                                                                loading="lazy"
                                                                onError={handleImageError}
                                                            />
                                                        </div>
                                                        <div>
                                                            <div className="product-table-name">{product.name}</div>
                                                            <div className="product-table-description">
                                                                {product.description || "No detailed description."}
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>

                                                <td>
                                                    <span className="category-tag">
                                                        {product.category?.name || "Other"}
                                                    </span>
                                                </td>

                                                <td>
                                                    <strong className="table-price">{formattedPrice}</strong>
                                                </td>

                                                <td>
                                                    <span className={`stock-count ${isLowStock ? "low" : ""}`}>
                                                        {product.stockQuantity ?? 0} units
                                                    </span>
                                                </td>

                                                <td>
                                                    {(product.stockQuantity ?? 0) > 0 ? (
                                                        <span className="status-pill active">Selling</span>
                                                    ) : (
                                                        <span className="status-pill out">Out of Stock</span>
                                                    )}
                                                </td>

                                                <td>
                                                    <div className="table-actions">
                                                        <Link
                                                            to={`/admin/products/${product.id}/edit`}
                                                            className="admin-secondary-button"
                                                        >
                                                            Edit
                                                        </Link>

                                                        <button
                                                            type="button"
                                                            className="admin-danger-button"
                                                            onClick={() => handleDelete(product.id)}
                                                        >
                                                            Delete
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination FE-12 */}
                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            onPageChange={(page) => setCurrentPage(page)}
                            totalItems={filteredProducts.length}
                            pageSize={ITEMS_PER_PAGE}
                        />
                    </>
                )}
            </div>
        </div>
    );
};

export default AdminProductPage;
