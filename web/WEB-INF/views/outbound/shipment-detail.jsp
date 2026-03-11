<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>

                <t:layout title="Chi tiết vận chuyển: ${shipment.shipmentNumber}">
                    <style>
                        .premium-card {
                            border: none;
                            border-radius: 15px;
                            transition: transform 0.2s, box-shadow 0.2s;
                        }

                        .premium-card:hover {
                            box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
                        }

                        .info-label {
                            font-size: 0.85rem;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                            color: #858796;
                            margin-bottom: 2px;
                        }

                        .info-value {
                            font-weight: 700;
                            color: #2e3b4e;
                        }

                        .status-badge-lg {
                            font-size: 0.9rem;
                            padding: 8px 16px;
                            border-radius: 50px;
                            font-weight: 700;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
                        }

                        /* Premium Timeline */
                        .premium-timeline {
                            position: relative;
                            padding-left: 3rem;
                            list-style: none;
                        }

                        .premium-timeline:before {
                            content: "";
                            position: absolute;
                            left: 1.2rem;
                            top: 0;
                            bottom: 0;
                            width: 3px;
                            background: #e3e6f0;
                            border-radius: 10px;
                        }

                        .timeline-item {
                            position: relative;
                            padding-bottom: 2rem;
                        }

                        .timeline-marker {
                            position: absolute;
                            left: -2.35rem;
                            width: 1.5rem;
                            height: 1.5rem;
                            border-radius: 50%;
                            background: #fff;
                            border: 3px solid #e3e6f0;
                            z-index: 10;
                        }

                        .timeline-item.active .timeline-marker {
                            border-color: #4e73df;
                            background: #4e73df;
                            box-shadow: 0 0 0 4px rgba(78, 115, 223, 0.2);
                            animation: pulse-blue 2s infinite;
                        }

                        .timeline-item.completed .timeline-marker {
                            border-color: #1cc88a;
                            background: #1cc88a;
                        }

                        .timeline-content {
                            padding: 15px;
                            background: #f8f9fc;
                            border-radius: 12px;
                            border-left: 4px solid #e3e6f0;
                        }

                        .timeline-item.completed .timeline-content {
                            border-left-color: #1cc88a;
                        }

                        .timeline-item.active .timeline-content {
                            border-left-color: #4e73df;
                            background: #fff;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                        }

                        @keyframes pulse-blue {
                            0% {
                                box-shadow: 0 0 0 0 rgba(78, 115, 223, 0.4);
                            }

                            70% {
                                box-shadow: 0 0 0 10px rgba(78, 115, 223, 0);
                            }

                            100% {
                                box-shadow: 0 0 0 0 rgba(78, 115, 223, 0);
                            }
                        }

                        .icon-box {
                            width: 40px;
                            height: 40px;
                            border-radius: 10px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            margin-right: 15px;
                        }
                    </style>

                    <div class="container-fluid py-4">
                        <!-- Page Header -->
                        <div class="d-sm-flex align-items-center justify-content-between mb-4">
                            <h1 class="h3 mb-0 text-gray-800 font-weight-bold">
                                <i class="fas fa-shipping-fast text-warning mr-2"></i> ${shipment.shipmentNumber}
                            </h1>
                            <div>
                                <a href="${pageContext.request.contextPath}/shipment?action=list"
                                    class="btn btn-sm btn-light border shadow-sm">
                                    <i class="fas fa-arrow-left fa-sm text-gray-500"></i> Trở về danh sách
                                </a>
                            </div>
                        </div>

                        <div class="row">
                            <!-- Left Info Column -->
                            <div class="col-lg-8">
                                <!-- Shipping Info Card -->
                                <div class="card premium-card shadow-sm mb-4">
                                    <div
                                        class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                                        <h6 class="m-0 font-weight-bold text-dark text-uppercase small">
                                            <i class="fas fa-info-circle text-primary mr-2"></i> Thông tin vận chuyển
                                        </h6>
                                        <c:choose>
                                            <c:when test="${shipment.status == 'CREATED'}"><span
                                                    class="badge badge-warning status-badge-lg">Mới tạo</span></c:when>
                                            <c:when test="${shipment.status == 'PICKED_UP'}"><span
                                                    class="badge badge-primary status-badge-lg">Đã lấy hàng</span>
                                            </c:when>
                                            <c:when test="${shipment.status == 'IN_TRANSIT'}"><span
                                                    class="badge badge-info status-badge-lg">Đang giao</span></c:when>
                                            <c:when test="${shipment.status == 'DELIVERED'}"><span
                                                    class="badge badge-success status-badge-lg">Đã giao</span></c:when>
                                            <c:when test="${shipment.status == 'CANCELLED'}"><span
                                                    class="badge badge-danger status-badge-lg">Đã hủy</span></c:when>
                                            <c:otherwise><span
                                                    class="badge badge-secondary status-badge-lg">${shipment.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="card-body">
                                        <div class="row g-4">
                                            <div class="col-md-6 mb-4">
                                                <div class="d-flex align-items-start">
                                                    <div class="icon-box bg-light-primary text-primary">
                                                        <i class="fas fa-fingerprint fa-lg text-primary"></i>
                                                    </div>
                                                    <div>
                                                        <div class="info-label text-muted">Mã vận chuyển</div>
                                                        <div class="info-value h5">${shipment.shipmentNumber}</div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-6 mb-4">
                                                <div class="d-flex align-items-start">
                                                    <div class="icon-box bg-light-info text-info">
                                                        <i class="fas fa-tag fa-lg text-info"></i>
                                                    </div>
                                                    <div>
                                                        <div class="info-label text-muted">Loại đơn hàng</div>
                                                        <div class="info-value">
                                                            <c:choose>
                                                                <c:when test="${shipment.shipmentType == 'CUSTOMER'}">
                                                                    Giao cho khách hàng</c:when>
                                                                <c:when test="${shipment.shipmentType == 'TRANSFER'}">
                                                                    Điều chuyển nội bộ</c:when>
                                                                <c:otherwise>${shipment.shipmentType}</c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-6 mb-4">
                                                <div class="d-flex align-items-start">
                                                    <div class="icon-box bg-light-warning text-warning">
                                                        <i class="fas fa-barcode fa-lg text-warning"></i>
                                                    </div>
                                                    <div>
                                                        <div class="info-label text-muted">Tracking Code</div>
                                                        <div class="info-value">
                                                            <span
                                                                class="text-monospace font-weight-bold px-2 py-1 bg-light border rounded">
                                                                ${empty shipment.trackingCode ? 'Chưa có thông tin' :
                                                                shipment.trackingCode}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-md-6 mb-4">
                                                <div class="d-flex align-items-start">
                                                    <div class="icon-box bg-light-success text-success">
                                                        <i class="fas fa-truck fa-lg text-success"></i>
                                                    </div>
                                                    <div>
                                                        <div class="info-label text-muted">Đơn vị vận chuyển</div>
                                                        <div class="info-value text-dark">${shipment.carrierName}</div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <div class="p-3 rounded bg-light border-left-warning">
                                                    <div class="info-label text-muted"><i class="fas fa-link mr-1"></i>
                                                        Linked GDN document</div>
                                                    <div class="info-value text-primary font-weight-bold">
                                                        ${shipment.gdnNumber}</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Internal Note -->
                                <div class="card premium-card shadow-sm mb-4">
                                    <div class="card-body">
                                        <div class="info-label text-muted mb-2"><i class="fas fa-sticky-note mr-1"></i>
                                            Ghi chú nội bộ</div>
                                        <p class="mb-0 text-dark italic">${empty shipment.note ? 'Không có ghi chú
                                            thêm.' : shipment.note}</p>
                                    </div>
                                </div>

                                <!-- Logistics Timeline -->
                                <div class="card premium-card shadow-sm mb-4 border-0">
                                    <div class="card-header bg-white py-3">
                                        <h6 class="m-0 font-weight-bold text-dark text-uppercase small">
                                            <i class="fas fa-history text-success mr-2"></i> Lộ trình vận chuyển
                                        </h6>
                                    </div>
                                    <div class="card-body">
                                        <div class="premium-timeline pr-3">
                                            <!-- Created -->
                                            <div class="timeline-item completed">
                                                <div class="timeline-marker"></div>
                                                <div class="timeline-content">
                                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                                        <span class="font-weight-bold text-success"><i
                                                                class="fas fa-check-circle mr-2"></i>Đã tạo đơn</span>
                                                    </div>
                                                    <div class="small text-muted time-ago mt-1 ml-0"
                                                        data-timestamp="${shipment.createdAt}">
                                                        ${shipment.createdAt}
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Picked Up -->
                                            <c:set var="isPickedUp" value="${not empty shipment.pickedUpAt}" />
                                            <c:set var="isDelivered" value="${not empty shipment.deliveredAt}" />

                                            <div
                                                class="timeline-item ${isPickedUp ? (isDelivered ? 'completed' : 'active') : ''}">
                                                <div class="timeline-marker"></div>
                                                <div class="timeline-content">
                                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                                        <span
                                                            class="font-weight-bold ${isPickedUp ? 'text-primary' : 'text-gray-500'}">
                                                            <i
                                                                class="fas ${isPickedUp ? 'fa-check-circle' : 'fa-hourglass-half'} mr-2"></i>Đã
                                                            lấy hàng
                                                        </span>
                                                    </div>
                                                    <div class="small text-muted ${isPickedUp ? 'time-ago' : ''} mt-1"
                                                        data-timestamp="${shipment.pickedUpAt}">
                                                        ${isPickedUp ? shipment.pickedUpAt : 'Chờ lấy hàng...'}
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Delivered -->
                                            <div class="timeline-item ${isDelivered ? 'active' : ''}">
                                                <div class="timeline-marker"></div>
                                                <div class="timeline-content border-bottom-0">
                                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                                        <span
                                                            class="font-weight-bold ${isDelivered ? 'text-success' : 'text-gray-500'}">
                                                            <i
                                                                class="fas ${isDelivered ? 'fa-check-circle' : 'fa-truck-loading'} mr-2"></i>Đã
                                                            giao hàng
                                                        </span>
                                                    </div>
                                                    <div class="small text-muted ${isDelivered ? 'time-ago' : ''} mt-1"
                                                        data-timestamp="${shipment.deliveredAt}">
                                                        ${isDelivered ? shipment.deliveredAt : 'Chờ giao hàng...'}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Right Sidebar Actions -->
                            <div class="col-lg-4">
                                <div class="card premium-card shadow-sm mb-4">
                                    <div class="card-header bg-white py-3">
                                        <h6 class="m-0 font-weight-bold text-dark text-uppercase small">Hành động nhanh
                                        </h6>
                                    </div>
                                    <div class="card-body">
                                        <c:if
                                            test="${shipment.status != 'DELIVERED' && shipment.status != 'CANCELLED'}">
                                            <a href="${pageContext.request.contextPath}/shipment?action=edit&id=${shipment.shipmentId}"
                                                class="btn btn-warning btn-block btn-lg shadow-sm border-0 mb-3 py-3">
                                                <i class="fas fa-edit mr-2"></i> <strong>Cập nhật trạng thái</strong>
                                            </a>
                                        </c:if>

                                        <button onclick="window.print()"
                                            class="btn btn-light btn-block btn-lg border shadow-sm mb-3 py-3">
                                            <i class="fas fa-print mr-2 text-info"></i> In mã vận đơn
                                        </button>

                                        <hr class="my-4">

                                        <div class="alert alert-secondary p-3 border-0 rounded-lg">
                                            <div class="small font-weight-bold text-uppercase mb-2">Hỗ trợ?</div>
                                            <div class="small">Nếu có bất kỳ vấn đề gì về đơn hàng này, vui lòng liên hệ
                                                bộ phận vận hành kho.</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </t:layout>