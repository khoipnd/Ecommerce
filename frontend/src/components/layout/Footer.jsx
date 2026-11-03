import { Container, Row, Col, Badge } from "react-bootstrap";

function Footer() {
    return (
        <footer className="footer">
            <Container>
                {/* Hàng chính của Footer */}
                <Row className="gx-5 gy-4">

                    {/* ===== Thông tin công ty ===== */}
                    <Col lg={3} md={6}>
                        {/* Logo + Badge Official */}
                        <div className="logo-box">
                            <h2 className="logo">MyStore</h2>

                            <Badge bg="primary" className="official">
                                Official
                            </Badge>
                        </div>

                        {/* Mô tả công ty */}
                        <p className="description">
                            Vietnam's leading authentic tech retailer.
                            Pioneering premium experiences for Laptops, Phones,
                            Tablets and High-end Audio.
                        </p>

                        {/* Hotline */}
                        <div className="contact">
                            <i className="bi bi-telephone-fill"></i>
                            <strong>1800 6933</strong>
                            <span> (Toll-free 8:00 - 21:30)</span>
                        </div>

                        {/* Địa điểm cửa hàng */}
                        <div className="contact">
                            <i className="bi bi-geo-alt"></i>
                            <span>45 stores nationwide</span>
                        </div>
                    </Col>

                    {/* ===== Chính sách ===== */}
                    <Col lg={3} md={6}>
                        <h5 className="footer-title">
                            POLICY INFORMATION
                        </h5>

                        <ul className="footer-menu">
                            <li>Comprehensive warranty policy</li>
                            <li>30-day 1-to-1 exchange policy</li>
                            <li>2-hour fast delivery policy</li>
                            <li>High-value trade-in program</li>
                            <li>Customer privacy policy</li>
                        </ul>
                    </Col>

                    {/* ===== Hỗ trợ khách hàng ===== */}
                    <Col lg={3} md={6}>
                        <h5 className="footer-title">
                            CUSTOMER SUPPORT
                        </h5>

                        <ul className="footer-menu">
                            <li>Shopping & payment guide</li>
                            <li>Track VAT e-invoice</li>
                            <li>Authorized Service Centers</li>
                            <li>Track order status</li>
                            <li>Feedback & complaints</li>
                        </ul>
                    </Col>

                    {/* ===== Payment Method ===== */}
                    <Col lg={3} md={6}>
                        <h5 className="footer-title">
                            PAYMENT METHODS
                        </h5>

                        {/* Danh sách phương thức thanh toán */}
                        <div className="payment-list">
                            <span>Cash on Delivery</span>
                        </div>

                        {/* Chứng nhận */}
                        <h5 className="footer-title mt-4">
                            CERTIFICATION
                        </h5>

                        <button className="cert-btn">
                            <i className="bi bi-patch-check-fill"></i>
                            Registered with MoIT
                        </button>
                    </Col>

                </Row>

                {/* Đường kẻ phân cách */}
                <hr />

                {/* Footer cuối cùng */}
                <Row className="footer-bottom align-items-center">

                    {/* Thông tin bản quyền */}
                    <Col md={6}>
                        © 2026 MyStore Tech JSC. Business License No.
                        0108998988 issued by DPI HCMC.
                    </Col>

                    {/* Thông tin phát triển hệ thống */}
                    <Col md={6} className="text-md-end">
                        Designed and operated by
                        <strong> MyStore Platform Engine.</strong>
                    </Col>

                </Row>

            </Container>
        </footer>
    );
}

export default Footer;
