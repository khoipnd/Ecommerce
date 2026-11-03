import { useEffect, useState, useMemo } from "react";
import { Link } from "react-router-dom";

import {
    getCategories,
    deleteCategory,
} from "../../services/categoryService";

import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import Pagination from "../../components/common/Pagination";
import ConfirmModal from "../../components/common/ConfirmModal";

const ITEMS_PER_PAGE = 5;

const AdminCategoryPage = () => {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [currentPage, setCurrentPage] = useState(1);
    const [toast, setToast] = useState(null); // { type: "success" | "error", message }
    const [categoryToDelete, setCategoryToDelete] = useState(null);

    const fetchCategories = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await getCategories();
            setCategories(data);
        } catch (err) {
            setError(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const filteredCategories = useMemo(() => {
        if (!searchQuery.trim()) return categories;
        const q = searchQuery.toLowerCase();
        return categories.filter((c) => c?.name?.toLowerCase().includes(q));
    }, [categories, searchQuery]);

    const totalPages = Math.ceil(filteredCategories.length / ITEMS_PER_PAGE);

    const paginatedCategories = useMemo(() => {
        const start = (currentPage - 1) * ITEMS_PER_PAGE;
        return filteredCategories.slice(start, start + ITEMS_PER_PAGE);
    }, [filteredCategories, currentPage]);

    const handleDelete = (id) => {
        setCategoryToDelete(id);
    };

    const showToast = (type, message) => {
        setToast({ type, message });
        setTimeout(() => setToast(null), type === "error" ? 5000 : 3000);
    };

    const confirmDelete = async () => {
        if (!categoryToDelete) return;
        try {
            await deleteCategory(categoryToDelete);
            await fetchCategories();
            showToast("success", "Category deleted successfully!");
        } catch (err) {
            // Xoá lỗi (vd. 409 category còn sản phẩm) chỉ báo toast, giữ nguyên danh sách
            showToast("error", err.message || "Failed to delete category.");
        } finally {
            setCategoryToDelete(null);
        }
    };

    if (loading) {
        return (
            <div className="admin-page">
                <Loading message="Loading categories..." />
            </div>
        );
    }

    if (error) {
        return (
            <div className="admin-page">
                <ErrorMessage message="Failed to load categories." />
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-page-header admin-list-header">
                <div>
                    <p className="admin-page-label">SYSTEM MANAGEMENT / CATEGORIES</p>
                    <h1>Category Management</h1>
                    <p>Classify and organize product groups for MyStore.</p>
                </div>

                <Link to="/admin/categories/create" className="admin-primary-button">
                    + Add New Category
                </Link>
            </div>

            <ConfirmModal 
                isOpen={!!categoryToDelete} 
                title="Delete Category" 
                message="Are you sure you want to delete this category? This action cannot be undone." 
                onConfirm={confirmDelete} 
                onCancel={() => setCategoryToDelete(null)} 
            />

            {toast && (
                <div role={toast.type === 'error' ? 'alert' : 'status'} style={{
                    position: 'fixed',
                    top: '20px',
                    right: '20px',
                    maxWidth: '420px',
                    background: toast.type === 'error' ? '#ef4444' : '#22c55e',
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
                    <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24" style={{ flexShrink: 0 }}>
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5"
                            d={toast.type === 'error' ? "M6 18L18 6M6 6l12 12" : "M5 13l4 4L19 7"} />
                    </svg>
                    {toast.message}
                </div>
            )}

            <div className="admin-table-card">
                {/* Search Bar & Filter Controls */}
                <div className="admin-table-controls">
                    <div className="admin-search-wrap">
                        <input
                            type="text"
                            placeholder="Search category by name..."
                            value={searchQuery}
                            onChange={(e) => {
                                setSearchQuery(e.target.value);
                                setCurrentPage(1);
                            }}
                        />
                    </div>
                    <div className="admin-total-badge">
                        Total: <strong>{filteredCategories.length}</strong> categories
                    </div>
                </div>

                {filteredCategories.length === 0 ? (
                    <div className="admin-empty-state">
                        <h2>No categories found</h2>
                        <p>Try searching with a different keyword or create a new category.</p>
                        <Link to="/admin/categories/create" className="admin-primary-button">
                            + Add New Category
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="admin-table-wrapper">
                            <table className="admin-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Category Name</th>
                                        <th>Description / Type</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {paginatedCategories.map((category) => (
                                        <tr key={category.id}>
                                            <td>
                                                <span className="id-badge">#{category.id}</span>
                                            </td>

                                            <td>
                                                <div className="table-product">
                                                    <div className="table-product-icon">
                                                        📁
                                                    </div>
                                                    <div>
                                                        <strong className="product-table-name">
                                                            {category.name}
                                                        </strong>
                                                    </div>
                                                </div>
                                            </td>

                                            <td>
                                                <span className="category-tag">Product Category</span>
                                            </td>

                                            <td>
                                                <div className="table-actions">
                                                    <Link
                                                        to={`/admin/categories/${category.id}/edit`}
                                                        className="admin-secondary-button"
                                                    >
                                                        Edit
                                                    </Link>

                                                    <button
                                                        type="button"
                                                        className="admin-danger-button"
                                                        onClick={() => handleDelete(category.id)}
                                                    >
                                                        Delete
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination FE-12 */}
                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            onPageChange={(page) => setCurrentPage(page)}
                            totalItems={filteredCategories.length}
                            pageSize={ITEMS_PER_PAGE}
                        />
                    </>
                )}
            </div>
        </div>
    );
};

export default AdminCategoryPage;