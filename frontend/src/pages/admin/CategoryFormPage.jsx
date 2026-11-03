import { useEffect, useState } from "react";
import {
    Link,
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    createCategory,
    getCategoryById,
    updateCategory,
} from "../../services/categoryService";

import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";

const CategoryFormPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const isEditMode = Boolean(id);

    const [name, setName] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] =
        useState(isEditMode);
    const [saving, setSaving] = useState(false);
    const [successToast, setSuccessToast] = useState("");

    useEffect(() => {
        if (!isEditMode) {
            return;
        }

        const fetchCategory = async () => {
            try {
                setLoading(true);
                setError("");

                const category =
                    await getCategoryById(id);

                if (!category) {
                    setError(
                        "Category not found."
                    );
                    return;
                }

                setName(category.name || "");
            } catch (err) {
                setError(
                    "Failed to load category."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchCategory();
    }, [id, isEditMode]);

    const handleSubmit = async (event) => {
        event.preventDefault();

        const trimmedName = name.trim();
        if (!trimmedName) {
            setError(
                "Category name is required."
            );
            return;
        }

        if (trimmedName.length < 2) {
            setError(
                "Category name must be at least 2 characters long."
            );
            return;
        }

        try {
            setSaving(true);
            setError("");

            const categoryData = {
                name: name.trim(),
            };

            if (isEditMode) {
                await updateCategory(id, categoryData);
                setSuccessToast("Category updated successfully!");
            } else {
                await createCategory(categoryData);
                setSuccessToast("Category created successfully!");
            }

            setTimeout(() => {
                navigate("/admin/categories");
            }, 1500);
        } catch (err) {
            setError(
                err.message ||
                "Failed to save category."
            );
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <Loading />;
    }

    return (
        <div className="admin-page">
            <div className="admin-page-header">
                <div>
                    <p className="admin-page-label">
                        CATALOG / CATEGORIES
                    </p>

                    <h1>
                        {isEditMode
                            ? "Edit Category"
                            : "Add Category"}
                    </h1>

                    <p>
                        {isEditMode
                            ? "Update category information."
                            : "Create a new category."}
                    </p>
                </div>

                <Link
                    to="/admin/categories"
                    className="admin-secondary-button"
                >
                    ← Back
                </Link>
            </div>

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

            <div className="admin-form-card small-form-card">
                <form
                    className="admin-form"
                    onSubmit={handleSubmit}
                >


                    <div className="admin-form-group">
                        <label htmlFor="category-name">
                            Category Name
                        </label>

                        <input
                            id="category-name"
                            type="text"
                            placeholder="Enter category name"
                            value={name}
                            onChange={(event) => {
                                setName(
                                    event.target
                                        .value
                                );
                                setError("");
                            }}
                            className={
                                error
                                    ? "input-error"
                                    : ""
                            }
                        />

                        {error && (
                            <span className="field-error">
                                {error}
                            </span>
                        )}
                    </div>

                    <div className="form-actions">
                        <Link
                            to="/admin/categories"
                            className="admin-secondary-button"
                        >
                            Cancel
                        </Link>

                        <button
                            type="submit"
                            className="admin-primary-button"
                            disabled={saving}
                        >
                            {saving
                                ? "Saving..."
                                : isEditMode
                                ? "Update Category"
                                : "Create Category"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CategoryFormPage;