<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>
<t:layout title="Goods Delivery Note Details">
    <div class="container-fluid py-4">
        <c:if test="${not empty param.error || not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <c:choose>
                    <c:when test="${not empty param.error}">${fn:replace(fn:replace(param.error, '+', ' '), '%3A', ':')}</c:when>
                    <c:otherwise>${error}</c:otherwise>
                </c:choose>
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </div>
        </c:if>
        <!-- Breadcrumb / Back Link -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a
                            href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                            class="text-decoration-none text-muted">Goods Delivery Note</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${gdn.gdnNumber}</li>
                </ol>
            </nav>
            <span class="badge ${gdn.status == 'PENDING' ? 'bg-warning text-dark' : (gdn.status == 'CONFIRMED' ? 'bg-success' : 'bg-secondary')} px-3 py-2 rounded-pill shadow-sm">
                <i class="fas ${gdn.status == 'PENDING' ? 'fa-clock' : (gdn.status == 'CONFIRMED' ? 'fa-check-circle' : 'fa-file')} me-1"></i>
                ${gdn.status == 'CONFIRMED' ? 'Done' : gdn.status}
            </span>
        </div>

        <div class="row g-4">
            <!-- Header Info Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0"><i class="fas fa-info-circle me-2"></i>General Information</h5>
                    </div>
                    <div class="card-body">
                        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN Number</p>
                                <p class="mb-0 fw-semibold">${gdn.gdnNumber}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Sales Order</p>
                                <p class="mb-0 fw-semibold">${gdn.soNumber}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Customer</p>
                                <p class="mb-0 fw-semibold">${gdn.customerName}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created At</p>
                                <p class="mb-0 fw-semibold">${gdn.createdAtDisplay}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge ${gdn.status == 'PENDING' ? 'bg-warning' : (gdn.status == 'CONFIRMED' ? 'bg-success' : 'bg-secondary')} fw-semibold">
                                        ${gdn.status == 'CONFIRMED' ? 'Done' : gdn.status}
                                    </span>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created By</p>
                                <p class="mb-0 fw-semibold">${gdn.creatorName}</p>
                            </div>
                            <c:if test="${not empty gdn.confirmedAt}">
                                <div class="col">
                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Confirmed At</p>
                                    <p class="mb-0 fw-semibold">${gdn.confirmedAtDisplay}</p>
                                </div>
                            </c:if>
                            <div class="col-lg-12">
                                <hr class="my-2 opacity-10">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Ship To Address</p>
                                <p class="mb-0 text-secondary">${gdn.customerAddress != null && gdn.customerAddress != '' ? gdn.customerAddress : 'No address provided.'}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Items to Pick Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-dark text-white py-3">
                        <h5 class="card-title mb-0"><i class="fas fa-list me-2"></i>Items to Pick</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light text-secondary text-uppercase small">
                                    <tr class="text-center">
                                        <th class="text-start">Variant SKU</th>
                                        <th class="text-start">Product Name</th>
                                        <th>Color</th>
                                        <th>Size</th>
                                        <th>Qty Required</th>
                                        <th>Qty Picked</th>
                                        <th>Qty Packed</th>
                                        <th>Available</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="line" items="${gdn.lines != null ? gdn.lines : []}">
                                        <tr class="text-center">
                                            <td class="text-start fw-bold">${line.variantSku}</td>
                                            <td class="text-start">${line.productName}</td>
                                            <td>${line.color != null ? line.color : 'N/A'}</td>
                                            <td>${line.size != null ? line.size : 'N/A'}</td>
                                            <td><strong><fmt:formatNumber value="${line.qtyRequired != null ? line.qtyRequired : 0}" maxFractionDigits="0" /></strong></td>
                                            <td><span class="badge bg-info"><fmt:formatNumber value="${line.qtyPicked != null ? line.qtyPicked : 0}" maxFractionDigits="0" /></span></td>
                                            <td><span class="badge bg-warning text-dark"><fmt:formatNumber value="${line.qtyPacked != null ? line.qtyPacked : 0}" maxFractionDigits="0" /></span></td>
                                            <td><span class="badge bg-success"><fmt:formatNumber value="${line.qtyAvailable != null ? line.qtyAvailable : 0}" maxFractionDigits="0" /></span></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Buttons -->
        <div class="d-flex justify-content-between align-items-center mt-3 p-3 bg-white rounded shadow-sm border">
            <div>
                <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                    class="btn btn-outline-secondary me-2">
                    <i class="fas fa-arrow-left me-1"></i> Back to List
                </a>
            </div>

            <%-- PENDING: Edit (popup) + Create wave & start picking --%>
            <c:if test="${gdn.status == 'PENDING'}">
                <div class="d-flex gap-2 flex-wrap align-items-center">
                    <button type="button" class="btn btn-warning shadow-sm text-dark d-flex align-items-center" data-toggle="modal" data-target="#editGdnModal"
                        style="min-width: 100px; height: 38px;">
                        <i class="fas fa-edit me-2"></i>Edit
                    </button>
                    <form action="${pageContext.request.contextPath}/pick-wave" method="post" class="d-flex align-items-center mb-0">
                        <input type="hidden" name="action" value="create"/>
                        <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                        <button type="submit" class="btn btn-primary shadow-sm d-flex align-items-center justify-content-center"
                            style="min-width: 180px; height: 38px;">
                            <i class="fas fa-box-open me-2"></i>Create wave & Start picking
                        </button>
                    </form>
                </div>
            </c:if>

            <c:if test="${gdn.status == 'ONGOING' || gdn.status == 'DRAFT'}">
                <div class="d-flex gap-2 flex-wrap align-items-center">
                    <button type="button" class="btn btn-warning shadow-sm text-dark" data-toggle="modal" data-target="#editGdnModal">
                        <i class="fas fa-edit me-2"></i>Edit
                    </button>
                    <c:if test="${wave != null}">
                        <a href="${pageContext.request.contextPath}/pick-task?action=assign&waveId=${wave.waveId}"
                            class="btn btn-primary shadow-sm">
                            <i class="fas fa-user-check me-2"></i>Assign tasks
                        </a>
                    </c:if>
                    <c:if test="${wave == null && gdn.status == 'DRAFT'}">
                        <a href="${pageContext.request.contextPath}/pick-task?action=assign&gdnId=${gdn.gdnId}"
                            class="btn btn-primary shadow-sm d-flex align-items-center justify-content-center"
                            style="min-width: 165px; height: 38px; padding: 0 1rem;">
                            <i class="fas fa-user-check me-2"></i>Assign Pick Task
                        </a>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${gdn.status == 'CONFIRMED'}">
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/shipment?action=create&gdnId=${gdn.gdnId}&soNumber=${gdn.soNumber}"
                        class="btn btn-success shadow-sm">
                        <i class="fas fa-truck me-2"></i>Create Shipment
                    </a>
                </div>
            </c:if>
        </div>

        <!-- Edit GDN Modal (Status + Qty Picked / Qty Packed) -->
        <c:if test="${gdn.status == 'PENDING' || gdn.status == 'DRAFT' || gdn.status == 'ONGOING'}">
            <div class="modal fade" id="editGdnModal" tabindex="-1" aria-labelledby="editGdnModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-scrollable">
                    <div class="modal-content shadow">
                        <form action="${pageContext.request.contextPath}/goods-delivery-note" method="post">
                            <input type="hidden" name="action" value="update" />
                            <input type="hidden" name="gdnId" value="${gdn.gdnId}" />
                            <div class="modal-header py-3 bg-light border-0">
                                <h5 class="modal-title text-dark mb-0" id="editGdnModalLabel">
                                    <i class="fas fa-edit text-primary me-2"></i>Edit GDN
                                </h5>
                                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                    <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                            <div class="modal-body pt-0 pb-4">
                                <!-- One row: GDN Number, SO, Customer, Status -->
                                <div class="row align-items-end g-3 mb-4 p-3 rounded bg-light">
                                    <div class="col">
                                        <label class="text-muted small text-uppercase fw-bold mb-1">GDN Number</label>
                                        <p class="mb-0 fw-semibold text-dark">${gdn.gdnNumber}</p>
                                    </div>
                                    <div class="col">
                                        <label class="text-muted small text-uppercase fw-bold mb-1">Sales Order</label>
                                        <p class="mb-0 fw-semibold text-dark">${gdn.soNumber}</p>
                                    </div>
                                    <div class="col">
                                        <label class="text-muted small text-uppercase fw-bold mb-1">Customer</label>
                                        <p class="mb-0 fw-semibold text-dark">${gdn.customerName}</p>
                                    </div>
                                    <div class="col">
                                        <label class="text-muted small text-uppercase fw-bold mb-1">Status</label>
                                        <select name="status" class="form-control form-control-sm" required>
                                            <option value="PENDING" ${gdn.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                            <option value="DRAFT" ${gdn.status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                                            <option value="ONGOING" ${gdn.status == 'ONGOING' ? 'selected' : ''}>ONGOING</option>
                                            <option value="CONFIRMED" ${gdn.status == 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                                        </select>
                                    </div>
                                </div>
                                <!-- Items table -->
                                <p class="text-muted small text-uppercase fw-bold mb-2">Items – Qty Picked / Qty Packed</p>
                                <div class="table-responsive rounded border">
                                    <table class="table table-hover table-sm align-middle mb-0">
                                        <thead class="thead-light">
                                            <tr class="text-center">
                                                <th class="text-start py-2 px-3">Variant / Product</th>
                                                <th class="py-2 px-3">Qty Required</th>
                                                <th class="py-2 px-3">Qty Picked</th>
                                                <th class="py-2 px-3">Qty Packed</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="line" items="${gdn.lines != null ? gdn.lines : []}">
                                                <tr>
                                                    <td class="text-start py-2 px-3">
                                                        <span class="fw-bold">${line.variantSku}</span>
                                                        <br /><small class="text-muted">${line.productName}</small>
                                                    </td>
                                                    <td class="text-center align-middle py-2 px-3"><fmt:formatNumber value="${line.qtyRequired != null ? line.qtyRequired : 0}" maxFractionDigits="0" /></td>
                                                    <td class="text-center py-2 px-3">
                                                        <input type="hidden" name="lineIds" value="${line.gdnLineId}" />
                                                        <input type="number" name="qtyPicked" class="form-control form-control-sm text-center mx-auto" min="0" step="1"
                                                            value="${line.qtyPicked != null ? line.qtyPicked.stripTrailingZeros().toPlainString() : '0'}" style="max-width: 80px;" />
                                                    </td>
                                                    <td class="text-center py-2 px-3">
                                                        <input type="number" name="qtyPacked" class="form-control form-control-sm text-center mx-auto" min="0" step="1"
                                                            value="${line.qtyPacked != null ? line.qtyPacked.stripTrailingZeros().toPlainString() : '0'}" style="max-width: 80px;" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="modal-footer border-0 bg-light py-3">
                                <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn btn-primary px-3"><i class="fas fa-save me-1"></i> Save</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:if>
    </div>
</t:layout>
