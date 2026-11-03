import useCategories from "../../hooks/useCategories";

const CategoryList = () => {
    const {
        categories,
        loading,
        error,
    } = useCategories();

    if (loading) {
        return <div>Loading categories...</div>;
    }

    if (error) {
        return <div>Failed to load categories.</div>;
    }

    return (
        <div>
            <h2>Categories</h2>

            {categories.map((category) => (
                <div key={category.id}>
                    {category.name}
                </div>
            ))}
        </div>
    );
};

export default CategoryList;