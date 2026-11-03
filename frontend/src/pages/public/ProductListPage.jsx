import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import useProducts from "../../hooks/useProducts";
import useCategories from "../../hooks/useCategories";

import ProductGrid from "../../components/product/ProductGrid";
import Loading from "../../components/common/Loading";
import ErrorMessage from "../../components/common/ErrorMessage";
import Pagination from "../../components/common/Pagination";
import { getCategoryKey } from "../../utils/productImages";

const PRODUCTS_PER_PAGE = 8;

const ProductListPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const categoryParam = searchParams.get("category") || "";
    const searchParam = searchParams.get("search") || "";

    const { products, loading: productsLoading, error: productsError } = useProducts();
    const { categories, loading: categoriesLoading, error: categoriesError } = useCategories();

    const [searchTerm, setSearchTerm] = useState(searchParam);
    const [selectedCategoryCode, setSelectedCategoryCode] = useState(categoryParam);
    const [selectedPriceRange, setSelectedPriceRange] = useState("all");
    const [sortBy, setSortBy] = useState("featured");
    const [currentPage, setCurrentPage] = useState(1);

    useEffect(() => {
        setSearchTerm(searchParam);
        setSelectedCategoryCode(categoryParam);
    }, [searchParam, categoryParam]);

    const loading = productsLoading || categoriesLoading;
    const error = productsError || categoriesError;

    // Filter products
    const filteredProducts = useMemo(() => {
        let result = [...products];
        const normalizedSearch = searchTerm.trim().toLowerCase();

        // Search Filter
        if (normalizedSearch) {
            result = result.filter(
                p => p.name.toLowerCase().includes(normalizedSearch) ||
                     (p.description && p.description.toLowerCase().includes(normalizedSearch)) ||
                     (p.category?.name && p.category.name.toLowerCase().includes(normalizedSearch))
            );
        }

        // Category Filter
        if (selectedCategoryCode) {
            result = result.filter(p => {
                const code = selectedCategoryCode.toLowerCase();
                if (code === "tablet") return /tablet|ipad|máy tính bảng/i.test(`${p.category?.name || ""} ${p.name}`);
                const group = code === "headphone" ? "audio" : code === "accessories" ? "accessory" : code;
                if (["phone", "laptop", "watch", "audio", "accessory", "electronics"].includes(group)) return getCategoryKey(p) === group;
                return String(p.categoryId) === selectedCategoryCode;
            });
        }

        // Price Filter
        if (selectedPriceRange !== "all") {
            result = result.filter(p => {
                const price = p.price || 0;
                if (selectedPriceRange === "under-10") return price < 10000000;
                if (selectedPriceRange === "10-25") return price >= 10000000 && price <= 25000000;
                if (selectedPriceRange === "above-25") return price > 25000000;
                return true;
            });
        }

        // Sorting
        if (sortBy === "price-asc") {
            result.sort((a, b) => (a.price || 0) - (b.price || 0));
        } else if (sortBy === "price-desc") {
            result.sort((a, b) => (b.price || 0) - (a.price || 0));
        } else if (sortBy === "rating") {
            result.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        }

        return result;
    }, [products, searchTerm, selectedCategoryCode, selectedPriceRange, sortBy]);

    const totalPages = Math.ceil(filteredProducts.length / PRODUCTS_PER_PAGE);

    const paginatedProducts = useMemo(() => {
        const startIndex = (currentPage - 1) * PRODUCTS_PER_PAGE;
        return filteredProducts.slice(startIndex, startIndex + PRODUCTS_PER_PAGE);
    }, [filteredProducts, currentPage]);

    useEffect(() => {
        setCurrentPage(1);
    }, [searchTerm, selectedCategoryCode, selectedPriceRange, sortBy]);

    const handleCategoryClick = (code) => {
        setSelectedCategoryCode(code);
        if (code) {
            setSearchParams({ category: code });
        } else {
            setSearchParams({});
        }
    };

    const handleResetAll = () => {
        setSearchTerm("");
        setSelectedCategoryCode("");
        setSelectedPriceRange("all");
        setSortBy("featured");
        setSearchParams({});
    };

    if (loading) {
        return (
            <div className="page-container">
                <Loading message="Loading tech products list..." />
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <ErrorMessage message="Could not load products. Please check your network connection." />
            </div>
        );
    }

    let bannerImage = "/assets/devices_banner.png";
    let bannerTitle = "All Tech Products";
    const bannerSubtitle = "Explore authentic Apple, Samsung, MacBook & Accessories with exclusive offers";

    if (selectedCategoryCode === "phone") {
        bannerImage = "/assets/banner_phone.png";
        bannerTitle = "Phones & Smartphones";
    } else if (selectedCategoryCode === "laptop") {
        bannerImage = "/assets/banner_laptop.png";
        bannerTitle = "Laptops & Tablets";
    } else if (selectedCategoryCode === "tablet") {
        bannerImage = "/assets/banner_laptop.png";
        bannerTitle = "Tablets & iPads";
    } else if (selectedCategoryCode === "watch") {
        bannerImage = "/assets/banner_smartwatch.png";
        bannerTitle = "Smartwatches";
    } else if (selectedCategoryCode === "headphone" || selectedCategoryCode === "audio") {
        bannerImage = "/assets/banner_headphone.png";
        bannerTitle = "Headphones & Audio";
    } else if (selectedCategoryCode === "accessories") {
        bannerImage = "/assets/banner_accessories.png";
        bannerTitle = "Tech Accessories";
    }

    return (
        <div className="product-list-page">
            <div className="container">
                {/* Header Banner */}
                <div className="products-banner" style={{ 
                    backgroundImage: `linear-gradient(rgba(15, 23, 42, 0.7), rgba(15, 23, 42, 0.7)), url(${bannerImage})`,
                    borderRadius: '16px',
                    marginTop: '20px',
                    backgroundSize: 'cover',
                    backgroundPosition: 'center',
                    boxShadow: '0 4px 20px rgba(0, 0, 0, 0.08)'
                }}>
                    <div className="products-overlay"></div>
                    <div className="products-banner-content">
                        <span className="banner-badge">MYSTORE TECH CATALOG 2026</span>
                        <h1>{bannerTitle}</h1>
                        <p className="products-subtitle">
                            {bannerSubtitle}
                        </p>
                    </div>
                </div>
            </div>

            <div className="container main-content-wrap" style={{ marginTop: '30px' }}>
                {/* Quick Category Bar Chips */}
                <div className="catalog-category-bar">
                    <button
                        type="button"
                        className={`cat-bar-chip ${!selectedCategoryCode ? "active" : ""}`}
                        onClick={() => handleCategoryClick("")}
                    >
                        <span>🛍️ All ({products.length})</span>
                    </button>
                    <button
                        type="button"
                        className={`cat-bar-chip ${selectedCategoryCode === "phone" ? "active" : ""}`}
                        onClick={() => handleCategoryClick("phone")}
                    >
                        <span>📱 Phone</span>
                    </button>
                    <button
                        type="button"
                        className={`cat-bar-chip ${selectedCategoryCode === "laptop" ? "active" : ""}`}
                        onClick={() => handleCategoryClick("laptop")}
                    >
                        <span>💻 Laptop</span>
                    </button>
                    <button
                        type="button"
                        className={`cat-bar-chip ${selectedCategoryCode === "watch" ? "active" : ""}`}
                        onClick={() => handleCategoryClick("watch")}
                    >
                        <span>⌚ Smartwatch</span>
                    </button>
                    <button
                        type="button"
                        className={`cat-bar-chip ${selectedCategoryCode === "headphone" || selectedCategoryCode === "audio" ? "active" : ""}`}
                        onClick={() => handleCategoryClick("headphone")}
                    >
                        <span>🎧 Headphones</span>
                    </button>
                    <button
                        type="button"
                        className={`cat-bar-chip ${selectedCategoryCode === "accessories" ? "active" : ""}`}
                        onClick={() => handleCategoryClick("accessories")}
                    >
                        <span>⚡ Accessories</span>
                    </button>
                </div>

                {/* Filter & Search Bar */}
                <div className="product-filters-card">
                    {/* Search Input */}
                    <div className="filter-group search-group">
                        <label htmlFor="product-search">Search devices</label>
                        <div className="input-with-icon">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="11" cy="11" r="8"/>
                                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                            </svg>
                            <input
                                id="product-search"
                                type="text"
                                placeholder="Enter keywords (iPhone 16, MacBook, AirPods, Sony...)"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            {searchTerm && (
                                <button className="clear-input-btn" onClick={() => setSearchTerm("")}>✕</button>
                            )}
                        </div>
                    </div>

                    {/* Price Range Filter */}
                    <div className="filter-group price-group">
                        <label>Price Range</label>
                        <select 
                            value={selectedPriceRange} 
                            onChange={(e) => setSelectedPriceRange(e.target.value)}
                        >
                            <option value="all">All price ranges</option>
                            <option value="under-10">Under 10 million</option>
                            <option value="10-25">From 10 - 25 million</option>
                            <option value="above-25">Above 25 million</option>
                        </select>
                    </div>

                    {/* Sort Selector */}
                    <div className="filter-group sort-group">
                        <label>Sort by</label>
                        <select 
                            value={sortBy} 
                            onChange={(e) => setSortBy(e.target.value)}
                        >
                            <option value="featured">Featured</option>
                            <option value="price-asc">Price: Low to High</option>
                            <option value="price-desc">Price: High to Low</option>
                            <option value="rating">Highest Rating</option>
                        </select>
                    </div>
                </div>

                {/* Results Header & Grid */}
                <div className="products-result">
                    <div className="result-header">
                        <h2>Product List ({filteredProducts.length})</h2>
                        {(selectedCategoryCode || searchTerm || selectedPriceRange !== "all" || sortBy !== "featured") && (
                            <button className="clear-filter-btn" onClick={handleResetAll}>
                                Clear all filters ✕
                            </button>
                        )}
                    </div>

                    {filteredProducts.length === 0 ? (
                        <div className="empty-state">
                            <div className="empty-icon">🔍</div>
                            <h3>No matching products found</h3>
                            <p>Try searching with different keywords or clear filters to view the entire catalog.</p>
                            <button 
                                className="btn-reset-filters" 
                                onClick={handleResetAll}
                            >
                                Reset filters
                            </button>
                        </div>
                    ) : (
                        <>
                            <ProductGrid products={paginatedProducts} />

                            {/* Pagination */}
                            {totalPages > 1 && (
                                <Pagination
                                    currentPage={currentPage}
                                    totalPages={totalPages}
                                    onPageChange={(page) => setCurrentPage(page)}
                                    totalItems={filteredProducts.length}
                                    pageSize={PRODUCTS_PER_PAGE}
                                />
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductListPage;
