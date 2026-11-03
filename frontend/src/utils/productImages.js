import PRODUCT_PLACEHOLDER from "../assets/product-placeholder.svg";
import { STOCK_GROUPS } from "../data/stockImages";

export { PRODUCT_PLACEHOLDER };

// Vite watches this directory and hashes each imported image for production.
const modules = import.meta.glob(
    "../assets/product_images/*.{[jJ][pP][gG],[jJ][pP][eE][gG],[pP][nN][gG],[wW][eE][bB][pP],[aA][vV][iI][fF]}",
    { eager: true, import: "default" }
);

const localImagesByProductId = {};
for (const [path, url] of Object.entries(modules)) {
    const fileName = path.split("/").pop();
    const match = /^(\d+)(?:-(\d+))?\.(jpe?g|png|webp|avif)$/i.exec(fileName);
    if (!match || (match[2] && Number(match[2]) < 2)) continue;

    const productId = match[1].replace(/^0+(?=\d)/, "");
    (localImagesByProductId[productId] ??= []).push({
        order: match[2] ? Number(match[2]) : 1,
        fileName,
        url,
    });
}
for (const images of Object.values(localImagesByProductId)) {
    images.sort((left, right) => left.order - right.order || left.fileName.localeCompare(right.fileName));
}

const stockModules = import.meta.glob("../assets/stock/*/*.{jpg,jpeg,png,webp}", { eager: true, import: "default" });
const localStockByGroup = {};
for (const [path, url] of Object.entries(stockModules)) {
    const [, group, file] = /\/stock\/([^/]+)\/([^/]+)$/.exec(path) ?? [];
    if (group && file) (localStockByGroup[group] ??= []).push({ file, url });
}
for (const photos of Object.values(localStockByGroup)) {
    photos.sort((left, right) => left.file.localeCompare(right.file, undefined, { numeric: true }));
}

const imageUrl = (image) => {
    const value = typeof image === "string" ? image : image?.url;
    return typeof value === "string" && /^(https?:\/\/|\/(?!\/)|data:image\/)/i.test(value.trim()) && !value.toLowerCase().includes("placeholder")
        ? value.trim()
        : null;
};

const localImagesFor = (product) => {
    const id = product?.id ?? product?.productId;
    return id == null ? [] : (localImagesByProductId[String(id)] ?? []);
};

const normalize = value => String(value ?? "").normalize("NFD").toLowerCase().replace(/[\u0300-\u036f]/g, "").replace(/đ/g, "d");
const contains = (text, words) => words.some(word => text.includes(word));

export const getCategoryKey = (product) => {
    const categoryName = normalize(product?.category?.name || product?.categoryName);
    let categoryKey = null;
    if (contains(categoryName, ["headphone", "audio", "tai nghe", "loa"])) categoryKey = "audio";
    else if (contains(categoryName, ["phone", "dien thoai"])) categoryKey = "phone";
    else if (contains(categoryName, ["laptop", "may tinh"])) categoryKey = "laptop";
    else if (contains(categoryName, ["watch", "dong ho"])) categoryKey = "watch";
    else if (contains(categoryName, ["accessor", "phu kien"])) categoryKey = "accessory";
    else if (categoryName.includes("electronic")) categoryKey = "electronics";
    if (categoryKey && categoryKey !== "electronics") return categoryKey;

    const name = normalize(product?.name);
    if (contains(name, ["keyboard", "ban phim", "mouse", "chuot", "hub"])) return "electronics";
    if (contains(name, ["charger", "adapter", "magsafe", "cable", "sac", "cap", "logitech mx"])) return "accessory";
    if (contains(name, ["airpods", "headphone", "earbuds", "buds", "jbl", "bose", "sony wh", "tai nghe", "loa"])) return "audio";
    if (contains(name, ["watch", "dong ho", "garmin venu"])) return "watch";
    if (contains(name, ["macbook", "laptop", "xps", "rog", "zephyrus", "may tinh"])) return "laptop";
    if (contains(name, ["iphone", "galaxy s", "pixel", "xiaomi", "dien thoai", "smartphone"])) return "phone";
    return "electronics";
};

export const getStockImages = product => {
    const group = getCategoryKey(product);
    const local = localStockByGroup[group] ?? [];
    if (local.length) return local.map(photo => photo.url);
    // Production must never request Unsplash because a stock group is missing.
    if (import.meta.env.PROD) return [];
    return (STOCK_GROUPS[group] ?? []).map(photo => photo.url);
};

export const getStockIndex = (product, length) => {
    if (!length) return 0;
    const id = String(product?.id ?? product?.productId ?? product?.name ?? "");
    const numericId = Number(id);
    if (id && Number.isFinite(numericId)) return ((numericId % length) + length) % length;
    let hash = 0;
    for (const character of id) hash = (hash * 31 + character.charCodeAt(0)) >>> 0;
    return hash % length;
};

export const getPrimaryImage = (product) => {
    const providedImage = imageUrl(product?.imageUrl) || imageUrl(product?.images?.[0]);
    if (providedImage) return providedImage;
    const ownImage = localImagesFor(product)[0]?.url;
    if (ownImage) return ownImage;
    const stock = getStockImages(product);
    return stock[getStockIndex(product, stock.length)] || PRODUCT_PLACEHOLDER;
};

