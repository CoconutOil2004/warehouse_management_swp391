<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<t:layout title="Packing – ${gdn.gdnNumber}">
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/packing?action=list">Packing</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}">${gdn.gdnNumber}</a></li>
                    <li class="breadcrumb-item active">Pack</li>
                </ol>
            </nav>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/packing?action=station&gdnId=${gdn.gdnId}" class="btn btn-warning">
                    <i class="fas fa-boxes me-1"></i> Packing Station
                </a>
                <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}" class="btn btn-outline-secondary">Back to GDN</a>
            </div>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-header bg-primary text-white py-2"><h5 class="mb-0">GDN ${gdn.gdnNumber} – ${gdn.soNumber}</h5></div>
            <div class="card-body">
                <div class="row row-cols-2 row-cols-md-4 g-2 small">
                    <div class="col"><strong>Customer:</strong> ${gdn.customerName}</div>
                    <div class="col"><strong>Status:</strong> <span class="badge bg-info">${gdn.status}</span></div>
                    <div class="col"><strong>Packing:</strong> <span class="badge ${packing.status == 'DONE' ? 'bg-success' : packing.status == 'IN_PROGRESS' ? 'bg-info' : 'bg-warning text-dark'}">${packing.status}</span></div>
                    <div class="col"><strong>Package:</strong> ${packing.packageType != null ? packing.packageType : '-'}</div>
                </div>
            </div>
        </div>

        <div class="card shadow-sm mb-4">
            <div class="card-header bg-dark text-white py-2 d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Lines (qty picked → pack)</h5>
                <a href="${pageContext.request.contextPath}/packing?action=station&gdnId=${gdn.gdnId}" class="btn btn-sm btn-light">
                    <i class="fas fa-external-link-alt me-1"></i> Open Station
                </a>
            </div>
            <div class="card-body p-0">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="text-center">
                            <th>Variant / Product</th>
                            <th>Qty required</th>
                            <th>Qty picked</th>
                            <th>Qty packed</th>
                            <th>Progress</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="line" items="${packingLines}">
                            <c:set var="progress" value="0"/>
                            <c:if test="${line.qtyPicked > 0}">
                                <c:set var="progress" value="${(line.qtyPacked / line.qtyPicked) * 100}"/>
                            </c:if>
                            <tr>
                                <td>
                                    <div class="fw-bold">${line.variantSku}</div>
                                    <small class="text-muted">${line.productName}</small>
                                </td>
                                <td class="text-center"><fmt:formatNumber value="${line.qtyRequired}" maxFractionDigits="0"/></td>
                                <td class="text-center"><fmt:formatNumber value="${line.qtyPicked}" maxFractionDigits="0"/></td>
                                <td class="text-center">
                                    <fmt:formatNumber value="${line.qtyPacked != null ? line.qtyPacked : 0}" maxFractionDigits="0"/>
                                </td>
                                <td style="width: 150px">
                                    <div class="progress" style="height: 20px">
                                        <div class="progress-bar ${progress >= 100 ? 'bg-success' : progress > 0 ? 'bg-warning' : 'bg-secondary'}" 
                                             role="progressbar" 
                                             style="width: ${progress}%" 
                                             aria-valuenow="${progress}" 
                                             aria-valuemin="0" 
                                             aria-valuemax="100">
                                            <c:if test="${progress > 20}">${progress}%</c:if>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="card shadow-sm">
            <div class="card-header bg-secondary text-white py-2"><h5 class="mb-0">Package Information</h5></div>
            <div class="card-body">
                <c:if test="${packing.status != 'DONE' && packing.status != 'CONFIRMED'}">
                    <form action="${pageContext.request.contextPath}/packing" method="post">
                        <input type="hidden" name="action" value="save"/>
                        <input type="hidden" name="packId" value="${packing.packId}"/>
                        <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                        
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Package label</label>
                                <input type="text" name="packageLabel" class="form-control" placeholder="e.g. PKG-001" value="${packing.packageLabel}"/>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Package Type</label>
                                <select name="packageType" class="form-select">
                                    <option value="">-- Select --</option>
                                    <option value="BOX" ${packing.packageType == 'BOX' ? 'selected' : ''}>Box</option>
                                    <option value="ENVELOPE" ${packing.packageType == 'ENVELOPE' ? 'selected' : ''}>Envelope</option>
                                    <option value="BAG" ${packing.packageType == 'BAG' ? 'selected' : ''}>Bag</option>
                                    <option value="PALLET" ${packing.packageType == 'PALLET' ? 'selected' : ''}>Pallet</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Weight</label>
                                <div class="input-group">
                                    <input type="number" name="weight" class="form-control" step="0.01" min="0" placeholder="0.00" value="${packing.weight}"/>
                                    <select name="weightUnit" class="form-select" style="max-width: 80px">
                                        <option value="kg" ${packing.weightUnit == 'kg' ? 'selected' : ''}>kg</option>
                                        <option value="lb" ${packing.weightUnit == 'lb' ? 'selected' : ''}>lb</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row mt-3">
                            <div class="col-12">
                                <label class="form-label">Notes</label>
                                <textarea name="notes" class="form-control" rows="2" placeholder="Special handling instructions...">${packing.notes}</textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-2 mt-3">
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save me-1"></i> Save Package Info
                            </button>
                            <a href="${pageContext.request.contextPath}/packing?action=station&gdnId=${gdn.gdnId}" class="btn btn-warning">
                                <i class="fas fa-boxes me-1"></i> Go to Packing Station
                            </a>
                        </div>
                    </form>
                </c:if>
                <c:if test="${packing.status == 'DONE' || packing.status == 'CONFIRMED'}">
                    <div class="alert alert-success mb-3">
                        <i class="fas fa-check-circle me-2"></i> Packing completed successfully!
                    </div>
                    <div class="row">
                        <div class="col-md-4">
                            <p class="mb-1 text-muted small">Label</p>
                            <p class="mb-0 fw-bold">${packing.packageLabel}</p>
                        </div>
                        <div class="col-md-4">
                            <p class="mb-1 text-muted small">Type / Weight</p>
                            <p class="mb-0">${packing.packageType} - ${packing.weight} ${packing.weightUnit}</p>
                        </div>
                        <div class="col-md-4">
                            <p class="mb-1 text-muted small">Packed At</p>
                            <p class="mb-0"><c:out value="${packing.packedAt}"/></p>
                        </div>
                    </div>
                    <c:if test="${not empty packing.notes}">
                        <div class="mt-3">
                            <p class="mb-1 text-muted small">Notes</p>
                            <p class="mb-0">${packing.notes}</p>
                        </div>
                    </c:if>
                    <div class="mt-3">
                        <a href="${pageContext.request.contextPath}/shipment?action=create&gdnId=${gdn.gdnId}&soNumber=${gdn.soNumber}" 
                           class="btn btn-success">
                            <i class="fas fa-truck me-1"></i> Create Shipment
                        </a>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</t:layout>
