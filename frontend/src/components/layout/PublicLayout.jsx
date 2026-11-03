import { Outlet } from "react-router-dom";

import Header from "./Header";
import Footer from "./Footer";

const PublicLayout = () => {
    return (
        <div>
            {/* Header chung hiển thị trên các trang public */}
            <Header />

            <main>
                {/* Outlet đóng vai trò là placeholder để render các trang con (routes) vào layout này */}
                <Outlet />
            </main>

            {/* Footer chung hiển thị trên các trang public */}
            <Footer />
        </div>
    );
};

export default PublicLayout;