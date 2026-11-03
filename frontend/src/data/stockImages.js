// Source photos are downloaded into src/assets/stock before building the app.
// The URLs below are only used by the fetch script and as a dev fallback.
const photo = (file, id, author, page) => ({
    file,
    url: `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=640&h=640&q=65&fm=jpg`,
    credit: `${author} / https://unsplash.com/photos/${page}`,
});

export const STOCK_GROUPS = {
    phone: [
        photo("phone-1.jpg", "1744251282729-52076dee36a7", "Mahdi Naserinejad", "idv5ymuDNXE"),
        photo("phone-2.jpg", "1664957968644-72602cbd9f55", "Jithuz Devan", "aUk18hKEqt4"),
        photo("phone-3.jpg", "1671899736981-d917010b8fb4", "David Švihovec", "7oldMLvqAbA"),
        photo("phone-4.jpg", "1696238510021-14ab17fa704f", "Omar Al-Ghosson", "OmJ7Nz2krgA"),
        photo("phone-5.jpg", "1737717926693-30bd9f17d128", "Nick Nice", "QoQVtMNS8-E"),
    ],
    laptop: [
        photo("laptop-1.jpg", "1578950435899-d1c1bf932ab2", "Paolo Conversano", "bqZw8GEppBM"),
        photo("laptop-2.jpg", "1496181133206-80ce9b88a853", "Kari Shea", "1SAnrIxw5OY"),
        photo("laptop-3.jpg", "1637329573832-5f6adfc3f637", "Anthony Choren", "7OQ08xQb2OU"),
        photo("laptop-4.jpg", "1693596936248-8fab255a3de1", "Darius Cotoi", "d_Vo4cKkiBY"),
        photo("laptop-5.jpg", "1507470828332-ab3b90a0a13f", "Raul Varzar", "3X1ypEYOBNA"),
    ],
    watch: [
        photo("watch-1.jpg", "1628427153638-9e35efe3e321", "Matúš Gocman", "A4Il7rpVC_U"),
        photo("watch-2.jpg", "1627794205161-b0398d3cf526", "Debagni Sarkhel", "7pUKnjb_7z4"),
        photo("watch-3.jpg", "1644776274208-7bbad46246f8", "Klim Musalimov", "uAFS9j5Updk"),
        photo("watch-4.jpg", "1626793167849-9d7c65d0932d", "Debagni Sarkhel", "mxj0xav4zts"),
        photo("watch-5.jpg", "1610820270236-3118408a09c2", "Harry Shelton", "TQ0k-WuVuY8"),
    ],
    audio: [
        photo("audio-1.jpg", "1505740420928-5e560c06d30e", "C D-X", "PDX_a_82obo"),
        photo("audio-2.jpg", "1613629758552-443027994609", "Birgith Roosipuu", "6jMXHpbpL0M"),
        photo("audio-3.jpg", "1742783637429-2452a4caee8c", "VCOM", "C0k_JxvcilU"),
        photo("audio-4.jpg", "1612858250434-b5358e2b3625", "YearOne", "xuioQR0qrHY"),
        photo("audio-5.jpg", "1674658556545-f18d4080ab6c", "Steven Erixon", "v_mf1iidtnk"),
    ],
    accessory: [
        photo("accessory-1.jpg", "1615526675221-e763c4ec84f1", "Studio Proper", "EfwdRR4OYAM"),
        photo("accessory-2.jpg", "1547479117-60c805aba44d", "Daniel Korpai", "ZNJ8uRBoCqE"),
        photo("accessory-3.jpg", "1540934238109-cef52a4b4b3b", "Michael Soledad", "XqQSWt9oods"),
        photo("accessory-4.jpg", "1660921436563-65ec990056e5", "Varad Murti", "mvFdCHhg2Nw"),
        photo("accessory-5.jpg", "1527864550417-7fd91fc51a46", "Oscar Ivan Esquivel Arteaga", "ZtxED1cpB1E"),
    ],
    electronics: [
        photo("electronics-1.jpg", "1684057770946-8c75c30e28e1", "bady abbas", "pgK2Khi85dU"),
        photo("electronics-2.jpg", "1632079003110-d694908500da", "JL Cabrera", "4GzqVNX0TCQ"),
        photo("electronics-3.jpg", "1760708551859-6559dc9640b8", "Bruce", "2BmIqyTdW-I"),
        photo("electronics-4.jpg", "1734126048491-a98cd0bef202", "Mihael Stojčević", "qPMncUPGceE"),
        photo("electronics-5.jpg", "1690439429860-4b47a25abab8", "nezt xs", "fkZ3QBimy8w"),
    ],
};
