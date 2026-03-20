<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>
<t:layout title="Packing Station">
    <style>
        .packing-container {
            display: grid;
            grid-template-columns: 1fr 400px;
            gap: 1.5rem;
            height: calc(100vh - 180px);
        }
        .items-panel {
            overflow-y: auto;
        }
        .package-panel {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 1.5rem;
            height: fit-content;
            position: sticky;
            top: 0;
        }
        .item-card {
            background: white;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 0.75rem;
            border: 1px solid #e9ecef;
            transition: all 0.2s;
        }
        .item-card:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .item-card.packed {
            background: #d4edda;
            border-color: #28a745;
        }
        .item-card.partial {
            background: #fff3cd;
            border-color: #ffc107;
        }
        .qty-display {
            font-size: 1.5rem;
            font-weight: bold;
        }
        .progress-ring {
            width: 120px;
            height: 120px;
        }
        .progress-ring circle {
            fill: none;
            stroke-width: 8;
        }
        .progress-ring .bg {
            stroke: #e9ecef;
        }
        .progress-ring .progress {
            stroke: #28a745;
            stroke-linecap: round;
            transform: rotate(-90deg);
            transform-origin: center;
            transition: stroke-dashoffset 0.5s ease;
        }
        .package-type-btn {
            border: 2px solid #dee2e6;
            border-radius: 8px;
            padding: 0.75rem 1rem;
            cursor: pointer;
            transition: all 0.2s;
            background: white;
            text-align: center;
        }
        .package-type-btn:hover {
            border-color: #4e73df;
        }
        .package-type-btn.selected {
            border-color: #4e73df;
            background: #e8f0fe;
        }
        .package-type-btn i {
            font-size: 1.5rem;
            display: block;
            margin-bottom: 0.5rem;
        }
        .scanner-input {
            font-size: 1.25rem;
            padding: 0.75rem;
            border-radius: 8px;
        }
        @media (max-width: 992px) {
            .packing-container {
                grid-template-columns: 1fr;
            }
            .package-panel {
                position: static;
            }
        }
    </style>

    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/packing?action=list">Packing</a></li>
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}">${gdn.gdnNumber}</a></li>
                    <li class="breadcrumb-item active">Station</li>
                </ol>
            </nav>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/packing?action=ready" class="btn btn-outline-secondary">
                    <i class="fas fa-list me-1"></i> Back to Queue
                </a>
                <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}" class="btn btn-outline-secondary">
                    <i class="fas fa-arrow-left me-1"></i> Back to GDN
                </a>
            </div>
        </div>

        <div class="packing-container">
            <div class="items-panel">
                <div class="card shadow-sm mb-4">
                    <div class="card-header bg-primary text-white py-3">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h5 class="mb-0"><i class="fas fa-boxes me-2"></i>Items to Pack</h5>
                                <small>${gdn.gdnNumber} - ${gdn.customerName}</small>
                            </div>
                            <div class="text-end">
                                <c:set var="totalItems" value="0"/>
                                <c:set var="packedItems" value="0"/>
                                <c:forEach var="line" items="${packingLines}">
                                    <c:set var="totalItems" value="${totalItems + 1}"/>
                                    <c:if test="${(line.qtyPacked != null && line.qtyPacked >= line.qtyPicked) || (line.qtyPicked != null && line.qtyPicked == 0)}">
                                        <c:set var="packedItems" value="${packedItems + 1}"/>
                                    </c:if>
                                </c:forEach>
                                <span class="badge bg-light text-dark fs-6">${packedItems}/${totalItems} items</span>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr class="text-center">
                                    <th style="width: 40%">Product</th>
                                    <th>Required</th>
                                    <th>Picked</th>
                                    <th>Packed</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="line" items="${packingLines}">
                                    <c:set var="lineStatus" value="pending"/>
                                    <c:set var="lineClass" value=""/>
                                    <c:if test="${line.qtyPacked != null && line.qtyPacked >= line.qtyPicked && line.qtyPicked > 0}">
                                        <c:set var="lineStatus" value="packed"/>
                                        <c:set var="lineClass" value="packed"/>
                                    </c:if>
                                    <c:if test="${line.qtyPacked != null && line.qtyPacked > 0 && line.qtyPacked < line.qtyPicked}">
                                        <c:set var="lineStatus" value="partial"/>
                                        <c:set var="lineClass" value="partial"/>
                                    </c:if>
                                    <tr class="text-center item-card ${lineClass}">
                                        <td class="text-start">
                                            <div class="fw-bold">${line.variantSku}</div>
                                            <small class="text-muted">${line.productName}</small>
                                            <c:if test="${not empty line.color || not empty line.size}">
                                                <div class="small text-secondary">
                                                    <c:if test="${not empty line.color}">${line.color}</c:if>
                                                    <c:if test="${not empty line.size}"> / ${line.size}</c:if>
                                                </div>
                                            </c:if>
                                        </td>
                                        <td><fmt:formatNumber value="${line.qtyRequired}" maxFractionDigits="0"/></td>
                                        <td><fmt:formatNumber value="${line.qtyPicked}" maxFractionDigits="0"/></td>
                                        <td>
                                            <span class="qty-display <c:if test='${lineStatus == "packed"}'>text-success</c:if><c:if test='${lineStatus == "partial"}'>text-warning</c:if>">
                                                <fmt:formatNumber value="${line.qtyPacked != null ? line.qtyPacked : 0}" maxFractionDigits="0"/>
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${lineStatus == 'packed'}">
                                                <span class="badge bg-success"><i class="fas fa-check me-1"></i>Packed</span>
                                            </c:if>
                                            <c:if test="${lineStatus == 'partial'}">
                                                <span class="badge bg-warning text-dark"><i class="fas fa-clock me-1"></i>Partial</span>
                                            </c:if>
                                            <c:if test="${lineStatus == 'pending'}">
                                                <span class="badge bg-secondary"><i class="fas fa-hourglass-start me-1"></i>Pending</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:if test="${lineStatus != 'packed' && line.qtyPicked > 0}">
                                                <button type="button" class="btn btn-sm btn-success" 
                                                        onclick="showPackModal(${line.gdnLineId}, ${line.qtyPicked - (line.qtyPacked != null ? line.qtyPacked : 0)})">
                                                    <i class="fas fa-plus me-1"></i>Pack
                                                </button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <c:if test="${not empty packing && packing.status == 'DONE' && gdn.status == 'SHIPPING'}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle me-2"></i>
                        Packing completed for ${gdn.gdnNumber}. 
                        <a href="${pageContext.request.contextPath}/shipment?action=create&gdnId=${gdn.gdnId}&soNumber=${gdn.soNumber}" class="alert-link">
                            Create Shipment
                        </a>
                    </div>
                </c:if>
                
                <c:if test="${empty packing}">
                    <div class="alert alert-info mb-3">
                        <i class="fas fa-info-circle me-2"></i>
                        No packing record. <a href="${pageContext.request.contextPath}/packing?action=start&gdnId=${gdn.gdnId}">Start Packing</a>
                    </div>
                </c:if>
            </div>

            <div class="package-panel">
                <h5 class="mb-3"><i class="fas fa-box me-2"></i>Package Info</h5>
                
                <c:if test="${not empty packing && packing.status != 'DONE'}">
                    <form action="${pageContext.request.contextPath}/packing" method="post">
                        <input type="hidden" name="action" value="saveStation"/>
                        <input type="hidden" name="packId" value="${packing.packId}"/>
                        <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Package Label</label>
                            <input type="text" name="packageLabel" class="form-control" 
                                   placeholder="e.g. PKG-${gdn.gdnNumber}-1" value="${packing.packageLabel}"/>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Package Type</label>
                            <div class="row g-2">
                                <div class="col-6">
                                    <div class="package-type-btn ${packing.packageType == 'BOX' ? 'selected' : ''}" 
                                         onclick="selectPackageType('BOX')">
                                        <i class="fas fa-box-open"></i>
                                        Box
                                    </div>
                                    <input type="radio" name="packageType" value="BOX" 
                                           class="d-none" ${packing.packageType == 'BOX' ? 'checked' : ''}/>
                                </div>
                                <div class="col-6">
                                    <div class="package-type-btn ${packing.packageType == 'ENVELOPE' ? 'selected' : ''}" 
                                         onclick="selectPackageType('ENVELOPE')">
                                        <i class="fas fa-envelope-open-text"></i>
                                        Envelope
                                    </div>
                                    <input type="radio" name="packageType" value="ENVELOPE" 
                                           class="d-none" ${packing.packageType == 'ENVELOPE' ? 'checked' : ''}/>
                                </div>
                                <div class="col-6">
                                    <div class="package-type-btn ${packing.packageType == 'BAG' ? 'selected' : ''}" 
                                         onclick="selectPackageType('BAG')">
                                        <i class="fas fa-shopping-bag"></i>
                                        Bag
                                    </div>
                                    <input type="radio" name="packageType" value="BAG" 
                                           class="d-none" ${packing.packageType == 'BAG' ? 'checked' : ''}/>
                                </div>
                                <div class="col-6">
                                    <div class="package-type-btn ${packing.packageType == 'PALLET' ? 'selected' : ''}" 
                                         onclick="selectPackageType('PALLET')">
                                        <i class="fas fa-pallet"></i>
                                        Pallet
                                    </div>
                                    <input type="radio" name="packageType" value="PALLET" 
                                           class="d-none" ${packing.packageType == 'PALLET' ? 'checked' : ''}/>
                                </div>
                            </div>
                        </div>

                        <div class="row mb-3">
                            <div class="col-8">
                                <label class="form-label fw-bold">Weight</label>
                                <input type="number" name="weight" class="form-control" step="0.01" min="0"
                                       placeholder="0.00" value="${packing.weight}"/>
                            </div>
                            <div class="col-4">
                                <label class="form-label fw-bold">Unit</label>
                                <select name="weightUnit" class="form-select">
                                    <option value="kg" ${packing.weightUnit == 'kg' ? 'selected' : ''}>kg</option>
                                    <option value="lb" ${packing.weightUnit == 'lb' ? 'selected' : ''}>lb</option>
                                    <option value="g" ${packing.weightUnit == 'g' ? 'selected' : ''}>g</option>
                                </select>
                            </div>
                        </div>

                        <div class="row mb-3">
                            <div class="col-6">
                                <label class="form-label fw-bold">Total Packages</label>
                                <input type="number" name="totalPackages" class="form-control" min="1" value="${packing.totalPackages != null ? packing.totalPackages : 1}"/>
                            </div>
                            <div class="col-6">
                                <label class="form-label fw-bold">Package #</label>
                                <input type="number" name="currentPackage" class="form-control" min="1" 
                                       max="${packing.totalPackages != null ? packing.totalPackages : 1}"
                                       value="${packing.currentPackageNum != null ? packing.currentPackageNum : 1}"/>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Notes</label>
                            <textarea name="notes" class="form-control" rows="2" 
                                      placeholder="Special handling instructions...">${packing.notes}</textarea>
                        </div>

                        <button type="submit" class="btn btn-primary w-100 mb-2">
                            <i class="fas fa-save me-1"></i> Save Package Info
                        </button>
                    </form>

                    <c:if test="${packing.status == 'IN_PROGRESS' || packing.status == 'PENDING'}">
                        <form action="${pageContext.request.contextPath}/packing" method="post">
                            <input type="hidden" name="action" value="complete"/>
                            <input type="hidden" name="packId" value="${packing.packId}"/>
                            <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                            <input type="hidden" name="packageLabel" value="${packing.packageLabel}"/>
                            <input type="hidden" name="packageType" value="${packing.packageType}"/>
                            <input type="hidden" name="weight" value="${packing.weight}"/>
                            <input type="hidden" name="weightUnit" value="${packing.weightUnit}"/>
                            <input type="hidden" name="notes" value="${packing.notes}"/>
                            <button type="submit" class="btn btn-success w-100">
                                <i class="fas fa-check-circle me-1"></i> Complete Packing & Confirm GDN
                            </button>
                        </form>
                    </c:if>
                </c:if>

                <c:if test="${packing.status == 'DONE'}">
                    <div class="alert alert-success mb-3">
                        <i class="fas fa-check-circle me-2"></i> Packing Completed
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold text-muted">Package Label</label>
                        <p class="mb-1 fs-5">${packing.packageLabel}</p>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6">
                            <label class="form-label text-muted small">Type</label>
                            <p class="mb-0">${packing.packageType}</p>
                        </div>
                        <div class="col-6">
                            <label class="form-label text-muted small">Weight</label>
                            <p class="mb-0">${packing.weight} ${packing.weightUnit}</p>
                        </div>
                    </div>
                    <c:if test="${not empty packing.notes}">
                        <div class="mb-3">
                            <label class="form-label text-muted small">Notes</label>
                            <p class="mb-0">${packing.notes}</p>
                        </div>
                    </c:if>
                    <div class="mb-3">
                        <label class="form-label text-muted small">Packed By</label>
                        <p class="mb-0">${packing.packedByName}</p>
                    </div>
                    <c:if test="${gdn.status == 'SHIPPING'}">
                        <a href="${pageContext.request.contextPath}/shipment?action=create&gdnId=${gdn.gdnId}&soNumber=${gdn.soNumber}" 
                           class="btn btn-primary w-100">
                            <i class="fas fa-truck me-1"></i> Create Shipment
                        </a>
                    </c:if>
                </c:if>
            </div>
        </div>
    </div>

    <div class="modal fade" id="packModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/packing" method="get">
                    <input type="hidden" name="action" value="packLine"/>
                    <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                    <input type="hidden" name="gdnLineId" id="modalGdnLineId"/>
                    <div class="modal-header">
                        <h5 class="modal-title"><i class="fas fa-box me-2"></i>Pack Item</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Quantity to Pack</label>
                            <input type="number" name="qtyPacked" id="modalQtyPacked" class="form-control form-control-lg" 
                                   min="1" required/>
                            <small class="text-muted">Max: <span id="modalMaxQty">0</span></small>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-check me-1"></i> Confirm Pack
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        function selectPackageType(type) {
            document.querySelectorAll('.package-type-btn').forEach(btn => btn.classList.remove('selected'));
            document.querySelector(`input[value="${type}"]`).checked = true;
            document.querySelector(`.package-type-btn:has(input[value="${type}"])`).classList.add('selected');
        }

        function showPackModal(gdnLineId, maxQty) {
            document.getElementById('modalGdnLineId').value = gdnLineId;
            document.getElementById('modalQtyPacked').max = maxQty;
            document.getElementById('modalQtyPacked').value = maxQty;
            document.getElementById('modalMaxQty').textContent = maxQty;
            new bootstrap.Modal(document.getElementById('packModal')).show();
        }

        document.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('message')) {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-success alert-dismissible fade show';
                alertDiv.innerHTML = urlParams.get('message') + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
                document.querySelector('.container-fluid').prepend(alertDiv);
            }
        });
    </script>
</t:layout>