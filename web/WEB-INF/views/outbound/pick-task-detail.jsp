<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<t:layout title="Task Detail: #${task.pickTaskId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <t:link url="${pageContext.request.contextPath}/pick-task?action=myTasks"
                    color="dark" variant="split" icon="arrow-left">
                Go back
            </t:link>
            <c:if test="${task.status == 'ASSIGNED'}">
                <form action="${pageContext.request.contextPath}/pick-task" method="post" class="m-0">
                    <input type="hidden" name="action" value="start"/>
                    <input type="hidden" name="id" value="${task.pickTaskId}"/>
                    <button type="submit" class="btn btn-success d-flex gap-2 align-items-center">
                        <i class="bi bi-play-fill"></i>
                        <span>Start Task</span>
                    </button>
                </form>
            </c:if>
        </div>
    </jsp:attribute>

    <jsp:body>
        <div class="row g-4">
            <!-- Task Info Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-info-circle me-2"></i>Task Information
                        </h5>
                    </div>
                    <div class="card-body">
                        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Task ID</p>
                                <p class="mb-0 fw-semibold">#${task.pickTaskId}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN / SO</p>
                                <p class="mb-0 fw-semibold">
                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${task.gdnId}"
                                       class="text-decoration-none">
                                        ${task.gdnNumber}
                                    </a>
                                    <span class="text-muted mx-1">/</span>
                                    <c:choose>
                                        <c:when test="${not empty task.soId}">
                                            <a href="${pageContext.request.contextPath}/sales-orders?action=detail&id=${task.soId}"
                                               class="text-decoration-none">
                                                ${task.soNumber}
                                            </a>
                                        </c:when>
                                        <c:otherwise>${task.soNumber != null ? task.soNumber : "-"}</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge rounded-pill
                                          ${task.status == 'ASSIGNED' ? 'bg-info' :
                                            (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-success')}
                                          fw-semibold">
                                        ${task.status}
                                    </span>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Total Lines</p>
                                <p class="mb-0 fw-semibold">${task.totalLines != null ? task.totalLines : (task.lines != null ? task.lines.size() : 0)}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Zone Progress Summary -->
            <c:if test="${task.status == 'IN_PROGRESS'}">
                <div class="col-lg-12">
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-success text-white py-2">
                            <h6 class="mb-0">
                                <i class="bi bi-pie-chart me-2"></i>Picking Progress by Zone
                            </h6>
                        </div>
                        <div class="card-body">
                            <div class="row" id="zoneProgressContainer">
                                <!-- Zone progress cards will be dynamically generated -->
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- Pick Lines Table with Zone Grouping -->
            <div class="col-lg-12">
                <form action="${pageContext.request.contextPath}/pick-task" method="post" id="completeForm">
                    <input type="hidden" name="action" value="complete"/>
                    <input type="hidden" name="pickTaskId" value="${task.pickTaskId}"/>

                    <div class="card shadow-sm border-0">
                        <div class="card-header bg-light py-3">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0">
                                    <i class="bi bi-list-check me-2"></i>Pick Lines
                                    <span class="badge bg-primary">${task.lines != null ? task.lines.size() : 0} lines</span>
                                </h5>
                                <c:if test="${task.status == 'IN_PROGRESS'}">
                                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="expandAll()">
                                        <i class="bi bi-arrows-expand me-1"></i>Expand All
                                    </button>
                                    <button type="button" class="btn btn-sm btn-outline-secondary ms-2" onclick="collapseAll()">
                                        <i class="bi bi-arrows-collapse me-1"></i>Collapse All
                                    </button>
                                </c:if>
                            </div>
                        </div>
                        <div class="card-body p-0">
                            <div class="accordion" id="zoneAccordion">
                                <c:set var="currentZone" value="" />
                                <c:set var="zoneIndex" value="0" />
                                <c:set var="zoneCompleted" value="0" />
                                <c:set var="zoneTotal" value="0" />

                                <c:forEach var="line" items="${task.lines}">
                                    <c:if test="${line.zoneCode != currentZone}">
                                        <!-- Close previous zone -->
                                        <c:if test="${currentZone != ''}">
                                        </div>
                                        <div class="accordion-body p-0">
                                            <table class="table table-hover mb-0">
                                                <tbody>
                                                    <c:forTokens var="prevLine" items="${zoneLines}" delims="," varStatus="status">
                                                        <!-- Previous zone lines rendered here -->
                                                    </c:forTokens>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:if>

                                    <!-- New zone header -->
                                    <c:set var="currentZone" value="${line.zoneCode}" />
                                    <c:set var="zoneIndex" value="${zoneIndex + 1}" />
                                    <c:set var="zoneCompleted" value="0" />
                                    <c:set var="zoneTotal" value="0" />

                                    <div class="accordion-item">
                                        <h2 class="accordion-header" id="zone${zoneIndex}Header">
                                            <button class="accordion-button ${zoneIndex > 1 ? 'collapsed' : ''}"
                                                    type="button"
                                                    data-bs-toggle="collapse"
                                                    data-bs-target="#zone${zoneIndex}Collapse"
                                                    aria-expanded="${zoneIndex == 1}"
                                                    aria-controls="zone${zoneIndex}Collapse">
                                                <div class="d-flex justify-content-between align-items-center w-100">
                                                    <span>
                                                        <i class="bi bi-geo-alt me-2"></i>
                                                        <strong>Zone ${line.zoneCode}</strong>
                                                    </span>
                                                    <span class="badge bg-primary" id="zone${zoneIndex}Progress">
                                                        <span class="zone-completed">0</span> / <span class="zone-total">0</span> completed
                                                    </span>
                                                </div>
                                            </button>
                                        </h2>
                                        <div id="zone${zoneIndex}Collapse"
                                             class="accordion-collapse collapse ${zoneIndex == 1 ? 'show' : ''}"
                                             data-bs-parent="#zoneAccordion">
                                            <div class="accordion-body p-0">
                                                <table class="table table-hover mb-0 zone-table" data-zone="${line.zoneCode}">
                                                    <thead class="table-light">
                                                        <tr>
                                                            <th style="width: 50px;" class="text-center">#</th>
                                                            <th>Slot</th>
                                                            <th>Variant / Product</th>
                                                            <th style="width: 120px;" class="text-center">Qty to Pick</th>
                                                            <th style="width: 130px;" class="text-center">Qty Picked</th>
                                                            <th style="width: 100px;" class="text-center">Status</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                    </c:if>

                                                    <!-- Render line item -->
                                                    <c:set var="zoneTotal" value="${zoneTotal + 1}" />
                                                    <c:if test="${line.pickStatus == 'DONE'}">
                                                        <c:set var="zoneCompleted" value="${zoneCompleted + 1}" />
                                                    </c:if>

                                                    <tr class="pick-line-row ${line.pickStatus == 'DONE' ? 'table-success' : ''}"
                                                        data-line-id="${line.pickTaskLineId}"
                                                        data-zone="${line.zoneCode}"
                                                        data-qty-to-pick="${line.qtyToPick}"
                                                        data-status="${line.pickStatus}">
                                                        <td class="text-center text-muted">${zoneTotal}</td>
                                                        <td class="fw-bold text-primary">${line.slotCode}</td>
                                                        <td>
                                                            <span class="fw-bold">${line.variantSku}</span>
                                                            <span class="text-muted mx-2">|</span>
                                                            ${line.productName}
                                                            <c:if test="${not empty line.color or not empty line.size}">
                                                                <br/><small class="text-muted">
                                                                    <c:if test="${not empty line.color}">Color: ${line.color}</c:if>
                                                                    <c:if test="${not empty line.size and not empty line.color}"> | </c:if>
                                                                    <c:if test="${not empty line.size}">Size: ${line.size}</c:if>
                                                                    </small>
                                                            </c:if>
                                                        </td>
                                                        <td class="text-center">
                                                            <fmt:formatNumber value="${line.qtyToPick}" maxFractionDigits="0"/>
                                                        </td>
                                                        <td class="text-center">
                                                            <input type="hidden" name="lineIds" value="${line.pickTaskLineId}"/>
                                                            <input type="number"
                                                                   name="qtyPicked"
                                                                   class="form-control form-control-sm text-center fw-bold qty-picked-input"
                                                                   min="0"
                                                                   step="1"
                                                                   data-line-id="${line.pickTaskLineId}"
                                                                   data-qty-to-pick="${line.qtyToPick}"
                                                                   value="${line.qtyPicked != null ? line.qtyPicked.stripTrailingZeros().toPlainString() : '0'}"
                                                                   ${task.status != 'IN_PROGRESS' ? 'disabled' : ''}
                                                                   onchange="validateQty(this)"/>
                                                            <div class="invalid-feedback invalid-qty-msg" style="display: none;">
                                                                <small>Qty > to pick!</small>
                                                            </div>
                                                        </td>
                                                        <td class="text-center">
                                                            <span class="badge ${line.pickStatus == 'PENDING' ? 'bg-light text-dark border' :
                                                                                 (line.pickStatus == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-success')}">
                                                                      ${line.pickStatus}
                                                                  </span>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>

                                                    <!-- Close last zone -->
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Complete Button -->
                            <c:if test="${task.status == 'IN_PROGRESS'}">
                                <div class="col-lg-12">
                                    <div class="card shadow-sm border-0 mt-4">
                                        <div class="card-body">
                                            <div class="d-flex justify-content-between align-items-center">
                                                <div>
                                                    <p class="mb-0 text-muted">
                                                        <i class="bi bi-info-circle me-2"></i>
                                                        Review all quantities before completing the task.
                                                    </p>
                                                </div>
                                                <button type="submit" form="completeForm" class="btn btn-primary btn-lg px-5 shadow">
                                                    <i class="bi bi-check-circle-fill me-2"></i>Complete Task
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                    </form>
                </div>
            </div>

            <!-- Validation Warning Modal -->
            <div class="modal fade" id="validationModal" tabindex="-1" aria-labelledby="validationModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header bg-warning">
                            <h5 class="modal-title" id="validationModalLabel">
                                <i class="bi bi-exclamation-triangle me-2"></i>Validation Warning
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <p class="mb-0" id="validationMessage"></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">OK</button>
                        </div>
                    </div>
                </div>
            </div>

            <script>
                // Zone progress tracking
                const zoneData = {};

                document.addEventListener('DOMContentLoaded', function () {
                  // Initialize zone data
                  const zoneTables = document.querySelectorAll('.zone-table');
                  zoneTables.forEach(table => {
                    const zone = table.dataset.zone;
                    const rows = table.querySelectorAll('.pick-line-row');
                    let completed = 0;
                    rows.forEach(row => {
                      if (row.dataset.status === 'DONE') {
                        completed++;
                      }
                    });
                    zoneData[zone] = {completed: completed, total: rows.length};
                  });

                  // Update zone progress badges
                  updateZoneProgress();

                  // Add focus highlight to pick lines
                  const inputs = document.querySelectorAll('.qty-picked-input');
                  inputs.forEach(input => {
                    input.addEventListener('focus', function () {
                      const row = this.closest('tr');
                      row.classList.add('table-active');
                    });

                    input.addEventListener('blur', function () {
                      const row = this.closest('tr');
                      row.classList.remove('table-active');
                    });
                  });
                });

                function updateZoneProgress() {
                  for (const [zone, data] of Object.entries(zoneData)) {
                    // Find all progress badges for this zone
                    const badges = document.querySelectorAll(`.accordion-button[aria-controls*="zone"][data-bs-target*="${zone}"] .zone-completed,
                                                                  .badge:has(.zone-completed)`);
                    badges.forEach(badge => {
                      const completedSpan = badge.querySelector('.zone-completed') || badge;
                      const totalSpan = badge.querySelector('.zone-total') || badge.parentElement;
                      if (completedSpan)
                        completedSpan.textContent = data.completed;
                      if (totalSpan)
                        totalSpan.textContent = data.total;
                    });
                  }
                }

                function validateQty(input) {
                  const qtyToPick = parseFloat(input.dataset.qtyToPick);
                  const qtyPicked = parseFloat(input.value);
                  const row = input.closest('tr');
                  const invalidMsg = row.querySelector('.invalid-qty-msg');

                  if (qtyPicked > qtyToPick) {
                    input.classList.add('is-invalid');
                    if (invalidMsg)
                      invalidMsg.style.display = 'block';

                    // Show warning modal
                    showValidationWarning(`Quantity picked (${qtyPicked}) is greater than quantity to pick (${qtyToPick}) for line ${input.dataset.lineId}`);
                  } else {
                    input.classList.remove('is-invalid');
                    if (invalidMsg)
                      invalidMsg.style.display = 'none';

                    // Update line status
                    const lineId = input.dataset.lineId;
                    const statusBadge = row.querySelector('.badge');
                    if (qtyPicked > 0) {
                      statusBadge.classList.remove('bg-light', 'text-dark');
                      statusBadge.classList.add('bg-warning', 'text-dark');
                      statusBadge.textContent = 'IN_PROGRESS';
                    }
                  }
                }

                function showValidationWarning(message) {
                  document.getElementById('validationMessage').textContent = message;
                  const modal = new bootstrap.Modal(document.getElementById('validationModal'));
                  modal.show();
                }

                function expandAll() {
                  const collapses = document.querySelectorAll('.accordion-collapse');
                  collapses.forEach(collapse => {
                    const bsCollapse = new bootstrap.Collapse(collapse, {toggle: false});
                    bsCollapse.show();
                  });
                }

                function collapseAll() {
                  const collapses = document.querySelectorAll('.accordion-collapse');
                  collapses.forEach(collapse => {
                    const bsCollapse = new bootstrap.Collapse(collapse, {toggle: false});
                    bsCollapse.hide();
                  });
                }

                // Form validation before submit
                document.getElementById('completeForm').addEventListener('submit', function (e) {
                  const invalidInputs = document.querySelectorAll('.qty-picked-input.is-invalid');
                  if (invalidInputs.length > 0) {
                    e.preventDefault();
                    showValidationWarning('Please correct quantities that exceed the quantity to pick before completing the task.');
                  }
                });
            </script>
        </jsp:body>
    </t:layout>
