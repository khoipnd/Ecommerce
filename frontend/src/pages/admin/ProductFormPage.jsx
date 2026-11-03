import { useEffect, useState } from "react";
import {
    Link,
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    createProduct,
    getProductById,
    updateProduct,
} from "../../services/productService";

import useCategories from "../../hooks/useCategories";

import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";

const ProductFormPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const isEditMode = Boolean(id);

    const {
        categories,
        loading: categoriesLoading,
        error: categoriesError,
    } = useCategories();

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        price: "",
        stockQuantity: "",
        categoryId: "",
    });

    const [loading, setLoading] = useState(
        isEditMode
    );

    const [errors, setErrors] = useState({});
    const [globalError, setGlobalError] = useState("");
    const [saving, setSaving] = useState(false);
    const [successToast, setSuccessToast] = useState("");

    useEffect(() => {
        if (!isEditMode) {
            return;
        }

        const fetchProduct = async () => {
            try {
                setLoading(true);
                setGlobalError("");

                const product =
                    await getProductById(id);

                if (!product) {
                    setGlobalError(
                        "Product not found."
                    );
                    return;
                }

                setFormData({
                    name: product.name || "",
                    description:
                        product.description ||
                        "",
                    price: product.price ?? "",
                    stockQuantity:
                        product.stockQuantity ??
                        "",
                    categoryId:
                        product.categoryId ??
                        "",
                });
            } catch (err) {
                setGlobalError(
                    "Failed to load product."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchProduct();
    }, [id, isEditMode]);

    const handleChange = (event) => {
        const {
            name,
            value,
        } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
        setErrors((prev) => ({
            ...prev,
            [name]: "",
        }));
        setGlobalError("");
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = "Product name is required.";
        }

        if (!formData.description.trim()) {
            newErrors.description = "Description is required.";
        }

        const priceNum = Number(formData.price);
        if (
            formData.price === "" ||
            priceNum <= 0 ||
            !Number.isInteger(priceNum)
        ) {
            newErrors.price = "Price must be a valid VND amount (integer > 0).";
        }

        const stockNum = Number(formData.stockQuantity);
        if (
            formData.stockQuantity === "" ||
            stockNum < 0 ||
            !Number.isInteger(stockNum)
        ) {
            newErrors.stockQuantity = "Stock quantity cannot be negative.";
        }

        if (!formData.categoryId) {
            newErrors.categoryId = "Category is required.";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            setSaving(true);
            setGlobalError("");

            const selectedCategory =
                categories.find(
                    (category) =>
                        category.id ===
                        Number(
                            formData.categoryId
                        )
                );

            const productData = {
                name: formData.name.trim(),
                description:
                    formData.description.trim(),
                price: Number(formData.price),
                stockQuantity: Number(
                    formData.stockQuantity
                ),
                categoryId: selectedCategory.id,
            };

            if (isEditMode) {
                await updateProduct(id, productData);
                setSuccessToast("Product updated successfully!");
            } else {
                await createProduct(productData);
                setSuccessToast("Product created successfully!");
            }

            setTimeout(() => {
                navigate("/admin/products");
            }, 1500);
        } catch (err) {
            setGlobalError(
                "Failed to save product."
            );
        } finally {
            setSaving(false);
        }
    };

    if (
        loading ||
        categoriesLoading
    ) {
        return <Loading />;
    }

    if (categoriesError) {
        return (
            <ErrorMessage
                message="Failed to load categories."
            />
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-page-header">
                <div>
                    <p className="admin-page-label">
                        CATALOG / PRODUCTS
                    </p>

                    <h1>
                        {isEditMode
                            ? "Edit Product"
                            : "Add Product"}
                    </h1>

                    <p>
                        {isEditMode
                            ? "Update product information."
                            : "Create a new product."}
                    </p>
                </div>

                <Link
                    to="/admin/products"
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

            <div className="admin-form-card">
                <form
                    className="admin-form"
                    onSubmit={handleSubmit}
                >
                    {globalError && (
                        <div className="form-error">
                            {globalError}
                        </div>
                    )}

                    <div className="form-grid">
                        <div className="admin-form-group full-width">
                            <label htmlFor="name">
                                Product Name
                            </label>

                            <input
                                id="name"
                                name="name"
                                type="text"
                                placeholder="Enter product name"
                                value={
                                    formData.name
                                }
                                onChange={
                                    handleChange
                                }
                                className={errors.name ? "input-error" : ""}
                            />
                            {errors.name && <span className="field-error">{errors.name}</span>}
                        </div>

                        <div className="admin-form-group full-width">
                            <label htmlFor="description">
                                Description
                            </label>

                            <textarea
                                id="description"
                                name="description"
                                rows="5"
                                placeholder="Enter product description"
                                value={
                                    formData.description
                                }
                                onChange={
                                    handleChange
                                }
                                className={errors.description ? "input-error" : ""}
                            />
                            {errors.description && <span className="field-error">{errors.description}</span>}
                        </div>

                        <div className="admin-form-group">
                            <label htmlFor="price">
                                Price
                            </label>

                            <input
                                id="price"
                                name="price"
                                type="number"
                                min="0"
                                step="1000"
                                placeholder="e.g. 100000"
                                value={
                                    formData.price
                                }
                                onChange={
                                    handleChange
                                }
                                className={errors.price ? "input-error" : ""}
                            />
                            {errors.price && <span className="field-error">{errors.price}</span>}
                        </div>

                        <div className="admin-form-group">
                            <label htmlFor="stockQuantity">
                                Stock Quantity
                            </label>

                            <input
                                id="stockQuantity"
                                name="stockQuantity"
                                type="number"
                                min="0"
                                placeholder="0"
                                value={
                                    formData.stockQuantity
                                }
                                onChange={
                                    handleChange
                                }
                                className={errors.stockQuantity ? "input-error" : ""}
                            />
                            {errors.stockQuantity && <span className="field-error">{errors.stockQuantity}</span>}
                        </div>

                        <div className="admin-form-group full-width">
                            <label htmlFor="categoryId">
                                Category
                            </label>

                            <select
                                id="categoryId"
                                name="categoryId"
                                value={
                                    formData.categoryId
                                }
                                onChange={
                                    handleChange
                                }
                                className={errors.categoryId ? "input-error" : ""}
                            >
                                <option value="">
                                    Select category
                                </option>

                                {categories.map(
                                    (category) => (
                                        <option
                                            key={
                                                category.id
                                            }
                                            value={
                                                category.id
                                            }
                                        >
                                            {
                                                category.name
                                            }
                                        </option>
                                    )
                                )}
                            </select>
                            {errors.categoryId && <span className="field-error">{errors.categoryId}</span>}
                        </div>

                    </div>

                    <div className="form-actions">
                        <Link
                            to="/admin/products"
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
                                ? "Update Product"
                                : "Create Product"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ProductFormPage;
