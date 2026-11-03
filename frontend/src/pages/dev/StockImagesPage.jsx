import { useEffect, useState } from "react";
import { STOCK_GROUPS } from "../../data/stockImages";
import { getProducts } from "../../services/productService";
import { getCategoryKey, getPrimaryImage, getPrimaryImageFile, getStockImages, handleImageError } from "../../utils/productImages";

const groupNames = {
    phone: "Phone",
    laptop: "Laptop",
    watch: "Smartwatch",
    audio: "Audio",
    accessory: "Accessories",
    electronics: "Electronics",
};

export default function StockImagesPage() {
    const [products, setProducts] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        getProducts()
            .then(items => { if (active) setProducts(items); })
            .catch(() => { if (active) setError("Không tải được sản phẩm từ API."); });
        return () => { active = false; };
    }, []);

    return (
        <main className="container py-4">
            <h1 className="mb-2">Stock image preview</h1>
            <p className="text-muted">Trang chỉ có trong môi trường development. Ảnh local được dùng khi production build.</p>

            {Object.entries(STOCK_GROUPS).map(([group, photos]) => {
                const images = getStockImages({ categoryName: group });
                return (
                    <section key={group} className="mb-5">
                        <h2 className="h4 mb-3">{groupNames[group]} ({images.length})</h2>
                        <div className="row row-cols-2 row-cols-md-3 row-cols-xl-5 g-3">
                            {photos.map((photo, index) => {
                                const source = images[index] || photo.url;
                                const local = source !== photo.url;
                                return (
                                    <div className="col" key={photo.file}>
                                        <article className="card h-100 shadow-sm">
                                            <img className="card-img-top" src={source} alt={`${groupNames[group]} ${index + 1}`} onError={handleImageError} style={{ aspectRatio: "1", objectFit: "cover" }} />
                                            <div className="card-body p-2">
                                                <div className="fw-semibold small">{photo.file}</div>
                                                <span className={`badge ${local ? "text-bg-success" : "text-bg-warning"}`}>{local ? "local" : "remote"}</span>
                                                <div className="small text-muted mt-1">{photo.credit.split(" / ")[0]}</div>
                                                <a className="small" href={photo.credit.split(" / ")[1]} target="_blank" rel="noreferrer">Nguồn Unsplash</a>
                                            </div>
                                        </article>
                                    </div>
                                );
                            })}
                        </div>
                    </section>
                );
            })}

            <section>
                <h2 className="h4">Sản phẩm từ API ({products.length})</h2>
                {error && <p className="alert alert-warning">{error}</p>}
                <div className="table-responsive">
                    <table className="table table-striped align-middle">
                        <thead><tr><th>ID</th><th>Tên</th><th>Category</th><th>Nhóm</th><th>Ảnh chọn</th></tr></thead>
                        <tbody>
                            {products.map(product => {
                                const group = getCategoryKey(product);
                                return (
                                    <tr key={product.id}>
                                        <td>{product.id}</td>
                                        <td>{product.name}</td>
                                        <td>{product.category?.name || "—"}</td>
                                        <td>{group}</td>
                                        <td><img src={getPrimaryImage(product)} alt="" onError={handleImageError} width="56" height="56" style={{ objectFit: "cover" }} /> <span className="ms-2">{getPrimaryImageFile(product)}</span></td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    );
}
