const fs = require('fs');
const path = require('path');
const filePath = path.join(__dirname, 'src', 'mocks', 'products.js');

let content = fs.readFileSync(filePath, 'utf8');

// Replace descriptions
content = content.replace(/description: ".*?",/g, 'description: "Experience premium quality and cutting-edge technology with this amazing product. Features include advanced specs, stunning design, and exceptional performance.",');

// Replace badges
content = content.replace(/badge: ".*?",/g, 'badge: "Top Rated",');

// Replace comments
content = content.replace(/ĐIỆN THOẠI/g, 'PHONES');
content = content.replace(/MÁY TÍNH XÁCH TAY/g, 'LAPTOPS');
content = content.replace(/ĐỒNG HỒ THÔNG MINH/g, 'SMARTWATCHES');
content = content.replace(/TAI NGHE & THIẾT BỊ ÂM THANH/g, 'AUDIO DEVICES');
content = content.replace(/PHỤ KIỆN/g, 'ACCESSORIES');

fs.writeFileSync(filePath, content, 'utf8');
console.log("Translation complete!");
