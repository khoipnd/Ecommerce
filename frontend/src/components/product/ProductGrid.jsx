import ProductCard from "./ProductCard";

const ProductGrid = ({ products = [] }) => {
    return (
        <div className="modern-product-grid">
            {products.map((product) => (
                <ProductCard
                    key={product.id}
                    product={product}
                />
            ))}
        </div>
    );
};

export default ProductGrid;