export const getPrimaryImageFile = product => {
    if (imageUrl(product?.imageUrl) || imageUrl(product?.images?.[0])) return "API image";
    const ownImage = localImagesFor(product)[0];
    if (ownImage) return ownImage.fileName;
    const group = getCategoryKey(product);
    const stock = getStockImages(product);
    if (!stock.length) return "PRODUCT_PLACEHOLDER";
    const index = getStockIndex(product, stock.length);
    if (localStockByGroup[group]?.[index]) return localStockByGroup[group][index].file;
    return import.meta.env.PROD ? "PRODUCT_PLACEHOLDER" : (STOCK_GROUPS[group]?.[index]?.file || "PRODUCT_PLACEHOLDER");
};

export const getProductGallery = (product) => {
    const providedImage = imageUrl(product?.imageUrl) || imageUrl(product?.images?.[0]);
    const ownImages = localImagesFor(product);
    if (!providedImage && ownImages.length === 0) {
        const stock = getStockImages(product);
        const start = getStockIndex(product, stock.length);
        return stock.length
            ? Array.from({ length: Math.min(3, stock.length) }, (_, offset) => stock[(start + offset) % stock.length])
            : [PRODUCT_PLACEHOLDER];
    }
    const primary = providedImage || ownImages[0].url;
    const localExtras = localImagesFor(product).filter(image => image.order > 1).map(image => image.url);
    const providedImages = Array.isArray(product?.images) ? product.images.map(imageUrl).filter(Boolean) : [];
    return [...new Set([primary, ...localExtras, ...providedImages])].slice(0, 5);
};

export const handleImageError = (e) => {
    e.currentTarget.onerror = null;
    e.currentTarget.src = PRODUCT_PLACEHOLDER;
};

export const getProductSpecs = (product) => {
    const catKey = getCategoryKey(product);

    switch(catKey) {
        case 'phone':
            return [
                { label: "Màn hình", val: "OLED Super Retina XDR 120Hz ProMotion" },
                { label: "Vi xử lý", val: "Apple A18 Pro / Snapdragon 8 Gen 3 (3nm)" },
                { label: "Camera", val: "Cụm 3 ống kính 48MP Zoom quang học 5x" },
                { label: "Pin & Sạc", val: "Pin dung lượng lớn, Sạc nhanh 30W, MagSafe" },
                { label: "Độ bền", val: "Titanium Grade 5 / Kháng nước IP68" },
                { label: "Hệ điều hành", val: "iOS 18 / Android 14 thế hệ mới" }
            ];
        case 'laptop':
            return [
                { label: "Màn hình", val: '14" - 16" Liquid Retina XDR / OLED 240Hz' },
                { label: "Vi xử lý", val: "Apple M3 Pro / Intel Core i9-14900HX" },
                { label: "RAM & SSD", val: "18GB - 32GB RAM | 512GB - 1TB SSD NVMe" },
                { label: "Card đồ họa", val: "18-Core GPU / NVIDIA GeForce RTX 4080 12GB" },
                { label: "Chất liệu", val: "Vỏ nhôm nguyên khối Space Black / Hợp kim cao cấp" },
                { label: "Thời lượng pin", val: "Lên đến 18-22 giờ sử dụng liên tục" }
            ];
        case 'watch':
            return [
                { label: "Khung vỏ & Mặt kính", val: "49mm Titanium Grade 5 & Mặt kính Sapphire chống trầy" },
                { label: "Màn hình", val: "Always-On Retina OLED 3000 nits cực sáng" },
                { label: "Cảm biến sức khỏe", val: "Nhịp tim ECG, SpO2, Đo nhiệt độ, Phát hiện ngưng thở" },
                { label: "Chuẩn chống nước", val: "100m - Tiêu chuẩn lặn biển chuyên nghiệp EN13319" },
                { label: "Thời lượng pin", val: "36 giờ sử dụng liên tục / 72 giờ tiết kiệm pin" }
            ];
        case 'audio':
            return [
                { label: "Công nghệ âm thanh", val: "Chống ồn chủ động ANC tiên tiến, Spatial Audio 3D" },
                { label: "Vi xử lý", val: "Chip Apple H2 / Sony V1 HD Audio Processor" },
                { label: "Thời lượng pin", val: "30 - 40 giờ nghe nhạc liên tục khi bật ANC" },
                { label: "Cổng sạc & Kết nối", val: "USB-C sạc nhanh 5 phút nghe 3h, Bluetooth 5.3" }
            ];
        case 'electronics':
            return [
                { label: "Loại thiết bị", val: "Bàn phím cơ / Chuột không dây / Hub USB-C" },
                { label: "Kết nối", val: "USB-C / USB-A / Bluetooth tùy sản phẩm" },
                { label: "Tương thích", val: "Máy tính Windows, macOS và thiết bị hỗ trợ" },
            ];
        default:
            return [
                { label: "Chất liệu & Độ bền", val: "Hợp kim nhôm xước & Nhựa chống cháy cao cấp" },
                { label: "Chuẩn kết nối & Sạc", val: "Sạc nhanh Qi2 MagSafe / Power Delivery 3.0 140W" },
                { label: "Tính năng", val: "Độ phân giải 8000 DPI, Màn hình LED hiển thị pin" },
                { label: "Tương thích", val: "All các dòng iPhone, iPad, MacBook, Windows PC" }
            ];
    }
};
