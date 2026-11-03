import { useEffect, useState } from "react";
import { getProducts } from "../services/productService";

// The backend exposes GET /api/v1/products only; filtering happens on the client.
export default function useFilteredProducts(params) {
    const key = JSON.stringify(params);
    const [result, setResult] = useState({ key: null, data: [], error: null });
    useEffect(() => {
        let active = true;
        getProducts().then(products => {
            if (!active) return;
            const filters = JSON.parse(key);
            const search = String(filters.search || "").toLowerCase();
            const filtered = products.filter(product =>
                (!search || product.name.toLowerCase().includes(search)) &&
                (!filters.categoryId || product.categoryId === Number(filters.categoryId))
            );
            setResult({ key, data: filtered, error: null });
        }).catch(error => { if (active) setResult({ key, data: [], error }); });
        return () => { active = false; };
    }, [key]);
    const current = result.key === key;
    return {
        products: current ? result.data : [],
        totalElements: current ? result.data.length : 0,
        totalPages: current ? 1 : 0,
        loading: !current,
        error: current ? result.error : null,
    };
}
