import { useLayoutEffect } from "react";
import { useLocation, useNavigationType } from "react-router-dom";

// BrowserRouter không tự reset vị trí cuộn khi chuyển trang,
// nên trang mới sẽ giữ scroll của trang cũ (thường bị nhảy xuống cuối).
const ScrollToTop = () => {
    const { pathname } = useLocation();
    const navigationType = useNavigationType();

    useLayoutEffect(() => {
        // Back/Forward (POP) thì để trình duyệt tự khôi phục vị trí cũ
        if (navigationType === "POP") return;
        window.scrollTo({ top: 0, left: 0, behavior: "instant" });
    }, [pathname, navigationType]);

    return null;
};

export default ScrollToTop;
