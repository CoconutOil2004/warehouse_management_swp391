<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
                <%@taglib uri="jakarta.tags.functions" prefix="fn" %>
                    <t:layout title="Goods Receipt Details">
                        <div class="container-fluid py-4">
                            <!-- Breadcrumb / Back Link -->
                            <div class="d-flex justify-content-between align-items-center mb-4">
                                <nav aria-label="breadcrumb">
                                    <ol class="breadcrumb mb-0">
                                        <li class="breadcrumb-item"><a
                                                href="${pageContext.request.contextPath}/goods-receipt?action=list"
                                                class="text-decoration-none text-muted">Goods Receipt</a></li>
                                        <li class="breadcrumb-item active" aria-current="page">${grn.grnNumber}</li>
                                    </ol>
                                </nav>
                                <span
                                    class="badge ${grn.status == 'PENDING' ? 'bg-warning text-dark' : (grn.status == 'APPROVED' ? 'bg-success' : 'bg-danger')} px-3 py-2 rounded-pill shadow-sm">
                                    <i
                                        class="fas ${grn.status == 'PENDING' ? 'fa-clock' : (grn.status == 'APPROVED' ? 'fa-check-circle' : 'fa-times-circle')} me-1"></i>
                                    ${grn.status}
                                </span>
                            </div>

                            <div class="row g-4">
                                <!-- Header Info Card -->
                                <div class="col-lg-12">
                                    <div class="card shadow-sm border-0">
                                        <div class="card-header bg-primary text-white py-3">
                                            <h5 class="card-title mb-0"><i class="fas fa-info-circle me-2"></i>General
                                                Information</h5>
                                        </div>
                                        <div class="card-body">
                                            <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">GRN Number
                                                    </p>
                                                    <p class="mb-0 fw-semibold">${grn.grnNumber}</p>
                                                </div>
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Purchase
                                                        Order
                                                    </p>
                                                    <p class="mb-0 fw-semibold">${grn.poNumber}</p>
                                                </div>
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Supplier</p>
                                                    <p class="mb-0 fw-semibold">${grn.supplierName}</p>
                                                </div>
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Created At
                                                    </p>
                                                    <p class="mb-0 fw-semibold">${grn.createdAt}</p>
                                                </div>
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Warehouse
                                                    </p>
                                                    <p class="mb-0 fw-semibold">${warehouseName}</p>
                                                </div>
                                                <div class="col">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                                    <p class="mb-0">
                                                        <span
                                                            class="badge ${grn.status == 'APPROVED' ? 'bg-success' : (grn.status == 'REJECTED' ? 'bg-danger' : 'bg-warning')} fw-semibold">
                                                            ${grn.status}
                                                        </span>
                                                    </p>
                                                </div>
                                                <c:if test="${not empty grn.approvedAt}">
                                                    <div class="col">
                                                        <p class="text-muted small mb-1 text-uppercase fw-bold">Approved
                                                            At</p>
                                                        <p class="mb-0 fw-semibold">${grn.approvedAt}</p>
                                                    </div>
                                                </c:if>
                                                <div class="col-lg-12">
                                                    <hr class="my-2 opacity-10">
                                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Internal
                                                        Note
                                                    </p>
                                                    <p class="mb-0 italic text-secondary">${grn.note != null && grn.note
                                                        !=
                                                        '' ? grn.note : 'No notes provided.'}</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- SKU Lines Card -->
                                <div class="col-lg-12">
                                    <div class="card shadow-sm border-0 mb-4">
                                        <div class="card-header bg-dark text-white py-3">
                                            <h5 class="card-title mb-0"><i class="fas fa-list me-2"></i>SKU Details</h5>
                                        </div>
                                        <div class="card-body p-0">
                                            <div class="table-responsive">
                                                <table class="table table-hover align-middle mb-0">
                                                    <thead class="table-light text-secondary text-uppercase small">
                                                        <tr class="text-center">
                                                            <th class="text-start">Product</th>
                                                            <th>Price</th>
                                                            <th>Quantity</th>
                                                            <th>Good</th>
                                                            <th>Damaged</th>
                                                            <th>Missing</th>
                                                            <th class="text-start">Note</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="l" items="${lines}">
                                                            <tr class="text-center">
                                                                <td class="text-start">
                                                                    <div class="fw-bold text-dark">${l.sku}</div>
                                                                    <div class="small text-muted">${l.productName}</div>
                                                                </td>
                                                                <td>
                                                                    <fmt:formatNumber value="${l.unitPrice}"
                                                                        pattern="#,##0.00" />
                                                                </td>
                                                                <td>
                                                                    <fmt:formatNumber value="${l.qtyExpected}"
                                                                        pattern="#,##0" />
                                                                </td>
                                                                <td><span
                                                                        class="badge bg-success-subtle text-success px-3">
                                                                        <fmt:formatNumber value="${l.qtyGood}"
                                                                            pattern="#,##0" />
                                                                    </span>
                                                                </td>
                                                                <td><span
                                                                        class="badge bg-danger-subtle text-danger px-3">
                                                                        <fmt:formatNumber value="${l.qtyDamaged}"
                                                                            pattern="#,##0" />
                                                                    </span>
                                                                </td>
                                                                <td><span
                                                                        class="badge bg-warning-subtle text-warning px-3">
                                                                        <fmt:formatNumber value="${l.qtyMissing}"
                                                                            pattern="#,##0" />
                                                                    </span>
                                                                </td>
                                                                <td class="text-start small text-muted">${l.note}</td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Putaway Information Card -->
                                <c:if test="${not empty putawayDetails}">
                                    <div class="col-lg-12 mt-4">
                                        <div class="card shadow-sm border-0">
                                            <div class="card-header bg-secondary text-white py-3">
                                                <h5 class="card-title mb-0"><i class="fas fa-warehouse me-2"></i>Storage
                                                    Locations</h5>
                                            </div>
                                            <div class="card-body p-0">
                                                <div class="table-responsive">
                                                    <table class="table table-hover align-middle mb-0">
                                                        <thead class="table-light text-secondary text-uppercase small">
                                                            <tr class="text-center">
                                                                <th class="text-start ps-4">Product</th>
                                                                <th>Type</th>
                                                                <th>Quantity</th>
                                                                <th>Slot</th>
                                                                <th>Zone</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach var="p" items="${putawayDetails}">
                                                                <tr class="text-center">
                                                                    <td class="text-start ps-4">
                                                                        <div class="fw-bold text-dark">${p.sku}</div>
                                                                        <div class="small text-muted">${p.productName}
                                                                        </div>
                                                                    </td>
                                                                    <td>
                                                                        <c:choose>
                                                                            <c:when test="${p.type == 'GOOD'}">
                                                                                <span
                                                                                    class="badge bg-success-subtle text-success border border-success-subtle px-2">GOOD</span>
                                                                            </c:when>
                                                                            <c:when test="${p.type == 'DAMAGED'}">
                                                                                <span
                                                                                    class="badge bg-danger-subtle text-danger border border-danger-subtle px-2">DAMAGED</span>
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <span
                                                                                    class="badge bg-secondary-subtle text-secondary border border-secondary-subtle px-2">${p.type}</span>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </td>
                                                                    <td>
                                                                        <fmt:formatNumber value="${p.qtyPutaway}"
                                                                            pattern="#,##0" />
                                                                    </td>
                                                                    <td>
                                                                        <span
                                                                            class="badge bg-info-subtle text-info px-3 border border-info-subtle">
                                                                            ${p.slotCode}
                                                                        </span>
                                                                    </td>
                                                                    <td>
                                                                        <span
                                                                            class="text-muted small">${p.zoneName}</span>
                                                                    </td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </div>

                            <!-- Action Buttons -->
                            <div
                                class="d-flex justify-content-between align-items-center mt-3 p-3 bg-white rounded shadow-sm border">
                                <div>
                                    <a href="${pageContext.request.contextPath}/goods-receipt?action=list"
                                        class="btn btn-outline-secondary me-2">
                                        <i class="fas fa-arrow-left me-1"></i> Back to List
                                    </a>
                                    <c:if test="${grn.status == 'PENDING' && not isPutawayComplete}">
                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">
                                            <i class="fas fa-exclamation-triangle me-1"></i>Please complete Putaway before Approving
                                        </span>
                                    </c:if>
                                </div>

                                <div class="d-flex gap-2">
                                    <c:choose>
                                        <c:when test="${grn.status == 'PENDING' || grn.status == 'DRAFT'}">

                                            <c:if test="${isManager}">
                                                <!-- Nút Approve -->
                                                <c:if test="${isPutawayComplete}">
                                                    <form action="${pageContext.request.contextPath}/goods-receipt"
                                                        method="post"
                                                        onsubmit="return confirm('Are you sure you want to APPROVE this receipt?')">
                                                        <input type="hidden" name="action" value="approve">
                                                        <input type="hidden" name="id" value="${grn.grnId}">
                                                        <button type="submit"
                                                            class="btn btn-success shadow-sm d-flex align-items-center justify-content-center"
                                                            style="height: 38px; padding: 0 16px;">
                                                            <i class="fas fa-check me-2"></i>Approve
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${not isPutawayComplete}">
                                                    <button type="button"
                                                        class="btn btn-success shadow-sm d-flex align-items-center justify-content-center disabled"
                                                        style="height: 38px; padding: 0 16px; opacity: 0.65; cursor: not-allowed;"
                                                        title="Putaway incomplete">
                                                        <i class="fas fa-lock me-2"></i>Approve
                                                    </button>
                                                </c:if>

                                                <!-- Nút Reject -->
                                                <form action="${pageContext.request.contextPath}/goods-receipt"
                                                    method="post"
                                                    onsubmit="return confirm('Are you sure you want to REJECT this receipt?')">
                                                    <input type="hidden" name="action" value="reject">
                                                    <input type="hidden" name="id" value="${grn.grnId}">
                                                    <button type="submit"
                                                        class="btn btn-outline-danger shadow-sm d-flex align-items-center justify-content-center"
                                                        style="height: 38px; padding: 0 16px;">
                                                        <i class="fas fa-times me-2"></i>Reject
                                                    </button>
                                                </form>

                                                <div class="vr mx-2"></div>
                                            </c:if>

                                            <c:if test="${canMutation}">
                                                <!-- Nút Putaway -->
                                                <a href="${pageContext.request.contextPath}/goods-receipt?action=putaway&id=${grn.grnId}"
                                                    class="btn btn-primary d-inline-flex align-items-center justify-content-center shadow-sm"
                                                    style="height: 38px; padding: 0 16px;">
                                                    <i class="fas fa-dolly-flatbed me-2"></i>Putaway
                                                </a>

                                                <!-- Nút Edit -->
                                                <a href="${pageContext.request.contextPath}/goods-receipt?action=edit&id=${grn.grnId}"
                                                    class="btn btn-outline-primary shadow-sm d-flex align-items-center justify-content-center"
                                                    style="height: 38px; padding: 0 16px;">
                                                    <i class="fas fa-edit me-2"></i>Edit
                                                </a>

                                                <!-- Nút Delete -->
                                                <form action="${pageContext.request.contextPath}/goods-receipt"
                                                    method="post"
                                                    onsubmit="return confirm('Delete this receipt? This action cannot be undone!')">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${grn.grnId}">
                                                    <button type="submit"
                                                        class="btn btn-danger shadow-sm d-flex align-items-center justify-content-center"
                                                        style="height: 38px; padding: 0 16px;">
                                                        <i class="fas fa-trash-alt me-2"></i>Delete
                                                    </button>
                                                </form>
                                            </c:if>

                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted fst-italic small align-self-center">
                                                <i class="fas fa-lock me-1"></i>
                                                Receipt processed (${grn.status}) — cannot be edited.
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                    </t:layout>