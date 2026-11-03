import { useEffect, useState } from "react";
import { getProducts } from "../services/productService";

export default function useProducts() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    useEffect(() => {
        let active = true;
        getProducts().then(data => { if (active) setProducts(data); })
            .catch(err => { if (active) setError(err); })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, []);
    return { products, loading, error };
}
