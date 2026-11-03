import React from "react";

function Pagination({
    currentPage = 1,
    totalPages = 1,
    onPageChange,
    totalItems = 0,
    pageSize = 12
}) {
    if (totalPages <= 1) return null;

    const getPageNumbers = () => {
        const pages = [];
        const maxVisible = 5;

        if (totalPages <= maxVisible) {
            for (let i = 1; i <= totalPages; i++) pages.push(i);
        } else {
            pages.push(1);
            if (currentPage > 3) pages.push("...");

            const start = Math.max(2, currentPage - 1);
            const end = Math.min(totalPages - 1, currentPage + 1);

            for (let i = start; i <= end; i++) {
                if (!pages.includes(i)) pages.push(i);
            }

            if (currentPage < totalPages - 2) pages.push("...");
            pages.push(totalPages);
        }
        return pages;
    };

    const startItem = totalItems > 0 ? (currentPage - 1) * pageSize + 1 : 0;
    const endItem = totalItems > 0 ? Math.min(currentPage * pageSize, totalItems) : 0;

    return (
        <div className="custom-pagination-wrapper">
            {totalItems > 0 && (
                <div className="pagination-info">
                    Showing <strong>{startItem} - {endItem}</strong> out of <strong>{totalItems}</strong> items
                </div>
            )}

            <div className="pagination-controls">
                {/* Prev button */}
                <button
                    className="pagination-btn prev-btn"
                    onClick={() => onPageChange && onPageChange(currentPage - 1)}
                    disabled={currentPage <= 1}
                >
                    ‹ Prev
                </button>

                {/* Page numbers */}
                <div className="page-numbers">
                    {getPageNumbers().map((page, idx) =>
                        page === "..." ? (
                            <span key={`dots-${idx}`} className="page-dots">...</span>
                        ) : (
                            <button
                                key={`page-${page}`}
                                className={`page-number-btn ${currentPage === page ? "active" : ""}`}
                                onClick={() => onPageChange && onPageChange(page)}
                            >
                                {page}
                            </button>
                        )
                    )}
                </div>

                {/* Next button */}
                <button
                    className="pagination-btn next-btn"
                    onClick={() => onPageChange && onPageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages}
                >
                    Next ›
                </button>
            </div>
        </div>
    );
}

export default Pagination;
