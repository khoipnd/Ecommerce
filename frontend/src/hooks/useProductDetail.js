import { useEffect, useState } from "react";
import { getProductById } from "../services/productService";

export default function useProductDetail(id) {
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    useEffect(() => {
        let active = true;
        getProductById(id).then(data => { if (active) setProduct(data); })
            .catch(err => { if (active) setError(err); })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, [id]);
    return { product, loading, error };
}
